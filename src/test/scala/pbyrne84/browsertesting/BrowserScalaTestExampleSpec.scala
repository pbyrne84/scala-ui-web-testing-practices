package pbyrne84.browsertesting

import pbyrne84.browsertesting.page.ebay.{EbayHomePage, EbaySearchResultsPage}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.must.Matchers

class BrowserScalaTestExampleSpec extends AnyFreeSpecLike with Matchers {

  "Ebay" - {

    "must load home page correctly" in {
      EbayHomePage.loadHomepage mustBe a[EbayHomePage]
    }

    "must be able to search" in {
      val searchResultsPage = EbayHomePage.loadHomepage.search("computer")

      // Don't really need to test this as it is a given
      searchResultsPage mustBe an[EbaySearchResultsPage]
      searchResultsPage.totalResults must be >= 0

      val firstSearchResult = searchResultsPage.getSearchResult(0)

      // Usually we would use canned data so we can check things like actual title
      // And more importantly we have a stable search order for pagination, else things get lost in the boundaries
      // going page to page.Usually it is worth having a secondary search field such as id to stop collisions on
      // things like search score causing things to bounce. Very annoying to deal with that
      firstSearchResult.title.length must be >= 0

      val searchResult = searchResultsPage.clickResult(0)

      // In real practice this would be bad as we should know the title etc
      searchResult.title.length must be >= 0
      searchResult.price.length must be >= 0

    }

  }
}
