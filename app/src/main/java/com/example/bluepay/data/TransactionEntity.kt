package com.example.bluepay.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val amount: String,
    val lat: Double,
    val lng: Double,
    val timestamp: Long,
    val status: String = "PENDING_SYNC", // PENDING_SYNC, CONFIRMED, REJECTED
    val signature: String? = null // For Q04: Digital Signature
)
