package youtube_payments.utils

import kotlin.test.Test
import youtube_payments.dto.AccountDTO
import java.time.LocalDate
import kotlin.test.assertEquals
import youtube_payments.utils.listOfUnconfirmed
import youtube_payments.utils.listOfActive


val acc1 = AccountDTO(
    email = "account1@mail.com",
    paidUntil = LocalDate.of(2024, 1,1),
    paymentConfirmed = false
)

val acc2 = AccountDTO(
    email = "account2@mail.com",
    paidUntil = LocalDate.of(2024, 1,2),
    paymentConfirmed = false
)

class UtilsTest {
    @Test
    fun testHumanFormatDate() {
        val expected = "2024-01-01"
        assertEquals(expected, prettyDateFormat(LocalDate.of(2024, 1,1)))
    }

    @Test
    fun listOfUnconfirmed(){
        val expected = "List of accounts, which was paid but not added to Family List yet: \n" +
                "\n" +
                "• account1@mail.com, valid until 2024-01-01, \n" +
                "• account2@mail.com, valid until 2024-01-02";
        assertEquals(expected, listOfUnconfirmed(listOf(acc1, acc2)))
    }

    @Test
    fun listOfActive(){
        val expected = "List of accounts which are in family list currently: \n" +
                "\n" +
                "• account1@mail.com, valid until 2024-01-01, \n" +
                "• account2@mail.com, valid until 2024-01-02"

        assertEquals(expected, listOfActive(listOf(acc1, acc2)))
    }
}