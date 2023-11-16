package youtube_payments.services

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.preCheckoutQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.payments.LabeledPrice
import com.github.kotlintelegrambot.entities.payments.PaymentInvoiceInfo
import com.github.kotlintelegrambot.extensions.filters.Filter
import java.util.regex.Pattern


const val WELCOME_MESSAGE = "Hello, welcome to my service for youtube family payment. Please provide us Your email" +
        ". Dont forget to adjust PHYSICAL address in your googloe account: Slavkovska 865/11, 627 00, BRNO"

const val INVOICE_TITLE = "YouTube premium"
const val INVOICE_DESCRIPTION = "Payment for 1 month Youtube premium"

class TelegramBot constructor(telegramToken: String) {
    private fun validateEmail (text: String): Boolean {
        val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
        val matcher = pattern.matcher(text)

        return matcher.matches()
    }

    private val telegramBot = bot {
        token=telegramToken
        dispatch {
            command("start") {
                val result = bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = WELCOME_MESSAGE)
                result.fold({

                },{
                    // do something with the error
                })
            }

            message(Filter.Text) {
                val email = message.text?: ""
                if (validateEmail(email)) {
                    val result = bot.sendInvoice(
                        chatId = ChatId.fromId(message.chat.id),
                        paymentInvoiceInfo = PaymentInvoiceInfo(
                            title = INVOICE_TITLE,
                            description = INVOICE_DESCRIPTION,
                            currency = "CZK",
                            prices = listOf(LabeledPrice(amount = 7000.toBigInteger(), label = "For 1 month")),
                            providerToken = "284685063:TEST:OWRlZDBiYzViY2Ix",
                            payload = email,
                            startParameter = ""
                        )
                    )

                    result.fold({
                        println("Success")
                    }, {
                        println(it)
                    })
                }
            }

            preCheckoutQuery {
                val from = preCheckoutQuery.from
                val invoicePayload = preCheckoutQuery.invoicePayload

                if (!validateEmail(invoicePayload)) {
                    bot.sendMessage(chatId = ChatId.fromId(update.message!!.chat.id), text="Can not read email from invoicePayload.")
                    return@preCheckoutQuery
                }

                bot.answerPreCheckoutQuery(ok=true, preCheckoutQueryId = preCheckoutQuery.id)
            }

            message() {
                val successfulPayment = message.successfulPayment;
                if (successfulPayment != null){
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text="Your email : " +successfulPayment.invoicePayload + ", will be added to Youtube Family list")
                }
            }
        }
    }

    init {
        telegramBot.startPolling()
    }
}