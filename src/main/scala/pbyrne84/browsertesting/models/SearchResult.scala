package pbyrne84.browsertesting.models

import java.util.UUID

object SearchResults {
  val empty: SearchResults = SearchResults("", 0, (0, 0), List.empty)
}

case class SearchResults(
    searchTerm: String,
    totalResults: Int,
    currentResultsRange: (Int, Int),
    results: Seq[SearchResult]
)

object SearchResult {
  def fromItem(item: Item): SearchResult =
    SearchResult(item.id, item.title, item.briefDescription, item.price)
}

case class SearchResult(id: UUID, title: String, briefDescription: String, price: Int)
