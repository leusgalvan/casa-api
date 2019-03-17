import javax.inject._

import com.google.inject.{AbstractModule, TypeLiteral}
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import person._
import doobie._
import cats.effect._

/**
  * Sets up custom components for Play.
  *
  * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
  */
class Module(environment: Environment, configuration: Configuration)
    extends AbstractModule
    with ScalaModule {

  override def configure() = {
    bind[PersonRepository].to[PersonRepositoryImpl].in[Singleton]
    bind(new TypeLiteral[Transactor[IO]](){}).toInstance(DoobieConfig.transactor)
  }
}
