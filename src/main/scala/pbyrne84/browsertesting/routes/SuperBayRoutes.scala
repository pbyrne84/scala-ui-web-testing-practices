package pbyrne84.browsertesting.routes

import com.typesafe.config.ConfigFactory
import org.apache.pekko.actor.typed.ActorSystem
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.*
import org.apache.pekko.http.scaladsl.server.Directives.*
import org.apache.pekko.http.scaladsl.server.Route
import pbyrne84.browsertesting.controllers.SearchController
import pbyrne84.browsertesting.models.{DbConfig, Item, SearchResult, SearchResults}
import pbyrne84.browsertesting.repo.{ItemRepository, SearchDatabase}
import pbyrne84.browsertesting.setup.DatabaseSetup
import slick.jdbc.PostgresProfile

import java.net.URLEncoder
import java.time.{Clock, Instant}
import java.util.{TimeZone, UUID}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.io.StdIn
import scala.util.Using

object SuperBayInit {

  def init(implicit ec: ExecutionContext): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    lazy val config             = ConfigFactory.load()
    lazy val dbConfig: DbConfig = DbConfig.fromConfig(config)
    import slick.jdbc.PostgresProfile.api.*
    lazy val db: PostgresProfile.backend.JdbcDatabaseDef = Database.forURL(
      dbConfig.url
    )

    val databaseSetup: DatabaseSetup = new DatabaseSetup(dbConfig, db)
    databaseSetup.reset

    val userUuids = List.fill(10)(UUID.randomUUID())
    Await.result(Future.sequence(userUuids.map(databaseSetup.user.generate)), Duration.Inf)

    val eventualItems = userUuids.flatMap { (userUuid: UUID) =>
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
  }
}

object SuperBayRoutes {

  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  def main(args: Array[String]): Unit = {

    SuperBayInit.init

    val superBayRoutes = new SuperBayRoutes(
      new SearchController(new ItemRepository(SearchDatabase.db, Clock.systemUTC(), () => UUID.randomUUID()), 20)
    )

    val bindingFuture = Http().newServerAt("localhost", 8080).bind(superBayRoutes.route)

    println(s"Server now online. Please navigate to http://localhost:8080\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind())                 // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}

class SuperBayRoutes(searchController: SearchController) {
  val errorOrCss = Using(scala.io.Source.fromResource("superbay.css")) { cssFile =>
    cssFile.getLines().mkString("\n")
  }.toEither

  val route: Route =
    pathSingleSlash {
      get {
        onSuccess(searchController.search("", 0)) { results =>
          complete {
            HttpEntity(
              ContentTypes.`text/html(UTF-8)`,
              superbay.html.superBayHomePage().body
            )
          }
        }
      }
    }
      ~ path("superbay.css") {
        complete {
          errorOrCss match {
            case Left(error) =>
              HttpEntity(
                ContentTypes.`text/html(UTF-8)`,
                error.getMessage
              )

            case Right(cssContents) =>
              HttpEntity(
                MediaTypes.`text/css`.withCharset(HttpCharsets.`UTF-8`),
                cssContents
              )
          }
        }
      }
      ~ path("search") {
        parameters("searchText") { searchText =>
          onSuccess(searchController.search(searchText, 0)) { results =>
            complete {

              val uuid = UUID.randomUUID()
              val empty = SearchResults(
                searchText,
                20,
                (0, 1),
                List(
                  SearchResult(
                    uuid,
                    "title",
                    "brief description",
                    BigDecimal(20),
                    s"/result/$uuid?searchText=$searchText"
                  )
                )
              )
              HttpEntity(
                ContentTypes.`text/html(UTF-8)`,
                superbay.html.superBaySearchResults(empty, Some(searchText)).body
              )
            }
          }
        }
      } ~ path("result" / JavaUUID) { itemId =>
        parameters("searchText".?) { maybeSearchText =>
          val maybeSearchUrl =
            maybeSearchText.map(searchText => s"/search?searchText=${URLEncoder.encode(searchText, "UTF-8")}")

          onSuccess(searchController.get(itemId)) {

            case Some(item) =>
              complete {
                HttpEntity(
                  ContentTypes.`text/html(UTF-8)`,
                  superbay.html.superBayItem.apply(item, maybeSearchText, maybeSearchUrl).body
                )
              }

            case _ =>
              val item = Item(
                id = itemId,
                userId = UUID.randomUUID(),
                title = s"title $itemId",
                briefDescription = s"briefDescription $itemId",
                description = s"description $itemId",
                price = 22,
                created = Instant.now(),
                updated = Instant.now()
              )
              complete {
                HttpEntity(
                  ContentTypes.`text/html(UTF-8)`,
                  superbay.html.superBayItem.apply(item, maybeSearchText, maybeSearchUrl).body
                )
              }

          }
        }
      }

}
