package pbyrne84.browsertesting.page.ebay

import pbyrne84.browsertesting.page.ebay.widget.EbaySearchWidget
import org.openqa.selenium.WebDriver

trait EbayPage

trait SearchWidgetEbayPage extends EbayPage {

  protected val driver: WebDriver

  def search(searchText: String): EbaySearchResultsPage = {
    val ebaySearchResultsPage = new EbaySearchWidget(driver).search(searchText)

    ebaySearchResultsPage
  }
}

case object NotLoadedEpagePage extends EbayPage
