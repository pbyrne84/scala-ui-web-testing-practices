package pbyrne84.browsertesting.util
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import pbyrne84.browsertesting.controllers.SearchController
import pbyrne84.browsertesting.repo.{ItemRepository, SearchDatabase}
import pbyrne84.browsertesting.routes.{SuperBayInit, SuperBayRoutes}

import java.time.Clock
import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.io.StdIn

object ApiRunner {

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  lazy val bindingFuture: Future[Http.ServerBinding] = {
    SuperBayInit.init

    val superBayRoutes = new SuperBayRoutes(
      new SearchController(new ItemRepository(SearchDatabase.db, Clock.systemUTC(), () => UUID.randomUUID()), 20)
    )

    Http().newServerAt("localhost", 8080).bind(superBayRoutes.route)

  }

  def init(): Unit = {
    Await.result(bindingFuture, Duration.Inf)
  }

}

trait ApiRunner {
  final private val initialised: Unit = ApiRunner.init()
}
