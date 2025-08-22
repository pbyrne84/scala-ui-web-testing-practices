package pbyrne84.browsertesting.repo

import pbyrne84.browsertesting.models.{NewUser, Tables, User}
import slick.jdbc.PostgresProfile

import java.time.{Clock, Instant}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class UserRepository(
    db: PostgresProfile.backend.JdbcDatabaseDef,
    clock: Clock,
    uuidProvider: () => UUID = () => UUID.randomUUID()
)(implicit ec: ExecutionContext) {
  import slick.jdbc.PostgresProfile.api.*

  def create(newUser: NewUser): Future[Either[RuntimeException, User]] = {
    val now = Instant.now(clock)
    val uuid = uuidProvider()

    val user = User(uuid, newUser.name, now, now)

    db.run(Tables.userTable += user)
      .flatMap { _ =>
        get(uuid).map {
          case Some(user) => Right(user)
          // could just return the item instead.
          case None => Left(new RuntimeException(s"Record $uuid not found after inserting"))
        }
      }
  }

  def get(uuid: UUID): Future[Option[User]] = {
    db.run {
      Tables.userTable.filter(_.id === uuid).result.headOption
    }
  }
}
