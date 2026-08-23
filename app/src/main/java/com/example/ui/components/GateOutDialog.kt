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
import com.example.model.RentCalculation
import com.example.ui.theme.*
import com.example.ui.viewmodel.CompanySettings
import com.example.util.DateUtils
import com.example.util.PrintUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GateOutDialog(
    container: ContainerEntity,
    settings: CompanySettings,
    onDismiss: () -> Unit,
    onGateOutConfirmed: (
        gateOutDate: Long,
        destinationType: String,
        millName: String,
        millLocation: String,
        paymentAmount: Double,
        paymentMethod: String,
        clearedBy: String,
        driverName: String,
        driverCnic: String,
        driverCell: String,
        transporter: String,
        truckNo: String,
        remarks: String
    ) -> Unit
) {
    val context = LocalContext.current
    var gateOutDate by remember { mutableStateOf(System.currentTimeMillis()) }

    // Destination Choice: "VEHICLE" (Gari Par), "MILL" (Kisi Mill MN), "FINAL_EXIT" (Mukammal Clearance)
    var destinationType by remember {
        mutableStateOf(
            if (container.destinationType == "MILL") "MILL"
            else if (container.destinationType == "VEHICLE") "VEHICLE"
            else "VEHICLE"
        )
    }

    var millName by remember { mutableStateOf(container.millName.ifBlank { "Orient Textile Mills Ltd." }) }
    var millLocation by remember { mutableStateOf(container.millLocation.ifBlank { "Multan Industrial Estate" }) }

    // Dynamic Calculation state based on selected gateOutDate
    var rentCalc by remember(gateOutDate, container) {
        mutableStateOf(RentCalculation.calculate(container, gateOutDate))
    }

    var paymentAmountText by remember(rentCalc) {
        mutableStateOf(if (rentCalc.pendingAmount > 0) "${rentCalc.pendingAmount.toInt()}" else "0")
    }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var clearedBy by remember { mutableStateOf(container.clearedBy.ifBlank { "Yard Gate Officer" }) }

    // Driver/Vehicle Outward details
    var outDriverName by remember { mutableStateOf(container.driverName) }
    var outDriverCnic by remember { mutableStateOf(container.driverCnic) }
    var outDriverCell by remember { mutableStateOf(container.driverCell) }
    var outTransporter by remember { mutableStateOf(container.transporter) }
    var outTruckNo by remember { mutableStateOf(container.truckNo) }
    var remarks by remember { mutableStateOf(container.remarks) }

    var showSlipPreview by remember { mutableStateOf(false) }

    // Date Picker for Gate Out
    val calendar = Calendar.getInstance().apply { timeInMillis = gateOutDate }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 12, 0, 0)
            }
            gateOutDate = cal.timeInMillis
            rentCalc = RentCalculation.calculate(container, gateOutDate)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    if (showSlipPreview) {
        val updatedForPreview = container.copy(
            gateOutDate = gateOutDate,
            destinationType = destinationType,
            millName = if (destinationType == "MILL") millName else "",
            millLocation = if (destinationType == "MILL") millLocation else "",
            driverName = outDriverName,
            driverCnic = outDriverCnic,
            driverCell = outDriverCell,
            transporter = outTransporter,
            truckNo = outTruckNo,
            clearedBy = clearedBy,
            status = when (destinationType) {
                "VEHICLE" -> "ON_VEHICLE"
                "MILL" -> "AT_MILL"
                else -> "GATED_OUT"
            },
            paidAmount = container.paidAmount + (paymentAmountText.toDoubleOrNull() ?: 0.0)
        )
        SlipPreviewDialog(
            container = updatedForPreview,
            calc = RentCalculation.calculate(updatedForPreview, gateOutDate),
            settings = settings,
            isGateOut = true,
            onDismiss = { showSlipPreview = false }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.95f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header (Clean Minimalism Style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceVariant)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "Gate Out & Dispatch",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Gari Par / Mill MN / Final Exit & Rent Tracking",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "OUTWARD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnPrimaryContainer
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 1. Destination Type Selection Card (Core Feature Requested)
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryBlue.copy(alpha = 0.4f)))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "SELECT GATE OUT DESTINATION / روانگی کا مقام",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    letterSpacing = 1.sp
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DestinationOptionCard(
                                        title = "🚛 Gari Par Out (On Vehicle / In Transit)",
                                        subtitle = "Container yard sy out ho kar gari/trailer pr hy — rent calculate hota rahy ga.",
                                        isSelected = destinationType == "VEHICLE",
                                        onClick = { destinationType = "VEHICLE" }
                                    )

                                    DestinationOptionCard(
                                        title = "🏭 Kisi Mill MN Out (At Mill / Factory)",
                                        subtitle = "Container kisi mill ya factory ko dispatch ho raha hy — rent calculate hota rahy ga.",
                                        isSelected = destinationType == "MILL",
                                        onClick = { destinationType = "MILL" }
                                    )

                                    DestinationOptionCard(
                                        title = "🏁 Final Line Exit / Full Clearance (ڈپو مکمل ایگزٹ)",
                                        subtitle = "Container shipping line ko return ya complete clear ho kar exit ho raha hy.",
                                        isSelected = destinationType == "FINAL_EXIT",
                                        onClick = { destinationType = "FINAL_EXIT" }
                                    )
                                }

                                if (destinationType == "MILL") {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Mill & Factory Details:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = millName,
                                            onValueChange = { millName = it },
                                            label = { Text("Mill / Factory Name *") },
                                            placeholder = { Text("e.g. Fazal Cloth Mills") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1.2f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        OutlinedTextField(
                                            value = millLocation,
                                            onValueChange = { millLocation = it },
                                            label = { Text("City / Location") },
                                            placeholder = { Text("e.g. Multan") },
                                            singleLine = true,
                                            modifier = Modifier.weight(0.8f),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. Container Summary Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = "CONTAINER DETAILS",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = container.containerNo,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceVariant)
                                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${container.size} • ${container.condition}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = TextPrimary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Booking Line", fontSize = 11.sp, color = TextSecondary)
                                        Text(container.line, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Gate In Date", fontSize = 11.sp, color = TextSecondary)
                                        Text(DateUtils.formatDate(container.gateInDate), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Current Yard Slot", fontSize = 11.sp, color = TextSecondary)
                                        Text(container.yardBay, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    }
                                }
                            }
                        }
                    }

                    // 3. Rental & Charges Calculation Card (Clean Minimalism Special Card)
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(SurfaceVariant)
                                .border(1.dp, PrimaryContainer, RoundedCornerShape(22.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = "RENTAL & BILLING SUMMARY",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue,
                                            letterSpacing = 1.sp
                                        )
                                        Row(
                                            verticalAlignment = Alignment.Bottom,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = DateUtils.formatCurrency(rentCalc.pendingAmount, settings.currencySymbol),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (rentCalc.pendingAmount > 0.01) StatusError else StatusSuccess
                                            )
                                            Text(
                                                text = "/ ${rentCalc.months} Mo, ${rentCalc.remainderDays}d",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = TextSecondary,
                                                modifier = Modifier.padding(bottom = 3.dp)
                                            )
                                        }
                                    }

                                    // Overdue/Days Chip
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White)
                                            .border(1.dp, PrimaryContainer, RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text("TOTAL DAYS", fontSize = 8.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                                            Text("${rentCalc.totalDays}d", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                                        }
                                    }
                                }

                                // Date Selection
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White)
                                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                        .clickable { datePickerDialog.show() }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Calculated To Gate-Out Date", fontSize = 10.sp, color = TextSecondary)
                                        Text(DateUtils.formatDateTime(gateOutDate), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                    }
                                    Text("Change Date 📅", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                                }

                                // Itemized Breakdown
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.8f))
                                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                        .padding(10.dp)
                                 ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        SummaryRow("Rate Tariff", "${container.rateType} (${DateUtils.formatCurrency(if (container.rateType == "MONTHLY") container.monthlyRate else container.dailyRate, settings.currencySymbol)})")
                                        SummaryRow("Billable Storage Days", "${rentCalc.billableDays} Days (Free: ${rentCalc.freeDays}d)")
                                        SummaryRow("Storage Rent Accumulated", DateUtils.formatCurrency(rentCalc.storageRent, settings.currencySymbol))
                                        SummaryRow("Handling / LOLO", DateUtils.formatCurrency(rentCalc.handlingCharges, settings.currencySymbol))
                                        if (rentCalc.repairCharges > 0) {
                                            SummaryRow("Repair / Damage Fee", DateUtils.formatCurrency(rentCalc.repairCharges, settings.currencySymbol))
                                        }
                                        Divider(modifier = Modifier.padding(vertical = 2.dp), color = SurfaceBorder)
                                        SummaryRow("Gross Total Charges", DateUtils.formatCurrency(rentCalc.grossTotal, settings.currencySymbol), isBold = true)
                                        SummaryRow("Previously Paid", DateUtils.formatCurrency(rentCalc.paidAmount, settings.currencySymbol), color = StatusSuccess)
                                        SummaryRow(
                                            "NET PENDING DUE",
                                            DateUtils.formatCurrency(rentCalc.pendingAmount, settings.currencySymbol),
                                            isBold = true,
                                            color = if (rentCalc.pendingAmount > 0) StatusError else StatusSuccess
                                        )
                                    }
                                }

                                // Re-calculate to current date button
                                Button(
                                    onClick = {
                                        gateOutDate = System.currentTimeMillis()
                                        rentCalc = RentCalculation.calculate(container, gateOutDate)
                                        if (rentCalc.pendingAmount > 0) {
                                            paymentAmountText = "${rentCalc.pendingAmount.toInt()}"
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("↻ RE-CALCULATE TO CURRENT DATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // 4. Outward Receiving Driver & Payment Settlement
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "OUTWARD DRIVER & PAYMENT RECEIPT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    letterSpacing = 1.sp
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = outDriverName,
                                        onValueChange = { outDriverName = it },
                                        label = { Text("Driver Name *") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = outDriverCnic,
                                        onValueChange = { outDriverCnic = it },
                                        label = { Text("Driver CNIC *") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = outTruckNo,
                                        onValueChange = { outTruckNo = it },
                                        label = { Text("Truck / Trailer No *") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = outDriverCell,
                                        onValueChange = { outDriverCell = it },
                                        label = { Text("Driver Cell No *") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                OutlinedTextField(
                                    value = outTransporter,
                                    onValueChange = { outTransporter = it },
                                    label = { Text("Transporter / Logistics Company") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = paymentAmountText,
                                        onValueChange = { paymentAmountText = it },
                                        label = { Text("Payment Receiving Now (${settings.currencySymbol})") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    OutlinedTextField(
                                        value = clearedBy,
                                        onValueChange = { clearedBy = it },
                                        label = { Text("Cleared By (Officer)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(0.8f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                Text("Payment Method:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("CASH", "BANK_TRANSFER", "CHEQUE", "ONLINE").forEach { method ->
                                        val isSel = paymentMethod == method
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSel) PrimaryBlue else SurfaceVariant)
                                                .clickable { paymentMethod = method }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = method.replace("_", " "),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else TextPrimary
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = remarks,
                                    onValueChange = { remarks = it },
                                    label = { Text("Dispatch Remarks / Instructions") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer Actions (Clean Minimalism Pattern)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSlipPreview = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PREVIEW SLIP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val pAmount = paymentAmountText.toDoubleOrNull() ?: 0.0
                            onGateOutConfirmed(
                                gateOutDate,
                                destinationType,
                                millName,
                                millLocation,
                                pAmount,
                                paymentMethod,
                                clearedBy,
                                outDriverName,
                                outDriverCnic,
                                outDriverCell,
                                outTransporter,
                                outTruckNo,
                                remarks
                            )
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (destinationType == "FINAL_EXIT") StatusSuccess else PrimaryBlue
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (destinationType) {
                                "VEHICLE" -> "DISPATCH ON GARI 🚛"
                                "MILL" -> "DISPATCH TO MILL 🏭"
                                else -> "ISSUE GATE PASS 🏁"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) PrimaryContainer.copy(alpha = 0.5f) else SurfaceVariant.copy(alpha = 0.6f)
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) PrimaryBlue else SurfaceBorder
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) PrimaryBlue else TextPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = TextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}
