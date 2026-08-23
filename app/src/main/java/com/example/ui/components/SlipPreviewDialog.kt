package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
fun SlipPreviewDialog(
    container: ContainerEntity,
    calc: RentCalculation,
    settings: CompanySettings,
    isGateOut: Boolean = container.status == "GATED_OUT",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val htmlContent = PrintUtils.generateHtmlSlip(container, calc, isGateOut, settings.companyName)
    val textSlip = if (isGateOut) {
        PrintUtils.generateGateOutPassText(container, calc, settings.companyName)
    } else {
        PrintUtils.generateGateInSlipText(container, settings.companyName)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
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
                    Text(
                        text = if (isGateOut) "Gate Out Pass Preview" else "Gate In Slip Preview",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Text("✕", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp), color = SurfaceBorder)

                // Slip Visual Paper Card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFAFAFA))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = settings.companyName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PrimaryBlue,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = settings.yardAddress,
                                    fontSize = 11.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryContainer)
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (isGateOut) "OFFICIAL GATE OUT PASS" else "GATE IN RECEIPT / EIR",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = OnPrimaryContainer,
                                        letterSpacing = 1.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Serial No: ${if (isGateOut) container.gatePassNo else container.eirNo} | Date: ${DateUtils.formatDateTime(if (isGateOut) (container.gateOutDate ?: System.currentTimeMillis()) else container.gateInDate)}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        item {
                            SlipSectionHeader("1. CONTAINER DETAILS")
                            SlipRow("Container No", container.containerNo, isBold = true)
                            SlipRow("Size & Type", container.size)
                            SlipRow("Shipping Line", container.line)
                            SlipRow("Condition", container.condition)
                            SlipRow("Seal No / Bay", "${container.sealNo.ifBlank { "N/A" }} / ${container.yardBay}")
                        }

                        item {
                            SlipSectionHeader("2. PARTIES & REFERENCE")
                            SlipRow("Booking / BL No", container.bookingNo.ifBlank { "N/A" })
                            SlipRow("NOC Certificate", container.nocNo.ifBlank { "N/A" })
                            SlipRow("Shipper (Sender)", container.shipper.ifBlank { "N/A" })
                            SlipRow("Consignee (Receiver)", container.consignee.ifBlank { "N/A" })
                        }

                        item {
                            SlipSectionHeader("3. TRANSPORTER & DRIVER IDENTITY")
                            SlipRow("Transporter", container.transporter.ifBlank { "N/A" })
                            SlipRow("Truck No", container.truckNo.ifBlank { "N/A" })
                            SlipRow("Driver Name", container.driverName.ifBlank { "N/A" })
                            SlipRow("Driver CNIC", container.driverCnic.ifBlank { "N/A" }, isBold = true)
                            SlipRow("Driver Cell", container.driverCell.ifBlank { "N/A" })
                        }

                        item {
                            SlipSectionHeader("4. RENT & CHARGES BREAKDOWN")
                            SlipRow("Gate In Date", DateUtils.formatDateTime(container.gateInDate))
                            if (isGateOut) {
                                SlipRow("Gate Out Date", DateUtils.formatDateTime(container.gateOutDate ?: System.currentTimeMillis()))
                            }
                            SlipRow("Total Duration", "${calc.totalDays} Days (${calc.months} Mo, ${calc.remainderDays} Days)")
                            SlipRow("Free Days Allowed", "${calc.freeDays} Days")
                            SlipRow("Billable Days", "${calc.billableDays} Days")
                            SlipRow("Storage Rent (${calc.rateType})", DateUtils.formatCurrency(calc.storageRent, settings.currencySymbol))
                            SlipRow("Handling (LOLO)", DateUtils.formatCurrency(calc.handlingCharges, settings.currencySymbol))
                            if (calc.repairCharges > 0) {
                                SlipRow("Repair / Damage Charges", DateUtils.formatCurrency(calc.repairCharges, settings.currencySymbol))
                            }
                            if (calc.otherCharges > 0) {
                                SlipRow("Other / Admin Charges", DateUtils.formatCurrency(calc.otherCharges, settings.currencySymbol))
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.Gray)
                            SlipRow("Gross Total Charges", DateUtils.formatCurrency(calc.grossTotal, settings.currencySymbol), isBold = true)
                            SlipRow("Total Amount Received", DateUtils.formatCurrency(calc.paidAmount, settings.currencySymbol), color = StatusSuccess)
                            SlipRow(
                                "Outstanding Pending Due",
                                DateUtils.formatCurrency(calc.pendingAmount, settings.currencySymbol),
                                isBold = true,
                                color = if (calc.pendingAmount > 0.01) StatusError else StatusSuccess
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (calc.pendingAmount <= 0.01) StatusSuccessBg else StatusErrorBg)
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (calc.pendingAmount <= 0.01) "STATUS: FULLY SETTLED & AUTHORIZED" else "STATUS: PENDING DUES (${DateUtils.formatCurrency(calc.pendingAmount, settings.currencySymbol)})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (calc.pendingAmount <= 0.01) StatusSuccess else StatusError
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.width(100.dp).height(1.dp).background(Color.DarkGray))
                                    Text("Driver Signature", fontSize = 10.sp, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(modifier = Modifier.width(100.dp).height(1.dp).background(Color.DarkGray))
                                    Text("Duty Officer / Stamp", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            PrintUtils.shareSlipText(
                                context = context,
                                text = textSlip,
                                title = "Share Slip - ${container.containerNo}"
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share Text/PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            PrintUtils.printSlip(
                                context = context,
                                htmlContent = htmlContent,
                                jobName = "Slip-${container.containerNo}"
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Slip", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SlipSectionHeader(title: String) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = PrimaryBlue,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun SlipRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = color,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}
