package person

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

case class PersonFormInput(name: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class PersonController @Inject()(cc: PersonControllerComponents)(implicit ec: ExecutionContext)
  extends PersonBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[PersonFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "name" -> nonEmptyText
      )(PersonFormInput.apply)(PersonFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = PersonAction.async { implicit request =>
    logger.trace("index: ")
    personResourceHandler.find.map { people =>
      Ok(Json.toJson(people))
    }
  }

  def show(id: String): Action[AnyContent] = PersonAction.async { implicit request =>
    logger.trace(s"show: id = $id")
    personResourceHandler.lookup(id).map { person =>
      Ok(Json.toJson(person))
    }
  }

  def process: Action[AnyContent] = PersonAction.async { implicit request =>
    logger.trace("process: ")
    processJsonPerson()
  }

  private def processJsonPerson[A]()(implicit request: PersonRequest[A]): Future[Result] = {
    def failure(badForm: Form[PersonFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: PersonFormInput) = {
      personResourceHandler.create(input).map { person =>
        Created(Json.toJson(person)).withHeaders(LOCATION -> person.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
