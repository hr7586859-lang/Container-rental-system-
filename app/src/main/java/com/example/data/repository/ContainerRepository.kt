package com.example.data.repository

import com.example.data.local.dao.ContainerDao
import com.example.data.local.dao.PaymentDao
import com.example.data.local.entity.ContainerEntity
import com.example.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Random

class ContainerRepository(
    private val containerDao: ContainerDao,
    private val paymentDao: PaymentDao
) {
    val allContainers: Flow<List<ContainerEntity>> = containerDao.getAllContainers()
    val inYardContainers: Flow<List<ContainerEntity>> = containerDao.getContainersInYard()
    val gatedOutContainers: Flow<List<ContainerEntity>> = containerDao.getGatedOutContainers()
    val allPayments: Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    fun getContainerFlow(id: Long): Flow<ContainerEntity?> = containerDao.getContainerFlowById(id)

    suspend fun getContainerById(id: Long): ContainerEntity? = containerDao.getContainerById(id)

    fun getPaymentsForContainer(containerId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForContainer(containerId)

    fun searchContainers(query: String): Flow<List<ContainerEntity>> =
        containerDao.searchContainers(query)

    suspend fun insertContainer(container: ContainerEntity): Long {
        val eir = if (container.eirNo.isBlank()) generateEirNo() else container.eirNo
        val gatePass = if (container.gatePassNo.isBlank()) generateGatePassNo() else container.gatePassNo
        return containerDao.insertContainer(
            container.copy(
                eirNo = eir,
                gatePassNo = gatePass
            )
        )
    }

    suspend fun updateContainer(container: ContainerEntity) {
        containerDao.updateContainer(container)
    }

    suspend fun deleteContainer(id: Long) {
        paymentDao.deletePaymentsForContainer(id)
        containerDao.deleteContainerById(id)
    }

    suspend fun deleteAllContainers() {
        containerDao.deleteAllContainers()
    }

    suspend fun recordPayment(
        containerId: Long,
        amount: Double,
        paymentMethod: String,
        referenceNo: String,
        receivedBy: String,
        notes: String
    ): Long {
        val container = containerDao.getContainerById(containerId) ?: return -1
        val newPaid = container.paidAmount + amount
        val paymentId = paymentDao.insertPayment(
            PaymentEntity(
                containerId = containerId,
                containerNo = container.containerNo,
                amount = amount,
                paymentDate = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                referenceNo = referenceNo,
                receivedBy = receivedBy,
                notes = notes
            )
        )
        containerDao.updateContainer(container.copy(paidAmount = newPaid))
        return paymentId
    }

    suspend fun gateOutContainer(
        containerId: Long,
        outDate: Long = System.currentTimeMillis(),
        destinationType: String = "FINAL_EXIT", // "VEHICLE", "MILL", "FINAL_EXIT"
        millName: String = "",
        millLocation: String = "",
        settlePayment: Boolean = true,
        paymentAmount: Double = 0.0,
        paymentMethod: String = "CASH",
        clearedBy: String = "Yard Master",
        driverName: String? = null,
        driverCnic: String? = null,
        driverCell: String? = null,
        transporter: String? = null,
        truckNo: String? = null,
        remarks: String? = null
    ) {
        val container = containerDao.getContainerById(containerId) ?: return
        var newPaid = container.paidAmount
        if (settlePayment && paymentAmount > 0) {
            newPaid += paymentAmount
            val note = when (destinationType) {
                "VEHICLE" -> "Gate-Out Dispatch (On Truck/Gari)"
                "MILL" -> "Gate-Out Dispatch (To Mill: $millName)"
                else -> "Final Gate-Out Settlement"
            }
            paymentDao.insertPayment(
                PaymentEntity(
                    containerId = containerId,
                    containerNo = container.containerNo,
                    amount = paymentAmount,
                    paymentDate = outDate,
                    paymentMethod = paymentMethod,
                    referenceNo = "CLR-${System.currentTimeMillis() % 100000}",
                    receivedBy = clearedBy,
                    notes = note
                )
            )
        }

        val newStatus = when (destinationType) {
            "VEHICLE" -> "ON_VEHICLE"
            "MILL" -> "AT_MILL"
            else -> "GATED_OUT"
        }

        val isFinal = destinationType == "FINAL_EXIT"

        val updated = container.copy(
            gateOutDate = if (isFinal) outDate else container.gateOutDate,
            dispatchDate = outDate,
            status = newStatus,
            destinationType = destinationType,
            millName = if (destinationType == "MILL") millName else container.millName,
            millLocation = if (destinationType == "MILL") millLocation else container.millLocation,
            paidAmount = newPaid,
            clearedBy = clearedBy,
            isSettled = isFinal,
            driverName = driverName?.takeIf { it.isNotBlank() } ?: container.driverName,
            driverCnic = driverCnic?.takeIf { it.isNotBlank() } ?: container.driverCnic,
            driverCell = driverCell?.takeIf { it.isNotBlank() } ?: container.driverCell,
            transporter = transporter?.takeIf { it.isNotBlank() } ?: container.transporter,
            truckNo = truckNo?.takeIf { it.isNotBlank() } ?: container.truckNo,
            remarks = remarks?.takeIf { it.isNotBlank() } ?: container.remarks
        )
        containerDao.updateContainer(updated)
    }

    suspend fun returnToYard(
        containerId: Long,
        yardBay: String,
        returnDate: Long = System.currentTimeMillis(),
        receivedBy: String = "Yard Gate Officer",
        notes: String = "Returned back to yard storage"
    ) {
        val container = containerDao.getContainerById(containerId) ?: return
        val updated = container.copy(
            status = "IN_YARD",
            destinationType = "YARD",
            yardBay = yardBay.ifBlank { "Bay A-01" },
            remarks = if (container.remarks.isNotBlank()) "${container.remarks} | $notes" else notes
        )
        containerDao.updateContainer(updated)
    }

    private fun generateEirNo(): String {
        val rand = (10000..99999).random()
        return "EIR-${rand}"
    }

    private fun generateGatePassNo(): String {
        val rand = (100000..999999).random()
        return "GP-${rand}"
    }
}
