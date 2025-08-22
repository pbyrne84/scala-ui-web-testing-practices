package pbyrne84.browsertesting.routes

import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import pbyrne84.browsertesting.controllers.SearchController
import pbyrne84.browsertesting.repo.{ItemRepository, SearchDatabase}
import pbyrne84.browsertesting.routes.SuperBayRoutes
import pbyrne84.browsertesting.routes.SuperBayRoutes.system

import java.net.ServerSocket
import java.time.Clock
import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

object TestService {

  lazy val startedServerPort: Int = {

    implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "test-system")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    // Auto detect free port
    val serverSocket = new ServerSocket(0)
    val superBayRoutes = new SuperBayRoutes(
      new SearchController(new ItemRepository(SearchDatabase.db, Clock.systemUTC(), () => UUID.randomUUID()), 20)
    )

    println(s"Starting service on http://localhost:${serverSocket.getLocalPort}")

    val bindingFuture = Http().newServerAt("localhost", serverSocket.getLocalPort).bind(superBayRoutes.route)

    Await.result(bindingFuture, Duration.Inf)

    // clean up on shutdown
    scala.sys.addShutdownHook {
      bindingFuture
        .flatMap(_.unbind()) // trigger unbinding from the port
        .onComplete(_ => system.terminate()) // and shutdown when done
    }

    serverSocket.getLocalPort
  }

}
