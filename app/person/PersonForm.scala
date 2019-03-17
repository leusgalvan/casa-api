package person

import play.api.data.Forms._
import play.api.data.Form
import Person._

object PersonForm {
  def getForm(): Form[PersonData] = {
    Form(
      mapping(
        "id" -> optional(longNumber),
        "name" -> nonEmptyText
      )(PersonData.apply)(PersonData.unapply)
    )
  }
}
