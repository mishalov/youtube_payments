package youtube_payments.plugins

import io.ktor.server.application.*
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin

fun Application.configureKoin(modules: Module) {
    install(Koin) {
        modules(modules)
    }
}