package youtube_payments.dao
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction

object AccountDAO : IntIdTable() {
    val email = varchar("email", 50).index()
    val paidUntil = date("paidUntil")
    val paymentConfirmed = bool("paymentConfirmed")
}
