package pbyrne84.browsertesting.models

import java.util.UUID

class SearchResultsGenerator {

  def generateSearchResults: Seq[SearchResult] = {
    (1 to 10).map { index =>
      SearchResult(
        UUID.randomUUID(),
        s"item $index",
        s"description $index",
        index * 1000
      )
    }
  }

}
