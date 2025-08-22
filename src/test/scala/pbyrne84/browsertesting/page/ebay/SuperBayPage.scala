package pbyrne84.browsertesting.page.ebay

import org.openqa.selenium.WebDriver
import org.scalactic.source.Position
import pbyrne84.browsertesting.page.ebay.widget.EbaySearchWidget

import scala.util.{Failure, Success, Try}

trait SuperBayPage {

  def attempt[A](call: => A)(implicit position: Position): A = {
    import org.scalatest.matchers.should.Matchers.*
    val triedA = Try { call }

    triedA match {
      case Failure(exception) =>
        Some(exception) shouldBe None
        // hack as fail does not point you to where things were called as there is no implicit position passed
        fail(exception)
      case Success(value) => value
    }

  }

  def retry[A](maxAttempts: Int, currentAttempt: Int = 0)(call: => A)(implicit position: Position): A = {
    Try {
      call
    } match {
      case Failure(exception) =>
        if (currentAttempt == maxAttempts) {
          throw exception
        }
        Thread.sleep(1000)
        retry(maxAttempts, currentAttempt + 1)(call)
      case Success(value) =>
        value
    }
  }
}

trait SearchWidgetEbayPage extends SuperBayPage {

  protected val driver: WebDriver

  def search(searchText: String)(implicit position: Position): SuperBaySearchResultsPage = {
    val ebaySearchResultsPage = new EbaySearchWidget(driver).search(searchText)

    ebaySearchResultsPage
  }
}

case object NotLoadedPage extends SuperBayPage
