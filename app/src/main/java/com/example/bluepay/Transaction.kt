package com.example.bluepay

import androidx.compose.runtime.mutableStateListOf

data class Transaction(
    val id: String,
    val amount: String,
    val lat: Double,
    val lng: Double,
    val timestamp: Long = System.currentTimeMillis()
)

object TransactionStore {
    val transactions = mutableStateListOf<Transaction>()

    fun addTransaction(transaction: Transaction) {
        transactions.add(0, transaction) // Add to the top of the list
    }
}
