package pbyrne84.browsertesting

import org.scalatest.concurrent.ScalaFutures
import pbyrne84.browsertesting.repo.SearchDatabase
import pbyrne84.browsertesting.routes.SuperBayRoutes
import pbyrne84.browsertesting.setup.DatabaseSetup

import java.util.UUID
import scala.concurrent.{ExecutionContextExecutor, Future}

// ScalaFutures is a test thing that add .futureValue so you don't have to mess with await blocks
// production code should VERY RARELY block, usually hidden in a final point before showing within the framework
object TestBoot extends ScalaFutures {

  def main(args: Array[String]): Unit = {
    createData()

    SuperBayRoutes.main(args)
  }

  private def createData() = {
    implicit val ec: ExecutionContextExecutor = SuperBayRoutes.executionContext

    val databaseSetup = new DatabaseSetup(SearchDatabase.dbConfig, SearchDatabase.db)
    databaseSetup.reset

    val eventualUsers = (0 to 5).map { _ =>
      databaseSetup.user.generate(UUID.randomUUID())
    }

    val users = Future.sequence(eventualUsers).futureValue

    val eventualItems = (1 to 100).map { index =>
      val modulus = index % 5
      val user = users(modulus)

      val title =
        List(s"tiger $index", s"wolf $index", s"elephant $index", s"lion $index", s"sea lion $index")(modulus)

      databaseSetup.item.generate(
        id = UUID.randomUUID(),
        userId = user.id,
        hourOffset = index,
        titleFormat = title,
        price = (10000 - index) * index
      )
    }

    Future.sequence(eventualItems).futureValue

  }
}
