package person

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._
import Person._
import scala.concurrent.{ ExecutionContext, Future }

class PersonController @Inject()(
  personRepository: PersonRepository,
  val controllerComponents: ControllerComponents
  )(implicit ec: ExecutionContext) extends BaseController {

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
    // form.bindFromRequest.fold(
    //   formWithErrors => {
    //     BadRequest(formWithErrors.errors.toString)
    //   },
    //   personData => {
    //     personRepository.get(id).map { maybeData =>
    //       maybeData.fold(BadRequest(formWithErrors.errors.toString)) {
    //         Ok(Json.toJson(personData))
    //       }
    //     }
    //   }
    // )
  }

  def list(): Action[AnyContent] = Action.async { implicit request =>
    personRepository.list().map { people =>
      Ok(Json.toJson(people))
    }.recover {
      case _ => InternalServerError(Json.toJson(Map("message" -> "Internal error")))
    }
  }

  def create(): Action[AnyContent] = Action.async { implicit request =>
    Future {
      Ok(Json.toJson(Map("lala" -> "lala")))
    }
  }
}
