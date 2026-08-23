package com.example.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entity.ContainerEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.CompanySettings
import com.example.util.DateUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateInDialog(
    settings: CompanySettings,
    containerToEdit: ContainerEntity? = null,
    onDismiss: () -> Unit,
    onSave: (ContainerEntity) -> Unit
) {
    val isEditMode = containerToEdit != null
    val context = LocalContext.current
    var containerNo by remember { mutableStateOf(containerToEdit?.containerNo ?: "") }
    var size by remember { mutableStateOf(containerToEdit?.size ?: "40ft High Cube") }
    var line by remember { mutableStateOf(containerToEdit?.line ?: "Maersk") }
    var condition by remember { mutableStateOf(containerToEdit?.condition ?: "Sound / Cargo Worthy") }
    var sealNo by remember { mutableStateOf(containerToEdit?.sealNo ?: "") }
    var yardBay by remember { mutableStateOf(containerToEdit?.yardBay ?: "Bay A-01") }

    // Initial Status & Rental Mode: "IN_YARD", "ON_VEHICLE", "AT_MILL"
    var initialPlacement by remember { mutableStateOf(containerToEdit?.status ?: "IN_YARD") }
    var millName by remember { mutableStateOf(containerToEdit?.millName ?: "") }
    var millLocation by remember { mutableStateOf(containerToEdit?.millLocation ?: "") }

    var bookingNo by remember { mutableStateOf(containerToEdit?.bookingNo ?: "") }
    var nocNo by remember { mutableStateOf(containerToEdit?.nocNo ?: "") }
    var shipper by remember { mutableStateOf(containerToEdit?.shipper ?: "") }
    var consignee by remember { mutableStateOf(containerToEdit?.consignee ?: "") }

    var transporter by remember { mutableStateOf(containerToEdit?.transporter ?: "") }
    var truckNo by remember { mutableStateOf(containerToEdit?.truckNo ?: "") }
    var driverName by remember { mutableStateOf(containerToEdit?.driverName ?: "") }
    var driverCnic by remember { mutableStateOf(containerToEdit?.driverCnic ?: "") }
    var driverCell by remember { mutableStateOf(containerToEdit?.driverCell ?: "") }

    var gateInDate by remember { mutableStateOf(containerToEdit?.gateInDate ?: System.currentTimeMillis()) }
    var rateType by remember { mutableStateOf(containerToEdit?.rateType ?: "MONTHLY") }
    var monthlyRate by remember { mutableStateOf(containerToEdit?.monthlyRate?.toInt()?.toString() ?: "0") }
    var dailyRate by remember { mutableStateOf(containerToEdit?.dailyRate?.toInt()?.toString() ?: "0") }
    var freeDays by remember { mutableStateOf(containerToEdit?.freeDays?.toString() ?: "0") }
    var handlingCharges by remember { mutableStateOf(containerToEdit?.handlingCharges?.toInt()?.toString() ?: "0") }
    var repairCharges by remember { mutableStateOf(containerToEdit?.repairCharges?.toInt()?.toString() ?: "0") }
    var otherCharges by remember { mutableStateOf(containerToEdit?.otherCharges?.toInt()?.toString() ?: "0") }
    var paidAmount by remember { mutableStateOf(containerToEdit?.paidAmount?.toInt()?.toString() ?: "0") }
    var remarks by remember { mutableStateOf(containerToEdit?.remarks ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sizeOptions = listOf(
        "20ft Standard",
        "40ft High Cube",
        "40ft Standard",
        "45ft High Cube",
        "20ft Reefer",
        "40ft Reefer",
        "20ft Open Top",
        "40ft Flat Rack"
    )

    val lineOptions = listOf(
        "Maersk",
        "MSC",
        "CMA CGM",
        "Hapag-Lloyd",
        "COSCO",
        "ONE",
        "Evergreen",
        "PIL",
        "Yang Ming",
        "Wan Hai",
        "ZIM",
        "Private / NVOCC"
    )

    val conditionOptions = listOf(
        "Sound / Cargo Worthy",
        "Damaged Floor",
        "Damaged Roof/Wall",
        "Under Repair",
        "Dirty / Needs Wash",
        "Minor Dent"
    )

    // Date picker dialog
    val calendar = Calendar.getInstance().apply { timeInMillis = gateInDate }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 10, 0, 0)
            }
            gateInDate = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditMode) "Edit Container (${containerToEdit?.containerNo})" else "New Gate-In Entry",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        )
                        Text(
                            text = if (isEditMode) "Update specifications, rates, charges & details" else "Complete Container Inward & Tariff Setup",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceBorder)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Section 1: Container Core
                    item {
                        SectionCard(title = "1. CONTAINER SPECIFICATIONS & PLACEMENT") {
                            OutlinedTextField(
                                value = containerNo,
                                onValueChange = { containerNo = it.uppercase() },
                                label = { Text("Container Number * (e.g. MSKU-729481-2)") },
                                placeholder = { Text("MSKU-123456-7") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Initial Location / Rent Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val placementModes = listOf(
                                    "IN_YARD" to "📦 In Yard",
                                    "ON_VEHICLE" to "🚛 On Vehicle",
                                    "AT_MILL" to "🏭 At Mill"
                                )
                                placementModes.forEach { (mode, label) ->
                                    val isSelected = initialPlacement == mode
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) PrimaryBlue else SurfaceVariant)
                                            .border(1.dp, if (isSelected) PrimaryBlue else SurfaceBorder, RoundedCornerShape(10.dp))
                                            .clickable { initialPlacement = mode }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextPrimary
                                        )
                                    }
                                }
                            }

                            if (initialPlacement == "AT_MILL") {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = millName,
                                        onValueChange = { millName = it },
                                        label = { Text("Mill / Factory Name *") },
                                        placeholder = { Text("Orient Textile Mills") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = millLocation,
                                        onValueChange = { millLocation = it },
                                        label = { Text("Mill City / Area") },
                                        placeholder = { Text("Multan") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.8f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Container Size:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            ScrollableChipRow(
                                options = sizeOptions,
                                selected = size,
                                onSelect = {
                                    size = it
                                    if (it.contains("20")) {
                                        monthlyRate = "15000"
                                        dailyRate = "600"
                                    } else {
                                        monthlyRate = "22000"
                                        dailyRate = "1000"
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Shipping Line / Owner:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            ScrollableChipRow(
                                options = lineOptions,
                                selected = line,
                                onSelect = { line = it }
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Physical Condition:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            ScrollableChipRow(
                                options = conditionOptions,
                                selected = condition,
                                onSelect = { condition = it }
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = sealNo,
                                    onValueChange = { sealNo = it.uppercase() },
                                    label = { Text("Seal Number") },
                                    placeholder = { Text("SEAL-98412") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = yardBay,
                                    onValueChange = { yardBay = it.uppercase() },
                                    label = { Text(if (initialPlacement == "IN_YARD") "Yard Bay / Slot" else "Depot Staging") },
                                    placeholder = { Text("Bay A-01") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Section 2: Gate In Date & Party Details
                    item {
                        SectionCard(title = "2. SHIPPING & PARTY DETAILS") {
                            // Gate In Date Selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceVariant)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                    .clickable { datePickerDialog.show() }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Gate In Date & Time", fontSize = 11.sp, color = TextSecondary)
                                    Text(
                                        text = DateUtils.formatDateTime(gateInDate),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }
                                Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date", tint = PrimaryBlue)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = bookingNo,
                                    onValueChange = { bookingNo = it.uppercase() },
                                    label = { Text("Booking / BL No") },
                                    placeholder = { Text("BK-99120") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = nocNo,
                                    onValueChange = { nocNo = it.uppercase() },
                                    label = { Text("NOC Certificate No") },
                                    placeholder = { Text("NOC-2026-X") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = shipper,
                                onValueChange = { shipper = it },
                                label = { Text("Shipper (Sender Company / Person)") },
                                placeholder = { Text("e.g. Orient Textile Mills") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = consignee,
                                onValueChange = { consignee = it },
                                label = { Text("Consignee (Receiver / Delivery Party)") },
                                placeholder = { Text("e.g. Global Freight Logistics") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Section 3: Driver & Transporter Info
                    item {
                        SectionCard(title = "3. TRANSPORTER & DRIVER VERIFICATION") {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = transporter,
                                    onValueChange = { transporter = it },
                                    label = { Text("Transporter *") },
                                    placeholder = { Text("Al-Madina Goods") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = truckNo,
                                    onValueChange = { truckNo = it.uppercase() },
                                    label = { Text("Truck / Trailer No *") },
                                    placeholder = { Text("TLR-7841") },
                                    singleLine = true,
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = driverName,
                                onValueChange = { driverName = it },
                                label = { Text("Driver Name *") },
                                placeholder = { Text("Muhammad Rashid") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = driverCnic,
                                    onValueChange = { driverCnic = it },
                                    label = { Text("Driver CNIC *") },
                                    placeholder = { Text("42101-1234567-1") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = driverCell,
                                    onValueChange = { driverCell = it },
                                    label = { Text("Driver Cell / Phone *") },
                                    placeholder = { Text("0300-1234567") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }

                    // Section 4: Rent & Tariff Setup
                    item {
                        SectionCard(title = "4. RENTAL TARIFF & INITIAL CHARGES") {
                            Text("Rent Calculation Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = rateType == "MONTHLY",
                                    onClick = { rateType = "MONTHLY" },
                                    label = { Text("Monthly Rate (Per Month)") },
                                    modifier = Modifier.weight(1f)
                                )
                                FilterChip(
                                    selected = rateType == "DAILY",
                                    onClick = { rateType = "DAILY" },
                                    label = { Text("Daily Rate (Per Day)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = monthlyRate,
                                    onValueChange = { monthlyRate = it },
                                    label = { Text("Rent Per Month (${settings.currencySymbol})") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = dailyRate,
                                    onValueChange = { dailyRate = it },
                                    label = { Text("Rent Per Day (${settings.currencySymbol})") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = freeDays,
                                    onValueChange = { freeDays = it },
                                    label = { Text("Free Storage Days") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = handlingCharges,
                                    onValueChange = { handlingCharges = it },
                                    label = { Text("Handling / LOLO (${settings.currencySymbol})") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = repairCharges,
                                    onValueChange = { repairCharges = it },
                                    label = { Text("Damage / Repair (${settings.currencySymbol})") },
                                    placeholder = { Text("0") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                OutlinedTextField(
                                    value = otherCharges,
                                    onValueChange = { otherCharges = it },
                                    label = { Text("Other Charges (${settings.currencySymbol})") },
                                    placeholder = { Text("0") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = paidAmount,
                                onValueChange = { paidAmount = it },
                                label = { Text(if (isEditMode) "Total Paid / Received (${settings.currencySymbol})" else "Initial Advance Received (${settings.currencySymbol})") },
                                placeholder = { Text("0") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = remarks,
                                onValueChange = { remarks = it },
                                label = { Text("Remarks / Cargo Description") },
                                placeholder = { Text("e.g. Empty container for cotton loading") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    if (errorMessage != null) {
                        item {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (containerNo.isBlank()) {
                                errorMessage = "Please enter container number"
                                return@Button
                            }
                            if (driverCnic.isBlank() && driverName.isBlank()) {
                                errorMessage = "Please enter driver name and CNIC"
                                return@Button
                            }

                            val mRate = monthlyRate.toDoubleOrNull() ?: 0.0
                            val dRate = dailyRate.toDoubleOrNull() ?: 0.0
                            val fDays = freeDays.toIntOrNull() ?: 0
                            val hCharges = handlingCharges.toDoubleOrNull() ?: 0.0
                            val rCharges = repairCharges.toDoubleOrNull() ?: 0.0
                            val oCharges = otherCharges.toDoubleOrNull() ?: 0.0
                            val pAmount = paidAmount.toDoubleOrNull() ?: 0.0

                            val destination = when (initialPlacement) {
                                "ON_VEHICLE" -> "VEHICLE"
                                "AT_MILL" -> "MILL"
                                else -> "YARD"
                            }

                            val entity = ContainerEntity(
                                id = containerToEdit?.id ?: 0,
                                containerNo = containerNo.trim(),
                                size = size,
                                line = line,
                                condition = condition,
                                sealNo = sealNo.trim(),
                                yardBay = yardBay.trim().ifBlank { "Bay A-01" },
                                bookingNo = bookingNo.trim(),
                                nocNo = nocNo.trim(),
                                shipper = shipper.trim(),
                                consignee = consignee.trim(),
                                transporter = transporter.trim().ifBlank { "Unassigned" },
                                truckNo = truckNo.trim(),
                                driverName = driverName.trim(),
                                driverCnic = driverCnic.trim(),
                                driverCell = driverCell.trim(),
                                gateInDate = gateInDate,
                                dispatchDate = if (initialPlacement != "IN_YARD") gateInDate else containerToEdit?.dispatchDate,
                                gateOutDate = containerToEdit?.gateOutDate,
                                destinationType = destination,
                                millName = if (initialPlacement == "AT_MILL") millName.trim() else containerToEdit?.millName ?: "",
                                millLocation = if (initialPlacement == "AT_MILL") millLocation.trim() else containerToEdit?.millLocation ?: "",
                                rateType = rateType,
                                monthlyRate = mRate,
                                dailyRate = dRate,
                                freeDays = fDays,
                                handlingCharges = hCharges,
                                repairCharges = rCharges,
                                otherCharges = oCharges,
                                paidAmount = pAmount,
                                remarks = remarks.trim(),
                                status = if (isEditMode && containerToEdit?.status == "GATED_OUT") "GATED_OUT" else initialPlacement,
                                eirNo = containerToEdit?.eirNo ?: "",
                                gatePassNo = containerToEdit?.gatePassNo ?: "",
                                clearedBy = containerToEdit?.clearedBy ?: "",
                                isSettled = containerToEdit?.isSettled ?: false
                            )

                            onSave(entity)
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                isEditMode -> PrimaryBlue
                                initialPlacement == "ON_VEHICLE" -> StatusWarning
                                initialPlacement == "AT_MILL" -> Color(0xFF283593)
                                else -> PrimaryBlue
                            }
                        )
                    ) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Save else if (initialPlacement == "IN_YARD") Icons.Default.Check else Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when {
                                isEditMode -> "UPDATE CONTAINER"
                                initialPlacement == "ON_VEHICLE" -> "DISPATCH ON VEHICLE"
                                initialPlacement == "AT_MILL" -> "DISPATCH TO MILL"
                                else -> "SAVE & ENTER YARD"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = PrimaryBlue,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
private fun ScrollableChipRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(options.size) { idx ->
            val opt = options[idx]
            val isSel = opt == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSel) PrimaryBlue else SurfaceVariant)
                    .border(1.dp, if (isSel) PrimaryBlue else SurfaceBorder, RoundedCornerShape(10.dp))
                    .clickable { onSelect(opt) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = opt,
                    fontSize = 11.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSel) Color.White else TextPrimary
                )
            }
        }
    }
}
