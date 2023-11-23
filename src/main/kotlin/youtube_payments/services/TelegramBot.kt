package youtube_payments.services

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.preCheckoutQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.payments.LabeledPrice
import com.github.kotlintelegrambot.entities.payments.PaymentInvoiceInfo
import com.github.kotlintelegrambot.extensions.filters.Filter
import youtube_payments.utils.humanFormatDate
import youtube_payments.utils.listOfUnconfirmed
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern


const val WELCOME_MESSAGE = "Hello, welcome to my service for youtube family payment. Please provide us Your email" +
        ". Dont forget to adjust PHYSICAL address in your googloe account: Slavkovska 865/11, 627 00, BRNO"

const val INVOICE_TITLE = "YouTube premium"
const val INVOICE_DESCRIPTION = "Payment for 1 month Youtube premium"

class TelegramBot constructor(telegramToken: String, paymentToken: String, adminId: Long, accountsService: IAccountsService) {
    private fun validateEmail (text: String): Boolean {
        val pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\$")
        val matcher = pattern.matcher(text)

        return matcher.matches()
    }


    private val telegramBot = bot {
        token = telegramToken

        dispatch {
            command("start") {
                val result = bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = WELCOME_MESSAGE)
                result.fold({

                },{
                    // do something with the error
                })
            }

            command("my") {
                if (args.isEmpty() || !validateEmail(args[0])) {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), "Please add valid email to the command: ```/my a@gmail.com```, like this", parseMode = ParseMode.MARKDOWN_V2)
                    return@command
                }
                val email = args[0]
                val account = accountsService.getSingleSubscription(email);

                if(account == null) {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), "You dont have subscription yet")
                    return@command
                }

                val value: String = humanFormatDate(account.paidUntil)

                bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Hello, $email. Your subscription is valid until $value. Confirmation status: ${account.paymentConfirmed}. " +
                            "If confirmation status is false, just wait a little bit (5mins - 2hours). In case of any problems message to admin: @mieCreative"
                )
            }

            command("confirm") {
                if (message.from ?.id != adminId) {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), "You dont have permissions to do that")
                    return@command
                }

                if (args.isEmpty()) {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), listOfUnconfirmed(accountsService.getNonConfirmedPayments()))
                    return@command
                }

                val email = args[0]

                val result = accountsService.confirmPayment(email)
                bot.sendMessage(chatId = ChatId.fromId(message.chat.id), "Confirmed payment for ${result.email}. Valid until ${humanFormatDate(result.paidUntil)}")
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
                            prices = listOf(
                                LabeledPrice(amount = 8000.toBigInteger(), label = "For 1 month"),
                                LabeledPrice(amount = 22000.toBigInteger(), label = "For 3 month"),
                                LabeledPrice(amount = 52000.toBigInteger(), label = "For 6 month"),
                                ),
                            providerToken = paymentToken,
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
                    val email = successfulPayment.invoicePayload;
                    val result = accountsService.updateSubscription(email, 1);

                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text="Your email : $email, will be added to Youtube Family list. Your subscription expires at ${humanFormatDate(result.paidUntil) }")
                    bot.sendMessage(chatId = ChatId.fromId(adminId), "New Payment received! Email: ${result.email}")
                }
            }
        }
    }

    init {
        telegramBot.startPolling()
    }
}