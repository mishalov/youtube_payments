package youtube_payments.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun humanFormatDate(localDate: LocalDate): String {
    val formatter = DateTimeFormatter.ISO_DATE

    return localDate.format(formatter)
}