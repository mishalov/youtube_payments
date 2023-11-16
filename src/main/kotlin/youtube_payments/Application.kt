package youtube_payments

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import youtube_payments.plugins.*
import youtube_payments.services.*

fun main() {
    val bot = TelegramBotImpl("6912718321:AAGGxb7BsovgM1PcqTCgo_l-zuD21geE1CE")

    embeddedServer(Netty, port = 8899, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}
