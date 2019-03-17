package person

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._
import Person._
import scala.concurrent.{ ExecutionContext, Future }
import play.api.i18n.I18nSupport

class PersonController @Inject()(
  personRepository: PersonRepository,
  val controllerComponents: ControllerComponents
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  private val logger = Logger(getClass)

  private val form = PersonForm.getForm()

  def get(id: Long): Action[AnyContent] = Action.async { implicit request =>
    personRepository.get(id).map { maybeData =>
      maybeData.fold(BadRequest(Json.toJson(Map("message" -> s"No person found for id: $id")))) {
        personData => Ok(Json.toJson(personData))
      }
    }.recover {
      case _ => InternalServerError(Json.toJson(Map("message" -> "Internal error")))
    }
  }

  def list(): Action[AnyContent] = Action.async { implicit request =>
    personRepository.list().map { people =>
      Ok(Json.toJson(people))
    }.recover {
      case _ => InternalServerError(Json.toJson(Map("message" -> "Internal error")))
    }
  }

  def create(): Action[AnyContent] = Action.async { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      personData => {
        personRepository.create(personData).map { newId =>
          Ok(Json.obj("id" -> newId))
        }.recover {
          case _ => InternalServerError(Json.toJson(Map("message" -> "Internal error")))
        }
      }
    )
  }
}
