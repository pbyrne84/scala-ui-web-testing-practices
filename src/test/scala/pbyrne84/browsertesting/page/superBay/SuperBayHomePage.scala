package pbyrne84.browsertesting.page.superBay

import org.openqa.selenium.WebDriver
import org.scalactic.source.Position
import org.scalatest.matchers.should.Matchers
import pbyrne84.browsertesting.models.SiteTitle
import pbyrne84.browsertesting.page.ChromeDriverInstance
import pbyrne84.browsertesting.routes.SiteUrls

// I was doing this with amazon but they have capture all over the place
object SuperBayHomePage {
  private val driver = ChromeDriverInstance.driver

  private val superBayHomePage = new SuperBayHomePage(driver, PageObjectConfig.siteUrls)

  // When the jvm stops so will the driver

  // By not using static for everything we can create navigation chains so we can navigate a system easily from code
  // One we have page object we should rarely have to look at the html unless it changes/gets redesigned
  def loadHomepage(implicit position: Position): SuperBayHomePage = {
    superBayHomePage.loadHomepage
  }

}

class SuperBayHomePage(protected val driver: WebDriver, siteUrl: SiteUrls)
    extends Matchers
    with SearchWidgetSuperBayPage {

  // private def login(userName: String, password: String): AmazonHomePage = {}
  println(siteUrl.fullPath.homePage)

  def loadHomepage(implicit position: Position): SuperBayHomePage = {
    driver.get(siteUrl.fullPath.homePage)

    // Check we loaded the page, there can be other ways, just a demo
    // We should care about the mechanism in the test as it scatters the responsibilities about
    driver.getTitle should endWith(SiteTitle.homePage)
    this
  }

}
