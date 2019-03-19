package person

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._
import play.api.MarkerContext
import play.api.libs.json._
import org.scalatest._

import scala.concurrent._
import person.Person._

import scala.concurrent.ExecutionContext.global
import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
import person.PersonRepository.EmptyIdException

class PersonRepositorySpec extends AsyncFlatSpec with BeforeAndAfterEach {
  implicit val cs = IO.contextShift(global)
  lazy val transactor = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql:test",
    "postgres",
    "admin"
  )
  val personRepository = new PersonRepositoryImpl(transactor)

  val allPeople = Set(
    PersonData(Some(1L), "Leo"),
    PersonData(Some(2L), "Masi"),
    PersonData(Some(3L), "Ile"),
    PersonData(Some(4L), "Papu"),
    PersonData(Some(5L), "Nono")
  )
  val allPeopleById = Map(
    1L -> PersonData(Some(1L), "Leo"),
    2L -> PersonData(Some(2L), "Masi"),
    3L -> PersonData(Some(3L), "Ile"),
    4L -> PersonData(Some(4L), "Papu"),
    5L -> PersonData(Some(5L), "Nono")
  )

  override def beforeEach() {
    val upd = sql"create table person(id serial primary key, name text not null)".update.run *>
      sql"insert into person(name) values ('Leo')".update.run *>
      sql"insert into person(name) values ('Masi')".update.run *>
      sql"insert into person(name) values ('Ile')".update.run *>
      sql"insert into person(name) values ('Papu')".update.run *>
      sql"insert into person(name) values ('Nono')".update.run
    upd.transact(transactor).unsafeRunSync
  }

  override def afterEach() {
    sql"drop table person"
    .update
    .run
    .transact(transactor)
    .unsafeRunSync
  }

  "list" should "list all people in the database" in {
    val eventualPeople = personRepository.list().map(_.toSet)
    eventualPeople map {people => assert(people == allPeople)}
  }

  "get" should "return the person with the given id when it exists" in {
    val id = 1L
    val eventualPerson = personRepository.get(id)
    val expectedPerson = Some(PersonData(Some(id), "Leo"))
    eventualPerson map {person => assert(person == expectedPerson)}
  }

  it should "return None when the person with the given id does not exist" in {
    val id = 6L
    val eventualPerson = personRepository.get(id)
    val expectedPerson = None
    eventualPerson map {person => assert(person == expectedPerson)}
  }

  "create" should "add a new person to the person table" in {
    val person = PersonData(None, "Nuevita")
    for {
      newId <- personRepository.create(person)
      expectedNewPerson = person.copy(id = Some(newId))
      people = sql"select id, name from person".query[PersonData].to[Set].transact(transactor).unsafeRunSync
      expectedPeople = allPeople + expectedNewPerson
    } yield assert(people == expectedPeople)
  }

  "delete" should "remove the person with the given id from the person table and return true when it exists" in {
    val id = 1L
    for {
      deleted <- personRepository.delete(id)
      removedPerson = allPeople.find(_.id == Some(id)).get
      people = sql"select id, name from person".query[PersonData].to[Set].transact(transactor).unsafeRunSync
      expectedPeople = allPeople - removedPerson
    } yield assert(deleted && people == expectedPeople)
  }

  it should "leave the person table as it is and return false when no person with the given id exists" in {
    val id = 6L
    for {
      deleted <- personRepository.delete(id)
      people = sql"select id, name from person".query[PersonData].to[Set].transact(transactor).unsafeRunSync
    } yield assert(!deleted && people == allPeople)
  }

  "update" should "replace an existing person with the given person" in {
    val newPersonData = PersonData(Some(1L), "Leus mejorado")
    val oldPersonData = allPeopleById(1L)
    for {
      updated <- personRepository.update(newPersonData)
      people = sql"select id, name from person".query[PersonData].to[Set].transact(transactor).unsafeRunSync
      expectedPeople = allPeople - oldPersonData + newPersonData
    } yield assert(updated && people == expectedPeople)
  }

  it should "leave the person table as it is and return false when no person with the given id exists" in {
    val newPersonData = PersonData(Some(6L), "Leus mejorado")
    for {
      updated <- personRepository.update(newPersonData)
      people = sql"select id, name from person".query[PersonData].to[Set].transact(transactor).unsafeRunSync
    } yield assert(!updated && people == allPeople)
  }

  it should "fail when the given person has no id" in {
    val newPersonData = PersonData(None, "Leus mejorado")
    recoverToSucceededIf[EmptyIdException] {
      personRepository.update(newPersonData)
    }
  }
}
