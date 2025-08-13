package pbyrne84.browsertesting.page.superbay

import pbyrne84.browsertesting.page.ChromeDriverInstance
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.matchers.should.Matchers

object SuperBaySearchResultsPage {

  private val driver = ChromeDriverInstance.driver

  def loadHomePage(port: Int): SuperBaySearchResultsPage = {
    new SuperBaySearchResultsPage(port, driver).loadHomePage
  }
}

class SuperBaySearchResultsPage(port: Int, driver: WebDriver) extends Matchers {

  def loadHomePage: SuperBaySearchResultsPage = {

    driver.get(s"http://localhost:$port")
    // Check we loaded the page, there can be other ways, just a demo
    // We should care about the mechanism in the test as it scatters the responsibilities about
    driver.findElement(By.id("HeaderTitleContainer")).getText shouldBe "SuperBay"
    this
  }

  def resultCount: Int = driver.findElements(By.className("searchResult")).size()
}
