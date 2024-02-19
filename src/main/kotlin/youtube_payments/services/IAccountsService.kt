package youtube_payments.services

import youtube_payments.dto.AccountDTO

interface IAccountsService {
    fun addAccount(accountEmail: String): AccountDTO
    fun updateSubscription(accountEmail: String, countMonth: Int): AccountDTO
    fun confirmPayment(accountEmail: String): AccountDTO
    fun removeAccount(accountEmail: String): Int
    fun getAllSubscriptions(): List<AccountDTO>
    fun getSingleSubscription(accountEmail: String): AccountDTO?
    fun getNonConfirmedPayments(): List<AccountDTO>
    fun updateAccountsActivationState()
    fun getActive(): List<AccountDTO>
}