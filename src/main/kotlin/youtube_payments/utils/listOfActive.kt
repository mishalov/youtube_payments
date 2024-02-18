package youtube_payments.utils

import youtube_payments.dto.AccountDTO

fun listOfActive(accounts: List<AccountDTO>): String {
    return "List of accounts which are in family list currently: \n" + accounts.map { "\n" +
            "• ${it.email}, valid until ${humanFormatDate(it.paidUntil)}" }.joinToString()
}