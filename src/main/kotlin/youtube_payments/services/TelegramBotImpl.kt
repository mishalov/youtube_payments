package youtube_payments.services

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId

class TelegramBotImpl constructor(token: String) : TelegramBot {
    val telegramBot = bot {
        dispatch {
            command("start") {
                val result = bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Hi there!")
                result.fold({
                    // do something here with the response
                },{
                    // do something with the error
                })
            }
        }
    }

    init {
        telegramBot =
        bot.startPolling()
    }
}