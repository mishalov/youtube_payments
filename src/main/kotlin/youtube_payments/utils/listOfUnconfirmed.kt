package youtube_payments.utils

import youtube_payments.dto.AccountDTO

fun listOfUnconfirmed(accounts: List<AccountDTO>): String {
    return "List of accounts, which was paid but not added to Family List yet: \n" + accounts.map { "\n" +
            "• ${it.email}, valid until ${humanFormatDate(it.paidUntil)}" }.joinToString()
}