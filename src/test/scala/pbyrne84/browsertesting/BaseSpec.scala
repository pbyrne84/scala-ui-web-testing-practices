package pbyrne84.browsertesting

import com.typesafe.config.{Config, ConfigFactory}
import pbyrne84.browsertesting.models.DbConfig
import pbyrne84.browsertesting.setup.DatabaseSetup
import org.scalactic.Prettifier
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, EitherValues, OptionValues}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatest.matchers.must.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContext

abstract class BaseSpec
    extends AnyFreeSpecLike
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfter
    with Matchers
    with OptionValues
    with EitherValues
    with MockFactory
    with TestDatabaseSetup {

  implicit override val patienceConfig: PatienceConfig = PatienceConfig(
    timeout = scaled(Span(15, Seconds)),
    interval = scaled(Span(1, Millis))
  )

  // make diffs prettier
  implicit val prettifier: Prettifier = Prettifiers.prettifier

  // Give something for Dan to do :P
  protected implicit val ec: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = runnable.run()

    override def reportFailure(cause: Throwable): Unit = throw cause
  }

  def reset(): Unit = {
    databaseSetup.reset
  }

}
