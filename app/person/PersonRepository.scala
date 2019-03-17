package person

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}
import person.Person._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait PersonRepository {
  def create(data: PersonData)(implicit mc: MarkerContext): Future[Long]

  def list()(implicit mc: MarkerContext): Future[Iterable[PersonData]]

  def get(id: Long)(implicit mc: MarkerContext): Future[Option[PersonData]]
}

@Singleton
class PersonRepositoryImpl extends PersonRepository {

  private val logger = Logger(this.getClass)

  private val personList = List(
    PersonData(Some(1), "Leus"),
    PersonData(Some(2), "Ile"),
    PersonData(Some(3), "Masi"),
    PersonData(Some(4), "Papu"),
    PersonData(Some(5), "Nono")
  )

  var lastId: Long = 5
  def nextId(): Long = {
    lastId += 1
    lastId
  }
  override def list()(implicit mc: MarkerContext): Future[Iterable[PersonData]] = {
    Future {
      logger.trace(s"list: ")
      personList
    }
  }

  override def get(id: Long)(implicit mc: MarkerContext): Future[Option[PersonData]] = {
    Future {
      logger.trace(s"get: id = $id")
      personList.find(person => person.id == Some(id))
    }
  }

  def create(data: PersonData)(implicit mc: MarkerContext): Future[Long] = {
    Future {
      logger.info(s"create: data = $data")
      nextId()
    }
  }

}
