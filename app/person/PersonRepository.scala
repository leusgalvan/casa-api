package person

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class PersonData(id: PersonId, name: String)

class PersonId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object PersonId {
  def apply(raw: String): PersonId = {
    require(raw != null)
    new PersonId(Integer.parseInt(raw))
  }
}


class PersonExecutionContext @Inject()(actorSystem: ActorSystem) extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the PersonRepository.
  */
trait PersonRepository {
  def create(data: PersonData)(implicit mc: MarkerContext): Future[PersonId]

  def list()(implicit mc: MarkerContext): Future[Iterable[PersonData]]

  def get(id: PersonId)(implicit mc: MarkerContext): Future[Option[PersonData]]
}

/**
  * A trivial implementation for the Person Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class PersonRepositoryImpl @Inject()()(implicit ec: PersonExecutionContext) extends PersonRepository {

  private val logger = Logger(this.getClass)

  private val personList = List(
    PersonData(PersonId("1"), "Leus"),
    PersonData(PersonId("2"), "Ile"),
    PersonData(PersonId("3"), "Masi"),
    PersonData(PersonId("4"), "Papu"),
    PersonData(PersonId("5"), "Nono")
  )

  override def list()(implicit mc: MarkerContext): Future[Iterable[PersonData]] = {
    Future {
      logger.trace(s"list: ")
      personList
    }
  }

  override def get(id: PersonId)(implicit mc: MarkerContext): Future[Option[PersonData]] = {
    Future {
      logger.trace(s"get: id = $id")
      personList.find(person => person.id == id)
    }
  }

  def create(data: PersonData)(implicit mc: MarkerContext): Future[PersonId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
