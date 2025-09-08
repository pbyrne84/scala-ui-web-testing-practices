package pbyrne84.browsertesting

import org.scalamock.scalatest.MockFactory
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Millis, Seconds, Span}

import scala.concurrent.ExecutionContext

abstract class BaseSpec
    extends AnyFreeSpecLike
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfter
    with Matchers
    with MockFactory
    with TestDatabaseSetup {

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(15, Seconds)),
    interval = scaled(Span(1, Millis))
  )

  // Give something for Dan to do :P
  protected implicit val ec: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = runnable.run()

    override def reportFailure(cause: Throwable): Unit = throw cause
  }

  def reset(): Unit = {
    databaseSetup.reset
  }

}
