# UI Testing with Selenium

If JavaScript is being used, then we need to run an actual browser to test those features within a page. If you are doing
any front end work, you can use the tests/page objects to actually repeatedly get you where you are working. We should NOT
be doing this manually over and over, unless we like wasting a lot of time and money. 

## Page Objects

So designs can change, in fact a design change can make customers way more happy than new features, and design is also 
something customers can also have a lot of input on, this gives them a sense of control. Page objects help to make this 
easy as we can just change the selectors within them when this happens, for basic redesigns anyway. For more complicated
ones they also help a lot as we can redesign them and then just update our tests to use them differently easily.

We should get to the position we never have to look at the front end, unless we are actually verifying things like the 
design.


### Anatomy of a Page Object

1. The selectors are held within
2. When doing an operation, like search, we return the new page so we can do a fluent interface that should self-validate
   and fail coherently. Tests that do not fail coherently are not friendly tests. This allows us to do things like 

```scala
 SuperBayHomePage.loadHomepage
  .search("computer")
  .getSearchResult(0)
```
You can see how this can help with complicated journeys and the fact we may need to repeat over and over those steps to
get to the point we want to get to. We can add a Thread.sleep() to actually stall the browser at that point and just simply
work normally on that page. Else we can spend days clicking on this needlessly, not achieving much, and not getting negative
satisfaction from not achieving much.


#### Selectors
These can be ID of elements, or CSS Style selectors, etc. Though ID is usually more tolerant to style change as styles
may need to be combined to create a single reference point, and when there is a redesign all those styles change.

#### Example page object

Originally this drove Ebay, but having a project like this tied to someone else's design would mean the project would break
very easily. In fact, it did as Ebay did a redesign of some degree.

This implementation is using Scalatest, and **position: Position** is passed in silently, so if an assertion fails, we get
the line number the test failed on. We can still navigate down, which is a single path, whereas if it fails within,
there can be many paths to that call. If a test is failing, then it makes things more friendly to easily being able to 
navigate to it.

```scala
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
```


