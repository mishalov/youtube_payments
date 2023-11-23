package youtube_payments.dto

import java.time.LocalDate

data class AccountDTO(val email: String, val paidUntil: LocalDate, val paymentConfirmed: Boolean)