package person

import org.scalatestplus.play._
import org.scalatest.mockito.MockitoSugar
import play.api.test._
import play.api.test.Helpers._
import play.api.MarkerContext
import play.api.libs.json._
import org.scalatest._
import scala.concurrent._
import person.Person._
import org.mockito.Mockito._
import scala.concurrent.ExecutionContext.Implicits.global

class PersonControllerUnitSpec extends PlaySpec with MockitoSugar {

  "PersonController get with id" should {
    "return the person with the given id when it exists" in {
      val id = 1L
      val personName = "Test person"
      val person = PersonData(Some(id), personName)
      val personRepository = mock[PersonRepository]
      when(personRepository.get(id)) thenReturn Future(Some(person))

      val controller = new PersonController(personRepository, stubControllerComponents())
      val response = controller.get(id).apply(FakeRequest(GET, s"/people/$id/"))
      val json = Json.parse(contentAsString(response))
      val expectedJson = Json.obj("id" -> id, "name" -> personName)

      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      json mustBe expectedJson
    }

    "return a bad request error when there is no person with that id" in {
      val id = 1L
      val personRepository = mock[PersonRepository]
      when(personRepository.get(id)) thenReturn Future(None)

      val controller = new PersonController(personRepository, stubControllerComponents())
      val response = controller.get(id).apply(FakeRequest(GET, s"/people/$id/"))

      status(response) mustBe BAD_REQUEST
    }

    "return an internal server error when the repository fails to fetch the person" in {
      val id = 1L
      val personRepository = mock[PersonRepository]
      when(personRepository.get(id)) thenReturn Future.failed(new Exception("test"))

      val controller = new PersonController(personRepository, stubControllerComponents())
      val response = controller.get(id).apply(FakeRequest(GET, s"/people/$id/"))

      status(response) mustBe INTERNAL_SERVER_ERROR
    }
  }

  "PersonController list" should {
    "return all the people in the database" in {
      val person1 = PersonData(Some(1L), "Test person 1")
      val person2 = PersonData(Some(2L), "Test person 2")
      val people = Set(person1, person2)

      val personRepository = mock[PersonRepository]
      when(personRepository.list()) thenReturn Future(people)

      val controller = new PersonController(personRepository, stubControllerComponents())
      val response = controller.list().apply(FakeRequest(GET, s"/people/"))
      val json = Json.parse(contentAsString(response))
      val expectedJson = Json.arr(
        Json.obj("id" -> 1L, "name" -> "Test person 1"),
        Json.obj("id" -> 2L, "name" -> "Test person 2")
      )

      status(response) mustBe OK
      contentType(response) mustBe Some("application/json")
      json mustBe expectedJson
    }

    "return an internal server error when the repository fails to fetch the people" in {
      val id = 1L
      val personRepository = mock[PersonRepository]
      when(personRepository.list()) thenReturn Future.failed(new Exception("test"))

      val controller = new PersonController(personRepository, stubControllerComponents())
      val response = controller.list().apply(FakeRequest(GET, s"/people/"))

      status(response) mustBe INTERNAL_SERVER_ERROR
    }
  }
}
