package pbyrne84.browsertesting.services

import pbyrne84.browsertesting.models.{Item, SearchResults, SearchResultsGenerator}

import java.time.Instant
import java.util.UUID
import scala.concurrent.Future

class SearchService {

  def search(searchTerm: String): Future[SearchResults] = {
    Future.successful {
      val searchResultsGenerator = new SearchResultsGenerator()
      SearchResults(
        searchTerm = searchTerm,
        totalResults = 10,
        currentResultsRange = (1, 10),
        results = searchResultsGenerator.generateSearchResults
      )
    }
  }

  def show(id: UUID): Future[Item] = {
    Future.successful {
      val briefDescription = "Pussy cats are the best!"
      val description      = (1 to 100).map(_ => "Pussy cats are the best!").mkString("\n")

      Item(
        id = id,
        userId = id,
        title = s"title $id",
        briefDescription = briefDescription,
        description = description,
        price = (1000),
        created = Instant.now,
        updated = Instant.now
      )
    }
  }
}
