package pbyrne84.browsertesting.page

import org.openqa.selenium.chrome.ChromeDriver

object ChromeDriverInstance {
  lazy val driver: ChromeDriver = {

    val instance = new ChromeDriver()

    // instance.manage.timeouts.implicitlyWait(Duration.ofSeconds(100))
    scala.sys.addShutdownHook {
      instance.quit()
    }

    instance
  }

}
