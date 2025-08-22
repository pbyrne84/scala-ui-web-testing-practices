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
  def fromItem(item: Item): SearchResult = {
    val url = s"/item/${item.title.replace(" ", "-")}/${item.id}"

    SearchResult(item.id, item.title, item.briefDescription, item.price, url)
  }
}

case class SearchResult(id: UUID, title: String, briefDescription: String, price: BigDecimal, url: String)
