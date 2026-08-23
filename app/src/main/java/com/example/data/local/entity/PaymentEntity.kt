package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val containerId: Long,
    val containerNo: String,
    val amount: Double,
    val paymentDate: Long = System.currentTimeMillis(),
    val paymentMethod: String = "CASH", // "CASH", "BANK_TRANSFER", "CHEQUE", "ONLINE"
    val referenceNo: String = "",
    val receivedBy: String = "Admin / Cashier",
    val notes: String = ""
)
