package pbyrne84.browsertesting.routes

import com.typesafe.config.ConfigFactory
import pbyrne84.browsertesting.controllers.SearchController
import pbyrne84.browsertesting.models.DbConfig
import pbyrne84.browsertesting.repo.{ItemRepository, SearchDatabase}
import pbyrne84.browsertesting.services.SearchService
import pbyrne84.browsertesting.setup.DatabaseSetup
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model._
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import slick.jdbc.PostgresProfile

import java.time.Clock
import java.util.{TimeZone, UUID}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import scala.io.StdIn

object SuperBayRoutes {

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  def main(args: Array[String]): Unit = {

    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    lazy val config = ConfigFactory.load()
    lazy val dbConfig: DbConfig = DbConfig.fromConfig(config)

    import slick.jdbc.PostgresProfile.api._
    lazy val db: PostgresProfile.backend.JdbcDatabaseDef = Database.forURL(
      dbConfig.url
    )

    val databaseSetup: DatabaseSetup = new DatabaseSetup(dbConfig, db)
    databaseSetup.reset

    val userUuids = List.fill(10)(UUID.randomUUID())
    Await.result(Future.sequence(userUuids.map(databaseSetup.user.generate)), Duration.Inf)

    val eventualItems = userUuids.flatMap { userUuid: UUID =>
      (1 to 10).map { index =>
        databaseSetup.item.generate(
          id = UUID.randomUUID(),
          userId = userUuid,
          hourOffset = -index,
          titleFormat = s"Item $index from user $userUuid",
          briefDescriptionFormat = s"Short description for item $index from user $userUuid",
          descriptionFormat = s"This is the full description for item $index from user $userUuid",
          price = index * 100
        )
      }
    }

    Await.result(Future.sequence(eventualItems), Duration.Inf)

    val superBayRoutes = new SuperBayRoutes(
      new SearchController(new ItemRepository(SearchDatabase.db, Clock.systemUTC(), () => UUID.randomUUID()), 20)
    )

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(superBayRoutes.route)

    println(s"Server now online. Please navigate to http://localhost:8080\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}

class SuperBayRoutes(searchController: SearchController) {
  val route: Route =
    pathSingleSlash {
      get {
        onSuccess(searchController.search("", 0)) { results =>
          complete {
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              html.searchResults.apply(results).body
            )
          }
        }
      }
    } ~ path("search") {
      parameters("searchText") { searchText =>
        onSuccess(searchController.search(searchText, 0)) { results =>
          complete {
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              html.searchResults.apply(results).body
            )
          }
        }
      }
    } ~ path("result" / JavaUUID) { itemId =>
      onSuccess(searchController.get(itemId)) {
        case Some(item) =>
          complete {
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              html.item.apply(item).body
            )
          }

        case None =>
          complete(
            StatusCodes.NotFound,
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              s"Item id $itemId not found"
            )
          )

      }
    }

}
