package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "containers")
data class ContainerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val containerNo: String,               // e.g. "MSKU7294812"
    val size: String,                      // "20ft Standard", "40ft High Cube", "40ft Standard", "45ft HC", "20ft Reefer", "40ft Reefer", "Open Top", "Flat Rack"
    val line: String,                      // "Maersk", "MSC", "CMA CGM", "Hapag-Lloyd", "COSCO", "ONE", "Evergreen", etc.
    val bookingNo: String = "",            // Booking / BL No
    val nocNo: String = "",                // No Objection Certificate / DO No
    val gateInDate: Long = System.currentTimeMillis(),
    val gateOutDate: Long? = null,         // Null if currently inside yard
    val transporter: String = "",          // Transporter / Trucking company
    val truckNo: String = "",              // Vehicle / Truck number
    val driverName: String = "",           // Driver Name
    val driverCnic: String = "",           // Driver CNIC / National ID
    val driverCell: String = "",           // Driver Cell / Phone No
    val shipper: String = "",              // Shipper (Sender)
    val consignee: String = "",            // Consignee (Receiver)
    val condition: String = "Sound / Cargo Worthy", // "Sound / Cargo Worthy", "Damaged Floor", "Damaged Roof/Wall", "Under Repair", "Dirty / Needs Wash"
    val sealNo: String = "",               // Seal Number
    val yardBay: String = "Bay A-01",      // Yard Stack/Slot Location
    val rateType: String = "DAILY",        // "DAILY" or "MONTHLY"
    val dailyRate: Double = 600.0,         // Rate per day
    val monthlyRate: Double = 15000.0,     // Rate per month
    val freeDays: Int = 0,                 // Free storage days (e.g. 0, 3, 5, 7)
    val handlingCharges: Double = 2500.0,  // LOLO (Lift On / Lift Off) / Gate In fee
    val repairCharges: Double = 0.0,       // Damage / Cleaning / Repair fee
    val otherCharges: Double = 0.0,        // NOC verification / Administrative charges
    val paidAmount: Double = 0.0,          // Total amount paid
    val remarks: String = "",              // Special notes or cargo info
    val status: String = "IN_YARD",        // "IN_YARD", "ON_VEHICLE", "AT_MILL", "GATED_OUT"
    val destinationType: String = "YARD",  // "YARD", "VEHICLE", "MILL", "FINAL_EXIT"
    val millName: String = "",             // Mill / Factory Name (e.g. "Orient Textile Mills", "Al-Karam")
    val millLocation: String = "",         // Mill Location / City (e.g. "Multan", "Faisalabad", "Port Qasim")
    val dispatchDate: Long? = null,        // Timestamp when dispatched to vehicle/mill
    val eirNo: String = "",                // EIR (Equipment Interchange Receipt) No
    val gatePassNo: String = "",           // Gate Pass No
    val clearedBy: String = "",            // Yard Officer Name
    val isSettled: Boolean = false         // Fully paid and cleared
)
