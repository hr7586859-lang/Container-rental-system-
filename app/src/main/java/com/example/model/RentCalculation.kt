package com.example.model

import com.example.data.local.entity.ContainerEntity
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.max

data class RentCalculation(
    val fromDate: Long,
    val toDate: Long,
    val totalDays: Int,
    val freeDays: Int,
    val billableDays: Int,
    val months: Int,
    val remainderDays: Int,
    val rateType: String,
    val dailyRate: Double,
    val monthlyRate: Double,
    val storageRent: Double,
    val handlingCharges: Double,
    val repairCharges: Double,
    val otherCharges: Double,
    val grossTotal: Double,
    val paidAmount: Double,
    val pendingAmount: Double,
    val isFullyPaid: Boolean
) {
    companion object {
        fun calculate(
            container: ContainerEntity,
            targetToDate: Long = if (container.status == "GATED_OUT") (container.gateOutDate ?: System.currentTimeMillis()) else System.currentTimeMillis()
        ): RentCalculation {
            // If container is on a vehicle or at a mill, its rent cycle starts from the dispatchDate (or gateInDate)
            val fromDate = if ((container.status == "ON_VEHICLE" || container.status == "AT_MILL" || container.destinationType == "VEHICLE" || container.destinationType == "MILL") && container.dispatchDate != null && container.dispatchDate > 0) {
                container.dispatchDate
            } else {
                container.gateInDate
            }
            val toDate = if (targetToDate < fromDate) fromDate else targetToDate

            val diffMillis = toDate - fromDate
            // At least 1 day if within same day or rounded up
            val calculatedDays = if (diffMillis <= 0) {
                1
            } else {
                max(1, ceil(diffMillis.toDouble() / TimeUnit.DAYS.toMillis(1)).toInt())
            }

            val freeDays = container.freeDays
            val billableDays = max(0, calculatedDays - freeDays)

            val months = billableDays / 30
            val remainderDays = billableDays % 30

            val storageRent = if (container.rateType == "MONTHLY") {
                val monthCost = months * container.monthlyRate
                val dayCost = remainderDays * (container.monthlyRate / 30.0)
                monthCost + dayCost
            } else {
                billableDays * container.dailyRate
            }

            val handlingCharges = container.handlingCharges
            val repairCharges = container.repairCharges
            val otherCharges = container.otherCharges

            val grossTotal = storageRent + handlingCharges + repairCharges + otherCharges
            val paid = container.paidAmount
            val pending = max(0.0, grossTotal - paid)

            return RentCalculation(
                fromDate = fromDate,
                toDate = toDate,
                totalDays = calculatedDays,
                freeDays = freeDays,
                billableDays = billableDays,
                months = months,
                remainderDays = remainderDays,
                rateType = container.rateType,
                dailyRate = container.dailyRate,
                monthlyRate = container.monthlyRate,
                storageRent = storageRent,
                handlingCharges = handlingCharges,
                repairCharges = repairCharges,
                otherCharges = otherCharges,
                grossTotal = grossTotal,
                paidAmount = paid,
                pendingAmount = pending,
                isFullyPaid = pending <= 0.01 && (grossTotal > 0 || paid > 0)
            )
        }
    }
}
