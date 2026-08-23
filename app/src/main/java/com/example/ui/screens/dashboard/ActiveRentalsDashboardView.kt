package com.example.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ContainerEntity
import com.example.model.RentCalculation
import com.example.ui.theme.*
import com.example.ui.viewmodel.CompanySettings
import com.example.util.DateUtils

@Composable
fun ActiveRentalsDashboardView(
    containers: List<ContainerEntity>,
    settings: CompanySettings,
    onSelectContainer: (ContainerEntity) -> Unit,
    onPaymentClick: (ContainerEntity) -> Unit,
    onGateOutClick: (ContainerEntity) -> Unit,
    onSlipClick: (ContainerEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING_DUES, SETTLED, MONTHLY, DAILY
    var globalRecalcTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var showRecalculatedNotice by remember { mutableStateOf(false) }

    // Active rentals are all in-yard, on-vehicle, or at-mill containers currently accumulating storage rent
    val activeRentals = remember(containers) {
        containers.filter { it.status == "IN_YARD" || it.status == "ON_VEHICLE" || it.status == "AT_MILL" }
    }

    // Calculations for all active containers using live timestamp
    val activeCalculations = remember(activeRentals, globalRecalcTimestamp) {
        activeRentals.map { container ->
            container to RentCalculation.calculate(container, globalRecalcTimestamp)
        }
    }

    // Aggregate stats
    val totalAccruedRent = remember(activeCalculations) {
        activeCalculations.sumOf { it.second.storageRent }
    }
    val totalHandlingCharges = remember(activeCalculations) {
        activeCalculations.sumOf { it.second.handlingCharges }
    }
    val totalGrossBilled = remember(activeCalculations) {
        activeCalculations.sumOf { it.second.grossTotal }
    }
    val totalPaid = remember(activeCalculations) {
        activeCalculations.sumOf { it.second.paidAmount }
    }
    val totalPending = remember(activeCalculations) {
        activeCalculations.sumOf { it.second.pendingAmount }
    }
    val countPendingDues = remember(activeCalculations) {
        activeCalculations.count { it.second.pendingAmount > 0.01 }
    }

    // Filtered items
    val filteredList = remember(activeCalculations, searchQuery, selectedFilter) {
        activeCalculations.filter { (container, calc) ->
            val matchesSearch = searchQuery.isBlank() ||
                    container.containerNo.contains(searchQuery, ignoreCase = true) ||
                    container.line.contains(searchQuery, ignoreCase = true) ||
                    container.shipper.contains(searchQuery, ignoreCase = true) ||
                    container.consignee.contains(searchQuery, ignoreCase = true) ||
                    container.bookingNo.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "PENDING_DUES" -> calc.pendingAmount > 0.01
                "SETTLED" -> calc.pendingAmount <= 0.01
                "MONTHLY" -> container.rateType == "MONTHLY"
                "DAILY" -> container.rateType == "DAILY"
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Active Rental Financial Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceVariant),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PrimaryContainer))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ACTIVE STORAGE RENTALS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = DateUtils.formatCurrency(totalPending, settings.currencySymbol),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (totalPending > 0) StatusError else StatusSuccess
                        )
                        Text(
                            text = "Total Outstanding Receivables (${countPendingDues} Units Pending)",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    // Recalculate Button
                    Button(
                        onClick = {
                            globalRecalcTimestamp = System.currentTimeMillis()
                            showRecalculatedNotice = true
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recalculate", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Recalculate", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                AnimatedVisibility(visible = showRecalculatedNotice) {
                    Text(
                        text = "✓ Rent calculations synchronized to current date & time (${DateUtils.formatTime(globalRecalcTimestamp)})",
                        fontSize = 10.sp,
                        color = StatusSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }

                Divider(color = SurfaceBorder.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Accrued Rent", fontSize = 9.sp, color = TextSecondary)
                        Text(DateUtils.formatCurrency(totalAccruedRent, settings.currencySymbol), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("Handling / LOLO", fontSize = 9.sp, color = TextSecondary)
                        Text(DateUtils.formatCurrency(totalHandlingCharges, settings.currencySymbol), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column {
                        Text("Gross Total Billed", fontSize = 9.sp, color = TextSecondary)
                        Text(DateUtils.formatCurrency(totalGrossBilled, settings.currencySymbol), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Paid Collected", fontSize = 9.sp, color = TextSecondary)
                        Text(DateUtils.formatCurrency(totalPaid, settings.currencySymbol), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Container No, Line, Shipper, BL...") },
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

        // Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val filterOptions = listOf(
                "ALL" to "All Active (${activeRentals.size})",
                "PENDING_DUES" to "Pending Dues ($countPendingDues)",
                "SETTLED" to "Settled / Paid",
                "MONTHLY" to "Monthly Rate",
                "DAILY" to "Daily Rate"
            )

            items(filterOptions) { (key, label) ->
                val isSelected = selectedFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = key },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceLight,
                        labelColor = TextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) PrimaryBlue else SurfaceBorder
                    )
                )
            }
        }

        // Active Rentals List
        if (filteredList.isEmpty()) {
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
                    Text("⏱️", fontSize = 42.sp)
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedFilter != "ALL") "No matching active rentals" else "No active storage rentals in yard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Active rentals accumulate storage rent automatically based on tariff.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 2.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.first.id }) { (container, calc) ->
                    ActiveRentalCard(
                        container = container,
                        calc = calc,
                        settings = settings,
                        onClick = { onSelectContainer(container) },
                        onPaymentClick = { onPaymentClick(container) },
                        onGateOutClick = { onGateOutClick(container) },
                        onSlipClick = { onSlipClick(container) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveRentalCard(
    container: ContainerEntity,
    calc: RentCalculation,
    settings: CompanySettings,
    onClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onGateOutClick: () -> Unit,
    onSlipClick: () -> Unit
) {
    val isPending = calc.pendingAmount > 0.01

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isPending) PrimaryContainer else SurfaceBorder
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
            // Header: Container No + Rate Tariff Badge
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

                // Rate Tariff Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (container.rateType == "MONTHLY") SecondaryContainer else PrimaryContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (container.rateType == "MONTHLY") {
                            "Monthly: ${DateUtils.formatCurrency(container.monthlyRate, settings.currencySymbol)}/mo"
                        } else {
                            "Daily: ${DateUtils.formatCurrency(container.dailyRate, settings.currencySymbol)}/day"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryBlueDark
                    )
                }
            }

            // Duration & Billable Calculation Strip
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundLight)
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
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
                        text = "${calc.totalDays} Total Days (${calc.months} Mo, ${calc.remainderDays}d)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Free Days: ${calc.freeDays} days",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Billable Days: ${calc.billableDays} days",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }

            // Itemized Financial Calculation Box
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
                    Text("Storage Rent", fontSize = 9.sp, color = TextSecondary)
                    Text(DateUtils.formatCurrency(calc.storageRent, settings.currencySymbol), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column {
                    Text("Gross Billed", fontSize = 9.sp, color = TextSecondary)
                    Text(DateUtils.formatCurrency(calc.grossTotal, settings.currencySymbol), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Column {
                    Text("Paid", fontSize = 9.sp, color = TextSecondary)
                    Text(DateUtils.formatCurrency(calc.paidAmount, settings.currencySymbol), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Pending Dues", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
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
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Rent Slip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPaymentClick,
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pay Dues", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onGateOutClick,
                    modifier = Modifier.weight(1.2f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
