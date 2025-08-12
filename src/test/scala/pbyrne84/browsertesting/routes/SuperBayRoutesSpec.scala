package pbyrne84.browsertesting.routes

import pbyrne84.browsertesting.BaseSpec
import pbyrne84.browsertesting.models.{Item, User}
import pbyrne84.browsertesting.page.superbay.SuperBaySearchResultsPage
import org.scalatest.matchers.must.Matchers

class SuperBayRoutesSpec extends BaseSpec with Matchers {

  private var setupValues: Seq[(User, Seq[Item])] = _
  private val startedServicePort: Int = TestService.startedServerPort

  before {
    setupValues = databaseSetup.generateMany(userCount = 10, itemsPerUser = 10).futureValue
  }

  "super bay" - {

    "home" - {
      "should show all" in {
        val superBaySearchResultsPage = SuperBaySearchResultsPage.loadHomePage(startedServicePort)
        superBaySearchResultsPage.resultCount mustEqual 20
      }

    }
  }
}
