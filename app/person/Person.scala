package person

import play.api.libs.json._

object Person {
  final case class PersonData(id: Option[Long], name: String)
  implicit val implicitWrites = new Writes[PersonData] {
    def writes(person: PersonData): JsValue = {
      Json.obj(
        "id" -> person.id,
        "name" -> person.name
      )
    }
  }
}
