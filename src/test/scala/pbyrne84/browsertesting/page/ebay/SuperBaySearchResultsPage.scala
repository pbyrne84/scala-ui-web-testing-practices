package pbyrne84.browsertesting.page.ebay

import org.openqa.selenium.{By, WebDriver}
import org.scalactic.source.Position
import org.scalatest.matchers.should.Matchers
import pbyrne84.browsertesting.models.SiteTitle
case class SearchResult(title: String, price: String) extends SuperBayPage

class SuperBaySearchResultsPage(protected val driver: WebDriver)(implicit position: Position)
    extends SearchWidgetEbayPage
    with Matchers {

  retry(5) {
    driver.getTitle should endWith(SiteTitle.searchResults)
  }

  // #mainContent > div.s-answer-region.s-answer-region-center-top > div > div.clearfix.srp-controls__row-2 > div:nth-child(1) > div.srp-controls__control.srp-controls__count > h1 > span:nth-child(1)
  // *[@id="mainContent"]/div[1]/div/div[1]/div[1]/div[1]/h1/span[1]
  def totalResults(implicit position: Position): Int = {
    val searchCountHeadingClass = "results-summary"
    // Selenium throws exceptions when it cannot find element.
    // We could make the message nicer, though still letting the person know about what was the selector
    // was actually looking for, these things change and then we need to debug the html
    val selector: By = By.className(searchCountHeadingClass)
    val countText = attempt(
      driver
        .findElement(selector)
        .findElement(By.tagName("strong"))
        .getText
        .replace(",", "")
    ) // it comes out as 40,000 etc.

    countText should fullyMatch regex "\\d+" // check we can cast to int

    countText.toInt
  }

  def clickResult(index: Int)(implicit position: Position): SuperBayItemPage = {
    val searchResultElement = getSearchResultsElement(index)
    attempt {
      val linkElement = searchResultElement.findElement(By.className("searchResultUrl"))
      linkElement.click()
    }

    SuperBayItemPage(driver)
  }

  private def getSearchResultsElement(expectedIndex: Int)(implicit position: Position) = {
    val searchResultElements = attempt {
      val searchResultsElement =
        driver.findElement(By.className("results"))

      searchResultsElement.findElements(By.className("card"))
    }

    val totalSearchResultsOnPage = searchResultElements.size()
    withClue("There should be enough items on the page") {
      expectedIndex should be < totalSearchResultsOnPage
    }

    searchResultElements.get(expectedIndex)

  }

  def getSearchResult(index: Int)(implicit position: Position): SearchResult = {

    val searchResultElement = getSearchResultsElement(index)
    val titleText           = searchResultElement.findElement(By.className("info")).getText
    val priceText           = searchResultElement.findElement(By.className("price")).getText

    val searchResult = SearchResult(titleText, priceText)
    searchResult
  }

}
