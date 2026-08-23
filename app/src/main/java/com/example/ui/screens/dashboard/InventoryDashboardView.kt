package com.example.ui.screens.dashboard

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
fun InventoryDashboardView(
    containers: List<ContainerEntity>,
    settings: CompanySettings,
    onSelectContainer: (ContainerEntity) -> Unit,
    onGateInClick: () -> Unit,
    onGateOutClick: (ContainerEntity) -> Unit,
    onPaymentClick: (ContainerEntity) -> Unit,
    onSlipClick: (ContainerEntity) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLineFilter by remember { mutableStateOf("ALL") }
    var selectedConditionFilter by remember { mutableStateOf("ALL") }

    // Inventory is containers currently in the yard
    val inYardContainers = remember(containers) {
        containers.filter { it.status == "IN_YARD" }
    }

    // Breakdown metrics
    val count20ft = remember(inYardContainers) { inYardContainers.count { it.size.contains("20ft") } }
    val count40ft = remember(inYardContainers) { inYardContainers.count { it.size.contains("40ft") } }
    val countSpecial = remember(inYardContainers) { inYardContainers.count { !it.size.contains("20ft") && !it.size.contains("40ft") } }
    val countDamaged = remember(inYardContainers) { inYardContainers.count { it.condition.contains("Damaged") || it.condition.contains("Repair") } }

    // Available lines in current inventory
    val availableLines = remember(inYardContainers) {
        listOf("ALL") + inYardContainers.map { it.line.trim() }.distinct().filter { it.isNotBlank() }
    }

    // Filtered list
    val filteredInventory = remember(inYardContainers, searchQuery, selectedLineFilter, selectedConditionFilter) {
        inYardContainers.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.containerNo.contains(searchQuery, ignoreCase = true) ||
                    item.yardBay.contains(searchQuery, ignoreCase = true) ||
                    item.bookingNo.contains(searchQuery, ignoreCase = true) ||
                    item.nocNo.contains(searchQuery, ignoreCase = true) ||
                    item.driverName.contains(searchQuery, ignoreCase = true) ||
                    item.driverCnic.contains(searchQuery, ignoreCase = true) ||
                    item.transporter.contains(searchQuery, ignoreCase = true)

            val matchesLine = selectedLineFilter == "ALL" || item.line.equals(selectedLineFilter, ignoreCase = true)
            val matchesCondition = when (selectedConditionFilter) {
                "ALL" -> true
                "SOUND" -> !item.condition.contains("Damaged", ignoreCase = true) && !item.condition.contains("Repair", ignoreCase = true)
                "DAMAGED" -> item.condition.contains("Damaged", ignoreCase = true) || item.condition.contains("Repair", ignoreCase = true)
                else -> true
            }

            matchesSearch && matchesLine && matchesCondition
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Summary Quick Counters (Minimalist Grid)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            InventoryStatMiniCard(
                title = "Total in Yard",
                count = "${inYardContainers.size}",
                subtitle = "Active Bays",
                modifier = Modifier.weight(1f),
                containerColor = SurfaceVariant
            )
            InventoryStatMiniCard(
                title = "20ft Units",
                count = "$count20ft",
                subtitle = "Standard/Reefer",
                modifier = Modifier.weight(1f),
                containerColor = SurfaceVariant
            )
            InventoryStatMiniCard(
                title = "40ft Units",
                count = "$count40ft",
                subtitle = "HC / Standard",
                modifier = Modifier.weight(1f),
                containerColor = SurfaceVariant
            )
            if (countDamaged > 0) {
                InventoryStatMiniCard(
                    title = "Damaged",
                    count = "$countDamaged",
                    subtitle = "Needs Repair",
                    modifier = Modifier.weight(1f),
                    containerColor = StatusWarningBg,
                    textColor = StatusWarning
                )
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Container No, Bay, Booking BL, Transporter...") },
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

        // Filter Bar (Lines & Condition)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableLines) { line ->
                val isSelected = selectedLineFilter == line
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedLineFilter = line },
                    label = {
                        Text(
                            text = if (line == "ALL") "All Lines" else line,
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

            item {
                val isSoundSelected = selectedConditionFilter == "SOUND"
                FilterChip(
                    selected = isSoundSelected,
                    onClick = { selectedConditionFilter = if (isSoundSelected) "ALL" else "SOUND" },
                    label = { Text("Sound Only", fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusSuccess,
                        selectedLabelColor = Color.White,
                        containerColor = SurfaceLight,
                        labelColor = TextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSoundSelected,
                        borderColor = if (isSoundSelected) StatusSuccess else SurfaceBorder
                    )
                )
            }

            if (countDamaged > 0) {
                item {
                    val isDamagedSelected = selectedConditionFilter == "DAMAGED"
                    FilterChip(
                        selected = isDamagedSelected,
                        onClick = { selectedConditionFilter = if (isDamagedSelected) "ALL" else "DAMAGED" },
                        label = { Text("Damaged ($countDamaged)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StatusWarning,
                            selectedLabelColor = Color.White,
                            containerColor = SurfaceLight,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isDamagedSelected,
                            borderColor = if (isDamagedSelected) StatusWarning else SurfaceBorder
                        )
                    )
                }
            }
        }

        // Inventory List
        if (filteredInventory.isEmpty()) {
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
                    Text("📦", fontSize = 42.sp)
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedLineFilter != "ALL") "No matching containers in yard" else "Yard Inventory is empty",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Register inbound containers using the Gate In Entry action.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = onGateInClick,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Gate In Entry", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 2.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredInventory, key = { it.id }) { container ->
                    InventoryContainerCard(
                        container = container,
                        settings = settings,
                        onClick = { onSelectContainer(container) },
                        onSlipClick = { onSlipClick(container) },
                        onPaymentClick = { onPaymentClick(container) },
                        onGateOutClick = { onGateOutClick(container) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryStatMiniCard(
    title: String,
    count: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceVariant,
    textColor: Color = PrimaryBlue
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = TextSecondary, maxLines = 1)
            Text(count, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            Text(subtitle, fontSize = 8.sp, color = TextMuted, maxLines = 1)
        }
    }
}

@Composable
private fun InventoryContainerCard(
    container: ContainerEntity,
    settings: CompanySettings,
    onClick: () -> Unit,
    onSlipClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onGateOutClick: () -> Unit
) {
    val calc = RentCalculation.calculate(container)
    val isDamaged = container.condition.contains("Damaged", ignoreCase = true) || container.condition.contains("Repair", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Container Number, Line Pill, Bay Slot
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

                // Bay Slot Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryContainer)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "📍 ${container.yardBay}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnPrimaryContainer
                    )
                }
            }

            // Row 2: Specs & Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = container.size,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isDamaged) StatusWarningBg else StatusSuccessBg)
                            .border(1.dp, if (isDamaged) StatusWarning.copy(alpha = 0.5f) else StatusSuccess.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = container.condition,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDamaged) StatusWarning else StatusSuccess
                        )
                    }
                }

                Text(
                    text = "${calc.totalDays} days in yard",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }

            // Row 3: Key Logistics Information Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundLight)
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Gate In: ${DateUtils.formatShortDate(container.gateInDate)}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                    if (container.sealNo.isNotBlank()) {
                        Text(
                            text = "Seal: ${container.sealNo}",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }
                if (container.transporter.isNotBlank() || container.truckNo.isNotBlank()) {
                    Text(
                        text = "Transporter: ${container.transporter} (${container.truckNo.ifBlank { "No Truck No" }})",
                        fontSize = 10.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (container.driverName.isNotBlank()) {
                    Text(
                        text = "Driver: ${container.driverName} | CNIC: ${container.driverCnic.ifBlank { "N/A" }}",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Row 4: Action Buttons
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
                    Text("EIR Slip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onPaymentClick,
                    modifier = Modifier.weight(1f).height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pay", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                    Text("Gate Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
