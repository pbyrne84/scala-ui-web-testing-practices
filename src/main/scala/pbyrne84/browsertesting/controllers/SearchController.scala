package pbyrne84.browsertesting.controllers

import pbyrne84.browsertesting.models.{Item, SearchResults}
import pbyrne84.browsertesting.repo.ItemRepository

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class SearchController(itemRepository: ItemRepository, resultsLimit: Int)(implicit val ec: ExecutionContext) {

  def search(searchTerm: String, startOffset: Int): Future[SearchResults] = {
    itemRepository.search(searchTerm, startOffset, resultsLimit)
  }

  def get(id: UUID): Future[Option[Item]] = {
    itemRepository.get(id)
  }

}
