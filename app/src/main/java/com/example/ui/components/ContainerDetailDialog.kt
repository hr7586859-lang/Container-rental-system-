package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

@Composable
fun ContainerDetailDialog(
    container: ContainerEntity,
    settings: CompanySettings,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit = {},
    onGateOutClick: () -> Unit,
    onReturnToYardClick: (yardBay: String) -> Unit = {},
    onAddPaymentClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    var calculationTargetDate by remember {
        mutableStateOf(container.gateOutDate ?: System.currentTimeMillis())
    }
    var isRecalculatedNotice by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val calc = remember(calculationTargetDate, container) {
        RentCalculation.calculate(container, calculationTargetDate)
    }

    var showSlipPreview by remember { mutableStateOf(false) }
    var showReturnDialog by remember { mutableStateOf(false) }
    var returnBay by remember { mutableStateOf(container.yardBay.ifBlank { "Bay A-01" }) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete Container / کنٹینر ڈیلیٹ کریں",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusError
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to completely remove container '${container.containerNo}' and its payment records? This cannot be undone.",
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("DELETE CONTAINER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CANCEL", fontSize = 11.sp)
                }
            }
        )
    }

    if (showSlipPreview) {
        SlipPreviewDialog(
            container = container,
            calc = calc,
            settings = settings,
            isGateOut = container.status == "GATED_OUT",
            onDismiss = { showSlipPreview = false }
        )
    }

    if (showReturnDialog) {
        AlertDialog(
            onDismissRequest = { showReturnDialog = false },
            title = {
                Text(
                    text = "Return Container to Yard / یارڈ واپسی",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Container '${container.containerNo}' is currently ${if (container.status == "AT_MILL") "at Mill (${container.millName})" else "on Vehicle"}. Confirm receiving back into yard:",
                        fontSize = 12.sp,
                        color = TextPrimary
                    )
                    OutlinedTextField(
                        value = returnBay,
                        onValueChange = { returnBay = it },
                        label = { Text("Assign Yard Bay / Slot") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReturnToYardClick(returnBay)
                        showReturnDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("RECEIVE IN YARD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showReturnDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CANCEL", fontSize = 11.sp)
                }
            }
        )
    }

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceVariant)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = container.containerNo,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "${container.size} • ${container.line}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Dynamic Status Indicator
                        val (badgeText, badgeBg, badgeColor) = when (container.status) {
                            "IN_YARD" -> Triple("IN YARD", PrimaryContainer, OnPrimaryContainer)
                            "ON_VEHICLE" -> Triple("ON VEHICLE", StatusWarningBg, StatusWarning)
                            "AT_MILL" -> Triple("AT MILL", Color(0xFFE8EAF6), Color(0xFF283593))
                            else -> Triple("GATED OUT", StatusSuccessBg, StatusSuccess)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(badgeBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }

                        // Edit Button
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Container",
                                modifier = Modifier.size(16.dp),
                                tint = PrimaryBlue
                            )
                        }

                        // Delete Button
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusErrorBg)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Container",
                                modifier = Modifier.size(16.dp),
                                tint = StatusError
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp), color = SurfaceBorder)

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Financial / Pending Rent Highlight Box
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceVariant)
                                .border(1.dp, PrimaryContainer, RoundedCornerShape(20.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (container.status != "GATED_OUT") "OUTSTANDING RENT (RUNNING)" else "FINAL SETTLEMENT",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                        Text(
                                            text = DateUtils.formatCurrency(calc.pendingAmount, settings.currencySymbol),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (calc.pendingAmount > 0) StatusError else StatusSuccess
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("TOTAL BILLED", fontSize = 10.sp, color = TextSecondary)
                                        Text(DateUtils.formatCurrency(calc.grossTotal, settings.currencySymbol), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Divider(color = SurfaceBorder)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    DetailKeyVal("Duration", "${calc.totalDays} Days (${calc.months} Mo, ${calc.remainderDays}d)")
                                    DetailKeyVal("Paid Received", DateUtils.formatCurrency(calc.paidAmount, settings.currencySymbol), color = StatusSuccess)
                                    DetailKeyVal("Rate", "${container.rateType} (${DateUtils.formatCurrency(if (container.rateType == "MONTHLY") container.monthlyRate else container.dailyRate, settings.currencySymbol)})")
                                }

                                // Interactive Rent Recalculation Button
                                Button(
                                    onClick = {
                                        calculationTargetDate = System.currentTimeMillis()
                                        isRecalculatedNotice = true
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "CALCULATE RENT TO CURRENT DATE",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Complete Specification Cards
                    item {
                        DetailSection(title = "CONTAINER & LOCATION STATUS") {
                            DetailRow("Container No", container.containerNo, isBold = true)
                            DetailRow("Size / Type", container.size)
                            DetailRow("Shipping Line", container.line)
                            DetailRow("Current Status", when (container.status) {
                                "IN_YARD" -> "In Yard (${container.yardBay})"
                                "ON_VEHICLE" -> "On Vehicle / Gari Par (Dispatched)"
                                "AT_MILL" -> "At Mill: ${container.millName}"
                                else -> "Gated Out & Cleared"
                            }, isBold = true, color = PrimaryBlue)
                            if (container.status == "AT_MILL") {
                                DetailRow("Mill Name", container.millName, isBold = true)
                                DetailRow("Mill Location", container.millLocation.ifBlank { "Transit / Factory Area" })
                            }
                            DetailRow("Condition", container.condition)
                            DetailRow("Seal Number", container.sealNo.ifBlank { "N/A" })
                            DetailRow("Yard Bay Location", container.yardBay)
                            DetailRow("EIR Reference", container.eirNo)
                            if (container.gatePassNo.isNotBlank()) {
                                DetailRow("Gate Pass No", container.gatePassNo, color = PrimaryBlue, isBold = true)
                            }
                        }
                    }

                    item {
                        DetailSection(title = "TIMELINE & CLEARANCE") {
                            DetailRow("Gate In Date", DateUtils.formatDateTime(container.gateInDate))
                            if (container.dispatchDate != null) {
                                DetailRow("Dispatch Out Date", DateUtils.formatDateTime(container.dispatchDate))
                            }
                            if (container.gateOutDate != null) {
                                DetailRow("Gate Out Date", DateUtils.formatDateTime(container.gateOutDate))
                            }
                            DetailRow("Total Days Count", "${calc.totalDays} Days")
                            DetailRow("Free Storage Days", "${calc.freeDays} Days")
                            DetailRow("Billable Days", "${calc.billableDays} Days")
                            if (container.clearedBy.isNotBlank()) {
                                DetailRow("Cleared By Officer", container.clearedBy)
                            }
                        }
                    }

                    item {
                        DetailSection(title = "PARTIES & LOGISTICS") {
                            DetailRow("Booking / BL No", container.bookingNo.ifBlank { "N/A" })
                            DetailRow("NOC Certificate", container.nocNo.ifBlank { "N/A" })
                            DetailRow("Shipper (Sender)", container.shipper.ifBlank { "N/A" })
                            DetailRow("Consignee (Receiver)", container.consignee.ifBlank { "N/A" }, isBold = true)
                            DetailRow("Transporter", container.transporter)
                            DetailRow("Truck / Trailer", container.truckNo)
                            DetailRow("Driver Name", container.driverName)
                            DetailRow("Driver CNIC", container.driverCnic, isBold = true)
                            DetailRow("Driver Cell", container.driverCell)
                        }
                    }

                    item {
                        DetailSection(title = "CHARGES BREAKDOWN") {
                            DetailRow("Storage Rent", DateUtils.formatCurrency(calc.storageRent, settings.currencySymbol))
                            DetailRow("Handling (LOLO)", DateUtils.formatCurrency(calc.handlingCharges, settings.currencySymbol))
                            if (calc.repairCharges > 0) {
                                DetailRow("Damage / Repair", DateUtils.formatCurrency(calc.repairCharges, settings.currencySymbol))
                            }
                            if (calc.otherCharges > 0) {
                                DetailRow("Other Charges", DateUtils.formatCurrency(calc.otherCharges, settings.currencySymbol))
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = SurfaceBorder)
                            DetailRow("Gross Total", DateUtils.formatCurrency(calc.grossTotal, settings.currencySymbol), isBold = true)
                            DetailRow("Total Paid", DateUtils.formatCurrency(calc.paidAmount, settings.currencySymbol), color = StatusSuccess)
                            DetailRow(
                                "Pending Amount",
                                DateUtils.formatCurrency(calc.pendingAmount, settings.currencySymbol),
                                isBold = true,
                                color = if (calc.pendingAmount > 0) StatusError else StatusSuccess
                            )
                        }
                    }

                    if (container.remarks.isNotBlank()) {
                        item {
                            DetailSection(title = "NOTES & REMARKS") {
                                Text(container.remarks, fontSize = 12.sp, color = TextPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showSlipPreview = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SLIP / PASS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onAddPaymentClick,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PAYMENT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (container.status == "ON_VEHICLE" || container.status == "AT_MILL") {
                        Button(
                            onClick = { showReturnDialog = true },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                        ) {
                            Icon(Icons.Default.KeyboardReturn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("RETURN YARD", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (container.status == "IN_YARD") {
                        Button(
                            onClick = onGateOutClick,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GATE OUT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
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
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            content()
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
private fun DetailKeyVal(
    label: String,
    value: String,
    color: Color = TextPrimary
) {
    Column {
        Text(label, fontSize = 9.sp, color = TextSecondary)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
