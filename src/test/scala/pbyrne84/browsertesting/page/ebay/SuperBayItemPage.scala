package pbyrne84.browsertesting.page.ebay

import org.openqa.selenium.{By, WebDriver}
import org.scalactic.source.Position
import org.scalatest.matchers.should.Matchers
import pbyrne84.browsertesting.models.SiteTitle

class SuperBayItemPage(protected val driver: WebDriver)(implicit position: Position)
    extends SearchWidgetEbayPage
    with Matchers {

  retry(5) {
    driver.getTitle should include(SiteTitle.itemPrefix)
  }

  def title(implicit position: Position): String = {
    val titleElement = attempt(driver.findElement(By.className("title")))

    titleElement.getText
  }

  def description(implicit position: Position): String = {
    val descriptionElement = attempt(driver.findElement(By.className("description")))

    descriptionElement.getText
  }

  def price(implicit position: Position): String = {
    val priceElement = attempt(driver.findElement(By.className("price")))

    priceElement.getText
  }
}
