package youtube_payments

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import youtube_payments.plugins.*
import youtube_payments.services.*
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import youtube_payments.dao.AccountDAO

val dotenv = dotenv()
val telegramToken = dotenv["TELEGRAM_TOKEN"]
val paymentToken = dotenv["PAYMENT_TOKEN"]
val adminId = dotenv["ADMIN_ID"]
val adminUsername = dotenv["ADMIN_USERNAME"]

val accountsService = AccountsService()
val telegramBot = TelegramBot(telegramToken, paymentToken, adminId.toLong(), accountsService)

val modulesForInjection = module {
    single { telegramBot }
    single  { accountsService}
}

fun main() {
    Database.connect("jdbc:sqlite:youtube_payments.sqlite", driver = "org.sqlite.JDBC", user = "root", password = "")

    transaction {
        SchemaUtils.create(AccountDAO);
    }

    embeddedServer(Netty, port = 8899, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureKoin(modulesForInjection)
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}
