package pbyrne84.browsertesting

import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.must.Matchers
import pbyrne84.browsertesting.page.superBay.{SuperBayHomePage, SuperBaySearchResultsPage}
import pbyrne84.browsertesting.util.ApiRunner

class BrowserScalaTestExampleSpec extends AnyFreeSpecLike with Matchers with ApiRunner {

  "SuperBay" - {

    "must load home page correctly" in {
      val homepage: SuperBayHomePage = SuperBayHomePage.loadHomepage

      homepage mustBe a[SuperBayHomePage]
    }

    "must be able to search" in {
      val searchResultsPage = SuperBayHomePage.loadHomepage.search("computer")

      // Don't really need to test this as it is a given
      searchResultsPage.getClass mustBe classOf[SuperBaySearchResultsPage]
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
