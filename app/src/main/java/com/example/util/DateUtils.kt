package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun formatDate(timestamp: Long): String {
        return if (timestamp <= 0) "N/A" else dateFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return if (timestamp <= 0) "N/A" else dateTimeFormat.format(Date(timestamp))
    }

    fun formatShortDate(timestamp: Long): String {
        return if (timestamp <= 0) "N/A" else shortDateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return if (timestamp <= 0) "N/A" else timeFormat.format(Date(timestamp))
    }

    fun formatCurrency(amount: Double, currency: String = "Rs."): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        formatter.minimumFractionDigits = 0
        formatter.maximumFractionDigits = 2
        return "$currency ${formatter.format(amount)}"
    }
}
