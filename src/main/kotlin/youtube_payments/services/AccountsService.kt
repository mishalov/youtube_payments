package youtube_payments.services

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.javatime.CurrentDate
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import youtube_payments.dao.AccountDAO
import youtube_payments.dto.AccountDTO
import java.time.LocalDate

class AccountsService: IAccountsService {
    override fun addAccount(accountEmail: String):AccountDTO {
        return transaction {
            val insertResult = AccountDAO.insert {
                it[email] = accountEmail
                it[paidUntil] = LocalDate.now()
                it[paymentConfirmed] = false
            }

            if (insertResult.resultedValues?.get(0) == null) throw Exception("Can not create Account")

            AccountDTO(
                email = insertResult.resultedValues!![0][AccountDAO.email],
                paidUntil = insertResult.resultedValues!![0][AccountDAO.paidUntil],
                paymentConfirmed = false
            )
        }
    }

    override fun updateSubscription(accountEmail: String, countMonth: Int): AccountDTO {
        return transaction {
            var current = AccountDAO.select { AccountDAO.email eq accountEmail }.singleOrNull()

            if (current == null) {
                this@AccountsService.addAccount(accountEmail)
                current = AccountDAO.select { AccountDAO.email eq accountEmail }.single()
            }

            AccountDAO.update({ AccountDAO.email eq accountEmail }) {
                it[paidUntil] = current[paidUntil].plusMonths(countMonth.toLong())
                it[paymentConfirmed] = false
            }

            val newAccountObject = AccountDAO.select { AccountDAO.email eq accountEmail }.single()


            AccountDTO(
                email=newAccountObject[AccountDAO.email],
                paidUntil=newAccountObject[AccountDAO.paidUntil],
                paymentConfirmed = newAccountObject[AccountDAO.paymentConfirmed]
            )
        }
    }

    override fun getAllSubscriptions(): List<AccountDTO> {
        return transaction {
            AccountDAO.selectAll().map {
                AccountDTO(
                    email=it[AccountDAO.email],
                    paidUntil=it[AccountDAO.paidUntil],
                    paymentConfirmed = it[AccountDAO.paymentConfirmed]
                )
            }
        }
    }

    override fun getSingleSubscription(accountEmail: String): AccountDTO? {
        return transaction {
            AccountDAO.select { AccountDAO.email eq accountEmail }.singleOrNull() ?.let{
                AccountDTO(
                    email=it[AccountDAO.email],
                    paidUntil=it[AccountDAO.paidUntil],
                    paymentConfirmed = it[AccountDAO.paymentConfirmed]
                )
            }
        }
    }

    override fun removeAccount(accountEmail: String): Int {
        return transaction {
            AccountDAO.deleteWhere { AccountDAO.email eq accountEmail }
        }
    }

    override fun confirmPayment(accountEmail: String): AccountDTO {
        return transaction {
            AccountDAO.select { AccountDAO.email eq accountEmail }.singleOrNull()
                ?: throw Exception("Can not confirm non-existing account")

            AccountDAO.update({ AccountDAO.email eq accountEmail }) {
                it[paymentConfirmed] = true
            }

            AccountDAO.select { AccountDAO.email eq accountEmail }.single().let{
                AccountDTO(
                    email=it[AccountDAO.email],
                    paidUntil=it[AccountDAO.paidUntil],
                    paymentConfirmed = it[AccountDAO.paymentConfirmed]
                )
            }
        }
    }



    override fun getNonConfirmedPayments(): List<AccountDTO> {
        return transaction {
            AccountDAO.select { AccountDAO.paymentConfirmed eq false }.map {
                AccountDTO(
                    email=it[AccountDAO.email],
                    paidUntil=it[AccountDAO.paidUntil],
                    paymentConfirmed = it[AccountDAO.paymentConfirmed]
                )
            }
        }
    }

    override fun updateAccountsActivationState() {
        transaction {
            val wronglyActiveIds = AccountDAO.select {
                (AccountDAO.paymentConfirmed eq true
                and
                AccountDAO.paidUntil.less(CurrentDate))
            }.map{ it[AccountDAO.id] }

            AccountDAO.update ({
                AccountDAO.id inList wronglyActiveIds
            }) {
                it[AccountDAO.paymentConfirmed] = false
            }
        }
    }

    override fun getActive(): List<AccountDTO> {
        return transaction {
            AccountDAO.select {
                AccountDAO.paymentConfirmed  eq true
            }.map {
                AccountDTO(
                    email=it[AccountDAO.email],
                    paidUntil=it[AccountDAO.paidUntil],
                    paymentConfirmed = it[AccountDAO.paymentConfirmed]
                )
            }
        }
    }

    fun removeAll() {
        return transaction {
            AccountDAO.deleteAll()
        }
    }
}