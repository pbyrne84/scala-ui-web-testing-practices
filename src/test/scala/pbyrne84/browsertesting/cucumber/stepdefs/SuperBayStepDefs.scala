package pbyrne84.browsertesting.cucumber.stepdefs

import pbyrne84.browsertesting.cucumber.BaseStepDef
import pbyrne84.browsertesting.page.*
import pbyrne84.browsertesting.page.superBay.*

import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

class SuperBayStepDefs extends BaseStepDef {

  implicit class SuperPageOps(currentPage: SuperBayPage) {

    // Just give a normal scalatest error format instead of a thrown exception format
    def asExpected[A: ClassTag]: A = {
      Try(currentPage.asInstanceOf[A]) match {
        case Failure(exception) =>
          fail(exception.getMessage)

        case Success(value) => value
      }
    }
  }

  // As each step is discombobulated we need to store state between them in mutable vars (ick!)
  private var currentPage: SuperBayPage = NotLoadedPage

  // Purposely not in order as trying to share steps leads not being able to read the step linearly.
  // There can be thousands of them making it very very hard to organise them in a way that does not cause a lot of cognitive load,
  // if you care about that sort ot thing.
  Then("""I should have some results""") { () =>
    currentPage.asExpected[SuperBaySearchResultsPage].totalResults >= 0
  }

  Given("""I load the homepage""") { () =>
    currentPage = SuperBayHomePage.loadHomepage
  }

  When("""I search for {string}""") { (string: String) =>
    currentPage = currentPage.asExpected[SearchWidgetSuperBayPage].search(string)
  }

  Then("""I must be on the homepage""") { () =>
    currentPage mustBe a[SuperBayHomePage]
  }

  When("""I click on the first result""") { () =>
    currentPage = currentPage.asExpected[SuperBaySearchResultsPage].clickResult(0)
  }
  Then("""I should see the first result""") { () =>
    val searchResult = currentPage.asExpected[SearchResult]

    searchResult.title.length must be >= 0
    searchResult.price.length must be >= 0
  }

}
