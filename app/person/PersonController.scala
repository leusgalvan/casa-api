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
      maybeData.fold(BadRequest(Json.obj("message" -> s"No person found for id: $id"))) {
        personData => Ok(Json.toJson(personData))
      }
    }.recover {
      case _ => InternalServerError(Json.obj("message" -> "Internal error"))
    }
  }

  def list(): Action[AnyContent] = Action.async { implicit request =>
    personRepository.list().map { people =>
      Ok(Json.toJson(people))
    }.recover {
      case _ => InternalServerError(Json.obj("message" -> "Internal error"))
    }
  }

  def create(): Action[AnyContent] = Action.async { implicit request =>
    form.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      personData => {
        personRepository.create(personData).map { newId =>
          Ok(Json.toJson(personData.copy(id = Some(newId))))
        }.recover {
          case _ => InternalServerError(Json.obj("message" -> "Internal error"))
        }
      }
    )
  }

  def delete(id: Long): Action[AnyContent] = Action.async { implicit request =>
    personRepository.delete(id).map { found =>
      if(found)
        Ok(Json.obj("message" -> "Person deleted successfully"))
      else
        BadRequest(Json.obj("message" -> s"No person found for id: $id"))
    }.recover {
      case _ => InternalServerError(Json.obj("message" -> "Internal error"))
    }
  }

  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(formWithErrors.errorsAsJson))
      },
      personData => {
        val personDataWithId = personData.copy(id = Some(id))
        personRepository.update(personDataWithId).map { found =>
          if(found)
            Ok(Json.toJson(personDataWithId))
          else
            BadRequest(Json.obj("message" -> s"No person found for id: $id"))
        }.recover {
          case _ => InternalServerError(Json.obj("message" -> "Internal error"))
        }
      }
    )
  }
}
