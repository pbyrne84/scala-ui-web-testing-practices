package pbyrne84.browsertesting.page.ebay

import pbyrne84.browsertesting.page.ChromeDriverInstance
import org.openqa.selenium.{By, WebDriver}
import org.scalatest.matchers.must.Matchers

// I was doing this with amazon but they have capture all over the place
object EbayHomePage {
  private val driver = ChromeDriverInstance.driver

  private val ebayHomePage = new EbayHomePage(driver)

  // When the jvm stops so will the driver

  // By not using static for everything we can create navigation chains so we can navigate a system easily from code
  // One we have page object we should rarely have to look at the html unless it changes/gets redesigned
  def loadHomepage: EbayHomePage = {
    ebayHomePage.loadHomepage
  }

}

class EbayHomePage(protected val driver: WebDriver) extends Matchers with SearchWidgetEbayPage {

  // private def login(userName: String, password: String): AmazonHomePage = {}

  def loadHomepage: EbayHomePage = {
    driver.get("https://www.ebay.co.uk/")
    // Check we loaded the page, there can be other ways, just a demo
    // We should care about the mechanism in the test as it scatters the responsibilities about
    driver.findElement(By.id("gh-ug")).getText mustBe "Hello. Sign in or register"
    this
  }

}
