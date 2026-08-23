package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ContainerEntity
import com.example.model.RentCalculation
import com.example.ui.theme.*
import com.example.ui.viewmodel.CompanySettings
import com.example.util.DateUtils

@Composable
fun ContainerCard(
    container: ContainerEntity,
    settings: CompanySettings,
    onClick: () -> Unit,
    onEditClick: () -> Unit = {},
    onGateOutClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onSlipClick: () -> Unit
) {
    val calc = RentCalculation.calculate(container)
    val isInYard = container.status == "IN_YARD"
    val isOnVehicle = container.status == "ON_VEHICLE"
    val isAtMill = container.status == "AT_MILL"
    val isGatedOut = container.status == "GATED_OUT"
    val isPending = calc.pendingAmount > 0.01

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isPending && !isGatedOut) PrimaryContainer else SurfaceBorder
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Container Number, Size, Status Badge, Edit Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = container.containerNo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = container.line,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Dynamic Status Badge
                    when (container.status) {
                        "IN_YARD" -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🟢 IN YARD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OnPrimaryContainer
                                )
                            }
                        }
                        "ON_VEHICLE" -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusWarningBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🚛 GARI PR OUT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StatusWarning
                                )
                            }
                        }
                        "AT_MILL" -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8EAF6))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🏭 MILL MN OUT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF283593)
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(StatusSuccessBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "🏁 GATED OUT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = StatusSuccess
                                )
                            }
                        }
                    }

                    // Edit Button
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Container",
                            modifier = Modifier.size(15.dp),
                            tint = PrimaryBlue
                        )
                    }
                }
            }

            // Specs & Location / Mill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipTag(container.size)
                    ChipTag(container.condition, isWarning = container.condition.contains("Damaged"))
                }
                Text(
                    text = when {
                        isAtMill && container.millName.isNotBlank() -> "Mill: ${container.millName.take(16)}"
                        isOnVehicle -> "On Truck: ${container.truckNo.ifBlank { "Dispatched" }}"
                        else -> "Bay: ${container.yardBay}"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAtMill || isOnVehicle) PrimaryBlue else TextSecondary
                )
            }

            // Timeline & Parties
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundLight)
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Gate In: ${DateUtils.formatShortDate(container.gateInDate)}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "${calc.totalDays} Days (${calc.months} Mo, ${calc.remainderDays}d)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                if (isAtMill && container.millName.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🏭 Mill Destination:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "${container.millName} (${container.millLocation.ifBlank { "Transit" }})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                }

                if (container.driverCnic.isNotBlank() || container.driverName.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Driver: ${container.driverName}",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "CNIC: ${container.driverCnic}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }

                if (container.consignee.isNotBlank() || container.shipper.isNotBlank()) {
                    Text(
                        text = "Party: ${container.shipper.ifBlank { "Shipper" }} ➔ ${container.consignee.ifBlank { "Consignee" }}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Financial Breakdown Card (Clean Minimalism Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceVariant)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Billed", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        text = DateUtils.formatCurrency(calc.grossTotal, settings.currencySymbol),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text("Paid", fontSize = 10.sp, color = TextSecondary)
                    Text(
                        text = DateUtils.formatCurrency(calc.paidAmount, settings.currencySymbol),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusSuccess
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (!isGatedOut) "Rent Running ⏳" else "Final Balance",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = DateUtils.formatCurrency(calc.pendingAmount, settings.currencySymbol),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isPending) StatusError else StatusSuccess
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onSlipClick,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Slip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPaymentClick,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (!isGatedOut) {
                    Button(
                        onClick = onGateOutClick,
                        modifier = Modifier.weight(1.3f).height(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = if (isInYard) Icons.Default.ExitToApp else Icons.Default.LocalShipping,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isInYard) "Gate Out" else "Clearance",
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
private fun ChipTag(text: String, isWarning: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isWarning) StatusWarningBg else SurfaceVariant)
            .border(1.dp, if (isWarning) StatusWarning.copy(alpha = 0.5f) else SurfaceBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (isWarning) StatusWarning else TextSecondary
        )
    }
}
