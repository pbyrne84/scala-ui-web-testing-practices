package pbyrne84.browsertesting.repo

import pbyrne84.browsertesting.BaseSpec
import pbyrne84.browsertesting.models.{Item, NewItem, SearchResult, SearchResults}
import pbyrne84.browsertesting.repo.ItemRepository

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant}
import java.util.UUID

class ItemRepositorySpec extends BaseSpec {
  private val clock: Clock = mock[Clock]
  private val uuidProvider = mockFunction[UUID]

  private lazy val itemRepository = new ItemRepository(db, clock, uuidProvider)
  private val commonUserId = UUID.randomUUID()

  before {
    reset()
    databaseSetup.user.generate(commonUserId).futureValue
  }

  "create must" - {
    "add an item" in {
      // Accuracy is lost going in the DB so this would not be able to be used to compare later
      val now = Instant.now().truncatedTo(ChronoUnit.MILLIS)

      (clock.instant _)
        .expects()
        .returns(now)

      val uuid = UUID.randomUUID()

      uuidProvider
        .expects()
        .returns(uuid)

      val title = "itemTitle"
      val briefDescription = "brief Description"
      val description = "full description"
      val price = 22

      val createdItem =
        itemRepository
          .create(
            NewItem(
              userId = commonUserId,
              title = title,
              briefDescription = briefDescription,
              description = description,
              price = price
            )
          )
          .futureValue
          .value

      val itemInDb = databaseSetup.item.get(uuid).futureValue.value

      // Could have returned junk, or not saved in db
      createdItem mustBe itemInDb
      // Cannot predict id, values may be wrong
      val item = Item(
        id = uuid,
        userId = commonUserId,
        title = title,
        briefDescription = briefDescription,
        description = description,
        price = price,
        created = now,
        updated = now
      )

      createdItem mustBe item

    }
  }

  "get must" - {
    "return none when a record is not found and there are no entries" in {
      itemRepository.get(UUID.randomUUID()).futureValue mustBe None
    }

    "return none when a record is not found and there are many entries but none match" in {
      databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
      databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
      databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue

      itemRepository.get(UUID.randomUUID()).futureValue mustBe None
    }

    "return a record when one is one is found for the id and there are many entries" in {
      // We want to have enough values with enough value spread to guarantee ALL elements of a where clause
      databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
      val expectedItem = databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
      databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue

      itemRepository.get(expectedItem.id).futureValue mustBe Some(expectedItem)
    }
  }

  "remove must" - {
    "return false when the record was not found and there are no entries" in {
      itemRepository.remove(UUID.randomUUID()).futureValue mustBe false
      databaseSetup.item.getAll.futureValue mustBe List.empty
    }

    "return false and not remove any data when a record is not found and there are many entries but none match" in {
      val items = List(
        databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue,
        databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue,
        databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
      )

      // There are things like contain theSameElementsAs so we don't need to sort, but you then need to mentally sort
      // on failure to work it out. Computers are better at sorting than we are and we should try and not leave people
      // headaches.
      itemRepository.remove(UUID.randomUUID()).futureValue mustBe false
      databaseSetup.item.getAll.futureValue mustBe items.sortBy(_.id.toString)
    }

    "return true and only delete the valid record when it is found" in {
      val nonDeletedEntry1 = databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
      val expectedItem = databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
      val nonDeletedEntry2 = databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue

      itemRepository.remove(expectedItem.id).futureValue mustBe true

      databaseSetup.item.getAll.futureValue mustBe List(nonDeletedEntry1, nonDeletedEntry2).sortBy(_.id.toString)
    }
  }

  "search must" - {

    "return" - {
      "no results when there is no data" in {
        val searchTerm = "computers"
        itemRepository
          .search(searchTerm = searchTerm, startOffset = 0, limit = 1)
          .futureValue mustBe SearchResults.empty.copy(
          searchTerm = searchTerm,
          currentResultsRange = (0, 0)
        )
      }

      "no results when there is data" in {
        databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
        databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
        databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue

        val searchTerm = "computers"
        itemRepository
          .search(searchTerm = searchTerm, startOffset = 0, limit = 1)
          .futureValue mustBe SearchResults.empty.copy(
          searchTerm = searchTerm,
          currentResultsRange = (0, 0)
        )
      }

      "all results when they match across title, brief description, description when the offsets are outside the available range" in {
        val items = createData
        val searchTerm = "computers"
        itemRepository
          .search(searchTerm = searchTerm, startOffset = 0, limit = 10)
          .futureValue mustBe SearchResults.empty.copy(
          searchTerm = searchTerm,
          totalResults = 3,
          currentResultsRange = (0, 9),
          results = List(
            // direct access of indexes like this is too unsafe for production code, things like headOption are always better
            SearchResult.fromItem(items(0)),
            SearchResult.fromItem(items(1)),
            SearchResult.fromItem(items(2))
          )
        )
      }

      def createData = {
        databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
        // When testing order of results we should usually not have updated and created the same value if we are testing ordering
        // with them. I am just making my life difficult in this example by having both.
        val item1 =
          databaseSetup.item
            .generate(UUID.randomUUID(), commonUserId, hourOffset = 0, titleFormat = "title computers %s")
            .futureValue

        val item2 = databaseSetup.item
          .generate(
            UUID.randomUUID(),
            commonUserId,
            hourOffset = 1,
            briefDescriptionFormat = "brief description computers %s"
          )
          .futureValue

        val item3 = databaseSetup.item
          .generate(UUID.randomUUID(), commonUserId, hourOffset = 2, descriptionFormat = "description computers %s")
          .futureValue

        databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue
        databaseSetup.item.generate(UUID.randomUUID(), commonUserId).futureValue

        List(item1, item2, item3)

      }

      "all results when they match across title, brief description, description when the offsets are at the boundary of the range" in {
        val items = createData
        val searchTerm = "computers"
        itemRepository
          .search(searchTerm = searchTerm, startOffset = 0, limit = 3)
          .futureValue mustBe SearchResults.empty.copy(
          searchTerm = searchTerm,
          totalResults = 3,
          currentResultsRange = (0, 2),
          results = List(
            SearchResult.fromItem(items(0)),
            SearchResult.fromItem(items(1)),
            SearchResult.fromItem(items(2))
          )
        )
      }

      "a result within the range keeping the total found as total possible" in {
        val items = createData
        val searchTerm = "computers"
        itemRepository
          .search(searchTerm = searchTerm, startOffset = 1, limit = 1)
          .futureValue mustBe SearchResults.empty.copy(
          searchTerm = searchTerm,
          totalResults = 3,
          currentResultsRange = (1, 1),
          results = List(
            SearchResult.fromItem(items(1))
          )
        )
      }

    }
  }
}
