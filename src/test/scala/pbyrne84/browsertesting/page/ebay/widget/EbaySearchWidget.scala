package pbyrne84.browsertesting.page.ebay.widget

import pbyrne84.browsertesting.page.ebay.EbaySearchResultsPage
import org.openqa.selenium.{By, Keys, WebDriver}

class EbaySearchWidget(driver: WebDriver) {
  private val searchBoxId = "gh-ac"
  private val searchButtonId = "gh-btn"

  def search(text: String): EbaySearchResultsPage = {

    driver.findElement(By.id(searchBoxId)).sendKeys(text)
    // submit button had loading/temporal issues. Safer to use enter which is what most people use
    // RetryAble.retry(() =>submitButton.submit())

    driver.findElement(By.id(searchBoxId)).sendKeys(Keys.RETURN)


    val ebaySearchResultsPage = new EbaySearchResultsPage(driver)
    ebaySearchResultsPage
  }
}
