package pbyrne84.browsertesting.setup

import pbyrne84.browsertesting.migration.DatabaseMigration
import pbyrne84.browsertesting.models.{DbConfig, Item, Tables, User}
import slick.jdbc.PostgresProfile

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.{TimeZone, UUID}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.jdk.CollectionConverters.ListHasAsScala

class DatabaseSetup(dbConfig: DbConfig, db: PostgresProfile.backend.JdbcDatabaseDef)(implicit ec: ExecutionContext) {

  import slick.jdbc.PostgresProfile.api.*

  implicit class FutureOps[A](futures: Seq[Future[A]]) {
    def asFutureSequence: Future[Seq[A]] = Future.sequence(futures)
  }

  def reset: List[Int] = {
    // this was fun, BST was breaking stuff. If something is going to break in a test, dates most likely the cause
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    val migration = new DatabaseMigration(dbConfig)
    val migrate   = migration.migrate

    migrate.migrations.asScala.foreach(a => println(a.filepath))

    val allTables =
      List(
        Tables.itemTable.delete,
        Tables.userTable.delete
      )

    allTables.map(table => Await.result(db.run(table), Duration.Inf))
  }

  def generateMany(userCount: Int, itemsPerUser: Int): Future[Seq[(User, Seq[Item])]] = {
    val userUuids = List.fill(userCount)(UUID.randomUUID())
    userUuids.map(user.generate).asFutureSequence.flatMap { (generatedUsers: Seq[User]) =>
      generatedUsers.map { user =>
        (1 to itemsPerUser)
          .map { index =>
            val userUuid = user.id
            item.generate(
              id = UUID.randomUUID(),
              userId = userUuid,
              hourOffset = -index,
              titleFormat = s"Item $index from user $userUuid",
              briefDescriptionFormat = s"Short description for item $index from user $userUuid",
              descriptionFormat = s"This is the full description for item $index from user $userUuid",
              price = index * 100
            )
          }
          .asFutureSequence
          .map(items => user -> items)
      }.asFutureSequence
    }
  }

  // organising it like this has the benefit of both being lazy for now and not a problem
  // to extract class later to ItemDataSetup with no code changes to the callers
  // e.g. val item = new ItemDataSetup
  // The harder something is to reorganise the less likely it will be done
  object item {
    def get(id: UUID): Future[Option[Item]] = {
      // So this can be a bit of contention about why don't we use the main implementations and have a copy.
      // The reason is main versions can changed to business requirements causing the behaviour of tests to change,
      // sometimes in not very clear ways. Basically every PR has to be scrutinised more and people tend to ignore tests
      db.run {
        Tables.itemTable.filter(_.id === id).result.headOption
      }
    }

    def generate(
        id: UUID,
        userId: UUID,
        hourOffset: Int = 0,
        titleFormat: String = "title %s",
        briefDescriptionFormat: String = "briefDescription %s",
        descriptionFormat: String = "description %s",
        price: Int = 0
    ): Future[Item] = {
      val item = Item(
        id = id,
        userId = userId,
        title = titleFormat.format(id),
        briefDescription = briefDescriptionFormat.format(id),
        description = descriptionFormat.format(id),
        price = price,
        created = createNow.plusSeconds(60 * 60 * hourOffset),
        updated = createNow.plusSeconds(60 * 60 * hourOffset)
      )

      db.run {
        Tables.itemTable += item
      }.map(_ => item)
    }

    def getAll: Future[List[Item]] = {
      db.run {
        Tables.itemTable.sorted(_.id).result
      }.map(_.toList)
    }
  }

  private def createNow: Instant = {
    // Accuracy is lost when stored, so value is needed to be truncated so it can match
    Instant.now().truncatedTo(ChronoUnit.MILLIS)
  }

  object user {

    def get(id: UUID): Future[Option[User]] = {
      db.run {
        Tables.userTable.filter(_.id === id).result.headOption
      }
    }

    def generate(id: UUID): Future[User] = {
      val user = User(id = id, name = s"name $id", created = createNow, updated = createNow)

      db.run {
        Tables.userTable += user
      }.map(_ => user)
    }
  }

  def getAll: Future[List[User]] = {
    db.run {
      Tables.userTable.sorted(_.id).result
    }.map(_.toList)
  }

}

object DatabaseSetup {}
