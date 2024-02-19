package youtube_payments.services

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import youtube_payments.plugins.configureDatabase
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.*

val email = "a@a.com";

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountsServiceTest {
    private val accountsService = AccountsService();

    @BeforeAll
    fun beforeAll() = testApplication {
        application {
            configureDatabase(dbUsername="root", dbPassword="", sqliteFileName = "youtube_payments.test.sqlite")
        }
    }

    @BeforeEach
    fun beforeEach(){
        accountsService.removeAll()
    }

    @Test
    fun addAndRemoveAccount(){
        assertEquals(0,  accountsService.getAllSubscriptions().size)

        val accountDTO = accountsService.addAccount(email);

        assertEquals(1,  accountsService.getAllSubscriptions().size)
        assertEquals(accountDTO,  accountsService.getSingleSubscription(email))
        accountsService.removeAccount(email)
        assertEquals(0,  accountsService.getAllSubscriptions().size)
    }

    @Test
    fun getAllSubscriptions(){
        val accountDTO1 = accountsService.addAccount(email);
        val accountDTO2 = accountsService.addAccount(email+'2');

        assertEquals(listOf(accountDTO1, accountDTO2),  accountsService.getAllSubscriptions())
    }

    @Test
    fun getAndUpdateSubscription(){
        accountsService.addAccount(email);
        accountsService.updateSubscription(email, 5);
        val accountDTO = accountsService.getSingleSubscription(email);
        assertEquals(LocalDate.now().plusMonths(5),  accountDTO?.paidUntil)
    }

    @Test
    fun getAllUnconfirmed(){
        val accountDTO1 = accountsService.addAccount(email);
        val accountDTO2 = accountsService.addAccount(email+'2');

        assertEquals(listOf(accountDTO1, accountDTO2),  accountsService.getNonConfirmedPayments())
        accountsService.confirmPayment(email)
        assertEquals(listOf(accountDTO2),  accountsService.getNonConfirmedPayments())
        accountsService.confirmPayment(email+'2')
        assertEquals(listOf(),  accountsService.getNonConfirmedPayments())
    }

    @Test
    fun getAllActive(){
        accountsService.addAccount(email);
        accountsService.addAccount(email+'2');

        assertEquals(listOf(),  accountsService.getActive())
        accountsService.confirmPayment(email)
        val accountDTO1 = accountsService.getSingleSubscription(email);
        assertEquals(listOf(accountDTO1),  accountsService.getActive())
        accountsService.confirmPayment(email+'2')
        var accountDTO2 = accountsService.getSingleSubscription(email+'2');
        assertEquals(listOf(accountDTO1, accountDTO2),  accountsService.getActive())
    }
}