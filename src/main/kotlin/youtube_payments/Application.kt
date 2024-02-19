package youtube_payments

import io.github.cdimascio.dotenv.Dotenv
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import youtube_payments.plugins.*
import youtube_payments.services.*
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import youtube_payments.dao.AccountDAO

val dotenv = dotenv()

val accountsService = AccountsService()

val dbUsername = dotenv["DATABASE_USER"]
val dbPassword = dotenv["DATABASE_PASSWORD"]

fun configureTgService (dotenv: Dotenv) {
    val telegramToken = dotenv["TELEGRAM_TOKEN"]
    val paymentToken = dotenv["PAYMENT_TOKEN"]
    val adminId = dotenv["ADMIN_ID"]

    TelegramService(telegramToken, paymentToken, adminId.toLong(), accountsService)
}
fun main() {
    embeddedServer(Netty, port = 8899, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDatabase(dbUsername, dbPassword, sqliteFileName = "youtube_payments.sqlite")
    configureTgService(dotenv)
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}
