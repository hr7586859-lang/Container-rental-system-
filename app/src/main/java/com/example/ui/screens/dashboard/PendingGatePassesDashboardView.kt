package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.local.entity.ContainerEntity
import com.example.model.RentCalculation
import com.example.ui.theme.*
import com.example.ui.viewmodel.CompanySettings
import com.example.util.DateUtils
import com.example.util.PrintUtils

enum class GatePassSection {
    PENDING_CLEARANCE,
    ISSUED_PASSES
}

@Composable
fun PendingGatePassesDashboardView(
    containers: List<ContainerEntity>,
    settings: CompanySettings,
    onSelectContainer: (ContainerEntity) -> Unit,
    onGateOutClick: (ContainerEntity) -> Unit,
    onPaymentClick: (ContainerEntity) -> Unit,
    onSlipClick: (ContainerEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf(GatePassSection.PENDING_CLEARANCE) }
    var searchQuery by remember { mutableStateOf("") }

    // Containers in yard or dispatched to vehicle/mill awaiting final gate pass settlement
    val pendingClearanceContainers = remember(containers) {
        containers.filter { it.status != "GATED_OUT" }
    }

    // Cleared/Gated-out containers with issued gate passes
    val issuedPassContainers = remember(containers) {
        containers.filter { it.status == "GATED_OUT" }
    }

    // Filtered lists
    val filteredPending = remember(pendingClearanceContainers, searchQuery) {
        pendingClearanceContainers.filter { container ->
            searchQuery.isBlank() ||
                    container.containerNo.contains(searchQuery, ignoreCase = true) ||
                    container.line.contains(searchQuery, ignoreCase = true) ||
                    container.nocNo.contains(searchQuery, ignoreCase = true) ||
                    container.bookingNo.contains(searchQuery, ignoreCase = true) ||
                    container.transporter.contains(searchQuery, ignoreCase = true) ||
                    container.driverName.contains(searchQuery, ignoreCase = true) ||
                    container.driverCnic.contains(searchQuery, ignoreCase = true)
        }
    }

    val filteredIssued = remember(issuedPassContainers, searchQuery) {
        issuedPassContainers.filter { container ->
            searchQuery.isBlank() ||
                    container.gatePassNo.contains(searchQuery, ignoreCase = true) ||
                    container.containerNo.contains(searchQuery, ignoreCase = true) ||
                    container.line.contains(searchQuery, ignoreCase = true) ||
                    container.eirNo.contains(searchQuery, ignoreCase = true) ||
                    container.driverName.contains(searchQuery, ignoreCase = true) ||
                    container.driverCnic.contains(searchQuery, ignoreCase = true) ||
                    container.truckNo.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary & Sub-tab Switcher Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryContainer))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GATE PASS & CLEARANCE HUB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (selectedSection == GatePassSection.PENDING_CLEARANCE) {
                                "${pendingClearanceContainers.size} In Yard Awaiting Gate Pass"
                            } else {
                                "${issuedPassContainers.size} Gate Passes Issued"
                            },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedSection == GatePassSection.PENDING_CLEARANCE) StatusWarningBg else StatusSuccessBg)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (selectedSection == GatePassSection.PENDING_CLEARANCE) "AUTHORIZATION QUEUE" else "DISPATCHED / CLEARED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (selectedSection == GatePassSection.PENDING_CLEARANCE) StatusWarning else StatusSuccess
                        )
                    }
                }

                // Segmented Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceLight)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isPendingActive = selectedSection == GatePassSection.PENDING_CLEARANCE
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isPendingActive) PrimaryBlue else Color.Transparent)
                            .clickable { selectedSection = GatePassSection.PENDING_CLEARANCE }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pending Clearance (${pendingClearanceContainers.size})",
                            fontSize = 11.sp,
                            fontWeight = if (isPendingActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isPendingActive) Color.White else TextSecondary
                        )
                    }

                    val isIssuedActive = selectedSection == GatePassSection.ISSUED_PASSES
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isIssuedActive) PrimaryBlue else Color.Transparent)
                            .clickable { selectedSection = GatePassSection.ISSUED_PASSES }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Issued Passes (${issuedPassContainers.size})",
                            fontSize = 11.sp,
                            fontWeight = if (isIssuedActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isIssuedActive) Color.White else TextSecondary
                        )
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    if (selectedSection == GatePassSection.PENDING_CLEARANCE)
                        "Search Container No, NOC, Driver CNIC..."
                    else
                        "Search Gate Pass No, Container, Driver..."
                )
            },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceLight,
                unfocusedContainerColor = SurfaceLight,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = SurfaceBorder
            )
        )

        // Content
        if (selectedSection == GatePassSection.PENDING_CLEARANCE) {
            if (filteredPending.isEmpty()) {
                EmptySectionState(
                    icon = "📑",
                    title = if (searchQuery.isNotBlank()) "No matching containers" else "No containers pending clearance",
                    subtitle = "All containers currently inside the yard have been processed."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 2.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPending, key = { it.id }) { container ->
                        val calc = RentCalculation.calculate(container)
                        PendingClearanceCard(
                            container = container,
                            calc = calc,
                            settings = settings,
                            onClick = { onSelectContainer(container) },
                            onAuthorizeClick = { onGateOutClick(container) },
                            onPaymentClick = { onPaymentClick(container) },
                            onSlipClick = { onSlipClick(container) }
                        )
                    }
                }
            }
        } else {
            if (filteredIssued.isEmpty()) {
                EmptySectionState(
                    icon = "✅",
                    title = if (searchQuery.isNotBlank()) "No matching issued passes" else "No issued gate passes yet",
                    subtitle = "Authorized gate passes will appear here after containers are gated out."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 2.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredIssued, key = { it.id }) { container ->
                        val calc = RentCalculation.calculate(container)
                        IssuedGatePassCard(
                            container = container,
                            calc = calc,
                            settings = settings,
                            onClick = { onSelectContainer(container) },
                            onPrintClick = {
                                val html = PrintUtils.generateHtmlSlip(container, calc, isGateOut = true, companyName = settings.companyName)
                                PrintUtils.printSlip(context, html, "GatePass_${container.gatePassNo}")
                            },
                            onShareClick = {
                                val text = PrintUtils.generateGateOutPassText(container, calc, settings.companyName)
                                PrintUtils.shareSlipText(context, text, "Gate Pass ${container.gatePassNo}")
                            },
                            onSlipPreviewClick = { onSlipClick(container) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySectionState(icon: String, title: String, subtitle: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(icon, fontSize = 42.sp)
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PendingClearanceCard(
    container: ContainerEntity,
    calc: RentCalculation,
    settings: CompanySettings,
    onClick: () -> Unit,
    onAuthorizeClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onSlipClick: () -> Unit
) {
    val isDuesCleared = calc.pendingAmount <= 0.01

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isDuesCleared) StatusSuccess.copy(alpha = 0.5f) else StatusWarning.copy(alpha = 0.5f)
            )
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top: Container No, Line, Clearance Readiness
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

                // Readiness Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDuesCleared) StatusSuccessBg else StatusWarningBg)
                        .border(1.dp, if (isDuesCleared) StatusSuccess else StatusWarning, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isDuesCleared) "✓ DUES CLEARED" else "DUES PENDING",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDuesCleared) StatusSuccess else StatusWarning
                    )
                }
            }

            // Specs & NOC info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${container.size} | Bay: ${container.yardBay}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                if (container.nocNo.isNotBlank()) {
                    Text(
                        text = "NOC: ${container.nocNo}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }
            }

            // Dues & Financial Summary
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariant)
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Duration", fontSize = 9.sp, color = TextSecondary)
                    Text("${calc.totalDays} Days (${DateUtils.formatShortDate(container.gateInDate)})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Gross Total", fontSize = 9.sp, color = TextSecondary)
                    Text(DateUtils.formatCurrency(calc.grossTotal, settings.currencySymbol), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pending Balance", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Text(
                        text = DateUtils.formatCurrency(calc.pendingAmount, settings.currencySymbol),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDuesCleared) StatusSuccess else StatusError
                    )
                }
            }

            // Transporter & Driver Verification
            if (container.transporter.isNotBlank() || container.driverName.isNotBlank()) {
                Text(
                    text = "Transport: ${container.transporter.ifBlank { "N/A" }} | Driver: ${container.driverName.ifBlank { "Unassigned" }} (${container.driverCnic.ifBlank { "No CNIC" }})",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onSlipClick,
                    modifier = Modifier.weight(1f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Preview Slip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (!isDuesCleared) {
                    OutlinedButton(
                        onClick = onPaymentClick,
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Settle Dues", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = onAuthorizeClick,
                    modifier = Modifier.weight(1.3f).height(36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDuesCleared) StatusSuccess else PrimaryBlue
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isDuesCleared) "Issue Gate Pass" else "Clear & Gate Out",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun IssuedGatePassCard(
    container: ContainerEntity,
    calc: RentCalculation,
    settings: CompanySettings,
    onClick: () -> Unit,
    onPrintClick: () -> Unit,
    onShareClick: () -> Unit,
    onSlipPreviewClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(StatusSuccess.copy(alpha = 0.5f))
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Gate Pass No Pill & Gated Out Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "PASS: ${container.gatePassNo.ifBlank { "GP-${container.id + 1000}" }}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnPrimaryContainer
                        )
                    }
                    Text(
                        text = container.containerNo,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(StatusSuccessBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "✓ GATED OUT",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = StatusSuccess
                    )
                }
            }

            // Gate Out Date & Stay Duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Out Date: ${DateUtils.formatDateTime(container.gateOutDate ?: System.currentTimeMillis())}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Stayed: ${calc.totalDays} Days",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }

            // Outward Logistics Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundLight)
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Exit Truck: ${container.truckNo.ifBlank { "N/A" }} | Transporter: ${container.transporter.ifBlank { "N/A" }}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                Text(
                    text = "Driver: ${container.driverName.ifBlank { "N/A" }} | CNIC: ${container.driverCnic.ifBlank { "N/A" }} | Cell: ${container.driverCell.ifBlank { "N/A" }}",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                if (container.clearedBy.isNotBlank()) {
                    Text(
                        text = "Authorized By: ${container.clearedBy}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusSuccess
                    )
                }
            }

            // Settled Dues Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Paid & Settled: ${DateUtils.formatCurrency(calc.paidAmount, settings.currencySymbol)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusSuccess
                )
            }

            // Actions: Print Gate Pass, Share Pass, View Slip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onSlipPreviewClick,
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onShareClick,
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onPrintClick,
                    modifier = Modifier.weight(1.3f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print Pass", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
