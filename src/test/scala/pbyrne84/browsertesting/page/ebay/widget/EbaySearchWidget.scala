package pbyrne84.browsertesting.page.ebay.widget

import org.openqa.selenium.{By, Keys, WebDriver}
import org.scalactic.source.Position
import pbyrne84.browsertesting.page.ebay.SuperBaySearchResultsPage

class EbaySearchWidget(driver: WebDriver) {
  private val searchBoxId    = "searchText"
  private val searchButtonId = "gh-btn"

  def search(text: String)(implicit position: Position): SuperBaySearchResultsPage = {

    driver.findElement(By.name(searchBoxId)).sendKeys(text)
    driver.findElement(By.name("submitSearch")).submit()

    // submit button had loading/temporal issues. Safer to use enter which is what most people use
    // RetryAble.retry(() =>submitButton.submit())

    // driver.findElement(By.name(searchBoxId)).sendKeys(Keys.RETURN)

    val ebaySearchResultsPage = new SuperBaySearchResultsPage(driver)
    ebaySearchResultsPage
  }
}
