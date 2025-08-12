package pbyrne84.browsertesting.cucumber

import io.cucumber.scala.{EN, ScalaDsl}
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.remote.RemoteWebDriver
import org.scalatest.concurrent.Eventually
import org.scalatest.matchers.must.Matchers

object Driver {

  implicit val instance: RemoteWebDriver = new ChromeDriver()

}

trait BaseStepDef extends ScalaDsl with EN with Eventually with Matchers {
  implicit def driver: RemoteWebDriver = Driver.instance
}
