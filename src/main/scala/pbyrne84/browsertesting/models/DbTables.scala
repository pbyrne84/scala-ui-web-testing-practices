package pbyrne84.browsertesting.models

import com.github.tminglei.slickpg.*
import slick.ast.FieldSymbol
import slick.lifted.ProvenShape

import java.sql.{PreparedStatement, ResultSet}
import java.time.Instant
import java.util.UUID

trait PostgresProfile extends ExPostgresProfile {
  override val columnTypes = new CustomJdbcTypes

  class CustomJdbcTypes extends PostgresJdbcTypes {
    override val uuidJdbcType = new PostgresUUIDJdbcType {
      override def sqlTypeName(sym: Option[FieldSymbol]) = "UUID"

      override def valueToSQLLiteral(value: UUID) = "'" + value + "'"

      override def hasLiteralForm = true

      override def setValue(v: UUID, p: PreparedStatement, idx: Int) = p.setString(idx, toString(v))

      override def getValue(r: ResultSet, idx: Int) = fromString(r.getString(idx))

      override def updateValue(v: UUID, r: ResultSet, idx: Int) = r.updateString(idx, toString(v))

      private def toString(uuid: UUID) = if (uuid != null) uuid.toString else null

      private def fromString(uuidString: String) = if (uuidString != null) UUID.fromString(uuidString) else null
    }

  }
}

object PostgresProfile extends PostgresProfile

class DbTables(val profile: PostgresProfile) {

  import com.github.tminglei.slickpg.*

  // Just here to keep the import above from going missing as it is not detected as used.
  private val value: `[_,_)`.type = `[_,_)`
  import profile.api.*

  // implicit val uuidToString = MappedColumnType.base[UUID, String](_.toString, UUID.fromString)
  // Pluralising table names can make Database people cry
  // https://stackoverflow.com/questions/338156/table-naming-dilemma-singular-vs-plural-names to name Sock Drawer or Socks Drawer
  class ItemTable(tag: Tag) extends Table[Item](tag, "item") {
    def id: Rep[UUID]                 = column[UUID]("id", O.PrimaryKey)
    def userId: Rep[UUID]             = column[UUID]("user_id")
    def title: Rep[String]            = column[String]("title")
    def briefDescription: Rep[String] = column[String]("brief_description")
    def description: Rep[String]      = column[String]("description")
    def price: Rep[Int]               = column[Int]("price")
    def created: Rep[Instant]         = column[Instant]("created")
    def updated: Rep[Instant]         = column[Instant]("updated")
    def * = (id, userId, title, briefDescription, description, price, created, updated).mapTo[Item]
  }

  class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id: Rep[UUID]         = column[UUID]("id", O.PrimaryKey)
    def name: Rep[String]     = column[String]("name")
    def created: Rep[Instant] = column[Instant]("created")
    def updated: Rep[Instant] = column[Instant]("updated")

    override def * : ProvenShape[User] = (id, name, created, updated).mapTo[User]
  }

  class UserOrderTable(tag: Tag) extends Table[UserOrder](tag, "user_order") {
    def id: Rep[UUID]         = column[UUID]("id", O.PrimaryKey)
    def userId: Rep[UUID]     = column[UUID]("user_id")
    def created: Rep[Instant] = column[Instant]("created")

    override def * : ProvenShape[UserOrder] = (id, userId, created).mapTo[UserOrder]
  }

  class UserOrderItemTable(tag: Tag) extends Table[UserOrderItem](tag, "user_order_item") {
    def orderId: Rep[UUID] = column[UUID]("order_id")
    def itemId: Rep[UUID]  = column[UUID]("item_id")

    override def * : ProvenShape[UserOrderItem] = (orderId, itemId).mapTo[UserOrderItem]
  }

  lazy val itemTable          = TableQuery[ItemTable]
  lazy val userTable          = TableQuery[UserTable]
  lazy val userOrderTable     = TableQuery[UserOrderTable]
  lazy val userOrderItemTable = TableQuery[UserOrderItemTable]

}

object Tables extends DbTables(PostgresProfile)
