package person

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}
import person.Person._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._

trait PersonRepository {
  def create(data: PersonData)(implicit mc: MarkerContext): Future[Long]

  def list()(implicit mc: MarkerContext): Future[Iterable[PersonData]]

  def get(id: Long)(implicit mc: MarkerContext): Future[Option[PersonData]]

  def delete(id: Long)(implicit mc: MarkerContext): Future[Boolean]
}

@Singleton
class PersonRepositoryImpl @Inject()(transactor: Transactor[IO]) extends PersonRepository {
  private val logger = Logger(this.getClass)

  override def list()(implicit mc: MarkerContext): Future[Iterable[PersonData]] = {
    Future {
      sql"select id, name from person"
      .query[PersonData]
      .to[Set]
      .transact(transactor)
      .unsafeRunSync
    }
  }

  override def get(id: Long)(implicit mc: MarkerContext): Future[Option[PersonData]] = {
    Future {
      sql"select id, name from person where id = $id"
      .query[PersonData]
      .option
      .transact(transactor)
      .unsafeRunSync
    }
  }

  override def create(data: PersonData)(implicit mc: MarkerContext): Future[Long] = {
    Future {
      sql"insert into person(name) values (${data.name})"
      .update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(transactor)
      .unsafeRunSync
    }
  }

  override def delete(id: Long)(implicit mc: MarkerContext): Future[Boolean] = {
    Future {
      sql"delete from person where id = $id"
      .update
      .run
      .transact(transactor)
      .unsafeRunSync
    }.map(_ == 1)
  }
}
