package youtube_payments.plugins

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import youtube_payments.dao.AccountDAO


fun Application.configureDatabase(dbUsername: String, dbPassword: String, sqliteFileName: String) {
    Database.connect("jdbc:sqlite:"+sqliteFileName, driver = "org.sqlite.JDBC", user = dbUsername, password = dbPassword)

    transaction {
        SchemaUtils.create(AccountDAO);
    }
}
