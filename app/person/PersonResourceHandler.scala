package person

import javax.inject.{Inject, Provider}

import play.api.MarkerContext

import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._

case class PersonResource(id: String, link: String, name: String)

object PersonResource {

  /**
    * Mapping to write a PersonResource out as a JSON value.
    */
  implicit val implicitWrites = new Writes[PersonResource] {
    def writes(person: PersonResource): JsValue = {
      Json.obj(
        "id" -> person.id,
        "link" -> person.link,
        "name" -> person.name
      )
    }
  }
}

class PersonResourceHandler @Inject()(
    routerProvider: Provider[PersonRouter],
    personRepository: PersonRepository)(implicit ec: ExecutionContext) {


  def create(personInput: PersonFormInput)(implicit mc: MarkerContext): Future[PersonResource] = {
    val data = PersonData(PersonId("999"), personInput.name)
    // We don't actually create the person, so return what we have
    personRepository.create(data).map { id =>
      createPersonResource(data)
    }
  }


  def lookup(id: String)(implicit mc: MarkerContext): Future[Option[PersonResource]] = {
    val personFuture = personRepository.get(PersonId(id))
    personFuture.map { maybePersonData =>
      maybePersonData.map { personData =>
        createPersonResource(personData)
      }
    }
  }


  def find(implicit mc: MarkerContext): Future[Iterable[PersonResource]] = {
    personRepository.list().map { personDataList =>
      personDataList.map(personData => createPersonResource(personData))
    }
  }


  private def createPersonResource(p: PersonData): PersonResource = {
    PersonResource(p.id.toString, routerProvider.get.link(p.id), p.name)
  }

}
