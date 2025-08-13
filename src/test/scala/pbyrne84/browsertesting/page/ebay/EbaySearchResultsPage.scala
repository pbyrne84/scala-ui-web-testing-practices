package pbyrne84.browsertesting.page.ebay

import org.openqa.selenium.support.ui.ExpectedConditions.numberOfWindowsToBe
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest.matchers.must.Matchers

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{Failure, Success, Try}

case class SearchResult(title: String, price: String) extends EbayPage

class EbaySearchResultsPage(protected val driver: WebDriver) extends SearchWidgetEbayPage with Matchers {

  implicit class WebDriverExtension(driver: WebDriver) {

    def findElementWithRetry(by: By): WebElement = {
      runWithRetry(action = () => driver.findElement(by), currentCall = 1, maxCalls = 10)
    }

    @tailrec
    private def runWithRetry[A](action: () => A, currentCall: Int, maxCalls: Int): A = {
      Try(action()) match {
        case Failure(exception) =>
          if (currentCall == maxCalls) {
            Thread.sleep(10000000)
            throw exception
          } else {
            Thread.sleep(1000)
            println(s"retrying - ${driver.getCurrentUrl}")
            runWithRetry(action, currentCall + 1, maxCalls)
          }
        case Success(value) =>
          value
      }
    }
  }

  private val searchCountHeadingClass = "srp-controls__count-heading"
  // Selenium throws exceptions when it cannot find element.
  // We could make the message nicer, though still letting the person know about what was the selector
  // was actually looking for, these things change and then we need to debug the html
  private val selector: By = By.className(searchCountHeadingClass)
  driver.findElement(selector)

  // #mainContent > div.s-answer-region.s-answer-region-center-top > div > div.clearfix.srp-controls__row-2 > div:nth-child(1) > div.srp-controls__control.srp-controls__count > h1 > span:nth-child(1)
  // *[@id="mainContent"]/div[1]/div/div[1]/div[1]/div[1]/h1/span[1]
  def totalResults: Int = {
    val countText = driver
      .findElement(selector)
      .findElement(By.className("BOLD"))
      .getText
      .replace(",", "") // it comes out as 40,000 etc.

    countText must fullyMatch regex "\\d+" // check we can cast to int

    countText.toInt
  }

  def clickResult(index: Int): SearchResult = {
    val searchResultElement = getSearchResultsElement(index)
    // s-item__link

    val linkElement = searchResultElement.findElement(By.className("s-item__link"))
    linkElement.click()

    val originalWindow: String = driver.getWindowHandle
    numberOfWindowsToBe(2)
    switchWindow(originalWindow)

    val searchResultTitle = driver.findElement(By.className("x-item-title__mainTitle")).getText
    val searchResultPrice = driver.findElement(By.className("x-price-primary")).getText

    SearchResult(searchResultTitle, searchResultPrice)
  }

  private def switchWindow(originalWindow: String): Unit = {
    val windowHandles = driver.getWindowHandles.asScala.toList

    windowHandles
      .find { windowHandle =>
        !originalWindow.contentEquals(windowHandle)
      }
      .map { otherWindowName =>
        println(s"switching to window $otherWindowName")
        driver.switchTo.window(otherWindowName)
      }
  }

  private def getSearchResultsElement(expectedIndex: Int) = {
    val searchResultsElement =
      driver.findElement(By.id("srp-river-results"))

    println(searchResultsElement)

    val searchResultElements = searchResultsElement.findElements(By.className("s-item__wrapper"))
    val totalSearchResultsOnPage = searchResultElements.size()
    if (expectedIndex > totalSearchResultsOnPage) {
      fail(
        s"Search result $expectedIndex (starting at 0) not available on page, there are only $totalSearchResultsOnPage results on this page"
      )
    }

    searchResultElements.get(expectedIndex)
  }

  def getSearchResult(index: Int): SearchResult = {

    val searchResultElement = getSearchResultsElement(index)
    val titleText = searchResultElement.findElement(By.className("s-item__title")).getText
    val priceText = searchResultElement.findElement(By.className("s-item__price")).getText

    val searchResult = SearchResult(titleText, priceText)
    searchResult
  }

}
