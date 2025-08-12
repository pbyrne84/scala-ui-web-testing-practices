package pbyrne84.browsertesting.repo

import pbyrne84.browsertesting.BaseSpec
import pbyrne84.browsertesting.models.{NewUser, User}
import pbyrne84.browsertesting.repo.UserRepository

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}
import java.util.UUID

class UserRepositorySpec extends BaseSpec {

  private val clock: Clock = mock[Clock]
  private val uuidProvider = mockFunction[UUID]
  private lazy val userRepository = new UserRepository(db, clock, uuidProvider)

  before {
    reset()
  }

  "create must" - {
    "return a created one when successful" in {
      val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)

      (clock.instant _)
        .expects()
        .returns(now)

      val uuid = UUID.randomUUID()

      uuidProvider
        .expects()
        .returns(uuid)

      val name = "jimmy"
      val user = userRepository.create(NewUser.apply(name, now, now)).futureValue.value

      val itemInDb = databaseSetup.user.get(uuid).futureValue.value

      user mustBe itemInDb
      user mustBe User(uuid, name = name, created = now, updated = now)
    }
  }

  "get must" - {
    "return none when a record is not found and there are no entries" in {
      userRepository.get(UUID.randomUUID()).futureValue mustBe None
    }

    "return none when a record is not found and there are many entries but none match" in {
      databaseSetup.user.generate(UUID.randomUUID()).futureValue
      databaseSetup.user.generate(UUID.randomUUID()).futureValue
      databaseSetup.user.generate(UUID.randomUUID()).futureValue

      userRepository.get(UUID.randomUUID()).futureValue mustBe None
    }

    "return a record when one is one is found for the id and there are many entries" in {
      // We want to have enough values with enough value spread to guarantee ALL elements of a where clause
      databaseSetup.user.generate(UUID.randomUUID()).futureValue
      val expectedItem = databaseSetup.user.generate(UUID.randomUUID()).futureValue
      databaseSetup.user.generate(UUID.randomUUID()).futureValue

      userRepository.get(expectedItem.id).futureValue mustBe Some(expectedItem)
    }
  }
}
