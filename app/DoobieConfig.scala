import doobie._
import doobie.implicits._
import cats.effect._
import scala.concurrent.ExecutionContext.Implicits.global

object DoobieConfig {
  implicit val cs = IO.contextShift(global)
  val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:casa",
    "postgres",
    "admin"
  )
}
