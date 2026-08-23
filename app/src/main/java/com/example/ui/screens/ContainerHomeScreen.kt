package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ContainerEntity
import com.example.model.RentCalculation
import com.example.ui.components.*
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerFilter
import com.example.ui.viewmodel.ContainerViewModel
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContainerHomeScreen(
    viewModel: ContainerViewModel
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val authError by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val authSuccess by viewModel.authSuccessMessage.collectAsStateWithLifecycle()
    val lastSavedUsername by viewModel.lastSavedUsername.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    // If not logged in, display the Auth Screen (Login / Sign Up)
    if (currentUser == null) {
        AuthScreen(
            onLogin = { cred, pass, rememberMe ->
                viewModel.login(cred, pass, rememberMe)
            },
            onSignUp = { username, fullName, email, pass, role, phone ->
                viewModel.signUp(username, fullName, email, pass, role, phone)
            },
            isLoading = isAuthLoading,
            errorMessage = authError,
            successMessage = authSuccess,
            onClearError = { viewModel.clearAuthError() },
            savedLastUsername = lastSavedUsername
        )
        return
    }

    val containers by viewModel.filteredContainers.collectAsStateWithLifecycle()
    val allContainersList by viewModel.allContainers.collectAsStateWithLifecycle()
    val stats by viewModel.yardStats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    var showGateInDialog by remember { mutableStateOf(false) }
    var containerToEdit by remember { mutableStateOf<ContainerEntity?>(null) }
    var showGateOutSelectDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showClearAllConfirm by remember { mutableStateOf(false) }
    var selectedForGateOut by remember { mutableStateOf<ContainerEntity?>(null) }
    var selectedForPayment by remember { mutableStateOf<ContainerEntity?>(null) }
    var selectedForSlip by remember { mutableStateOf<ContainerEntity?>(null) }
    var selectedForDetail by remember { mutableStateOf<ContainerEntity?>(null) }

    // Clear All Confirmation Dialog
    if (showClearAllConfirm) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirm = false },
            title = {
                Text(
                    text = "Clear All Containers / تمام ریکارڈ ختم کریں",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusError
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all containers and start with a completely empty database?",
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearAllConfirm = false
                        viewModel.deleteAllContainers()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("DELETE ALL / سب ختم کریں", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showClearAllConfirm = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("CANCEL", fontSize = 11.sp)
                }
            }
        )
    }

    // User Profile Dialog
    if (showProfileDialog && currentUser != null) {
        UserProfileDialog(
            user = currentUser!!,
            allUsers = allUsers,
            onDismiss = { showProfileDialog = false },
            onLogout = {
                viewModel.logout()
                showProfileDialog = false
            }
        )
    }

    // Dialogs
    if (showGateInDialog) {
        GateInDialog(
            containerToEdit = containerToEdit,
            settings = settings,
            onDismiss = {
                showGateInDialog = false
                containerToEdit = null
            },
            onSave = { entity ->
                if (containerToEdit != null) {
                    viewModel.updateContainer(entity)
                } else {
                    viewModel.addContainer(entity)
                }
                showGateInDialog = false
                containerToEdit = null
            }
        )
    }

    if (showGateOutSelectDialog) {
        GateOutSelectionDialog(
            containers = allContainersList,
            settings = settings,
            onDismiss = { showGateOutSelectDialog = false },
            onContainerSelected = { container ->
                selectedForGateOut = container
            },
            onNewOutwardDispatchClick = {
                showGateInDialog = true
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            currentSettings = settings,
            onDismiss = { showSettingsDialog = false },
            onSave = { newSettings ->
                viewModel.updateSettings(newSettings)
                showSettingsDialog = false
            }
        )
    }

    if (selectedForGateOut != null) {
        GateOutDialog(
            container = selectedForGateOut!!,
            settings = settings,
            onDismiss = { selectedForGateOut = null },
            onGateOutConfirmed = { outDate, destType, millNm, millLoc, payAmt, payMethod, officer, dName, dCnic, dCell, trans, truck, rem ->
                viewModel.gateOutAndClear(
                    containerId = selectedForGateOut!!.id,
                    outDate = outDate,
                    paymentAmount = payAmt,
                    paymentMethod = payMethod,
                    clearedBy = officer,
                    driverName = dName,
                    driverCnic = dCnic,
                    driverCell = dCell,
                    transporter = trans,
                    truckNo = truck,
                    destinationType = destType,
                    millName = millNm,
                    millLocation = millLoc,
                    remarks = rem,
                    onComplete = {
                        selectedForGateOut = null
                    }
                )
            }
        )
    }

    if (selectedForPayment != null) {
        PaymentDialog(
            container = selectedForPayment!!,
            settings = settings,
            onDismiss = { selectedForPayment = null },
            onConfirm = { amount, method, ref, recBy, notes ->
                viewModel.recordPayment(
                    containerId = selectedForPayment!!.id,
                    amount = amount,
                    method = method,
                    ref = ref,
                    receivedBy = recBy,
                    notes = notes
                )
                selectedForPayment = null
            }
        )
    }

    if (selectedForSlip != null) {
        val calc = RentCalculation.calculate(selectedForSlip!!)
        SlipPreviewDialog(
            container = selectedForSlip!!,
            calc = calc,
            settings = settings,
            isGateOut = selectedForSlip!!.status == "GATED_OUT",
            onDismiss = { selectedForSlip = null }
        )
    }

    if (selectedForDetail != null) {
        ContainerDetailDialog(
            container = selectedForDetail!!,
            settings = settings,
            onDismiss = { selectedForDetail = null },
            onEditClick = {
                val c = selectedForDetail
                selectedForDetail = null
                containerToEdit = c
                showGateInDialog = true
            },
            onGateOutClick = {
                val c = selectedForDetail
                selectedForDetail = null
                selectedForGateOut = c
            },
            onReturnToYardClick = { yardBay ->
                val id = selectedForDetail!!.id
                viewModel.returnToYard(id, yardBay)
                selectedForDetail = null
            },
            onAddPaymentClick = {
                val c = selectedForDetail
                selectedForDetail = null
                selectedForPayment = c
            },
            onDeleteClick = {
                viewModel.deleteContainer(selectedForDetail!!.id)
                selectedForDetail = null
            }
        )
    }

    Scaffold(
        containerColor = BackgroundLight,
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Gate Out Action
                FloatingActionButton(
                    onClick = { showGateOutSelectDialog = true },
                    containerColor = StatusWarning,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocalShipping, contentDescription = "Gate Out Dispatch", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gate Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Quick Gate In Action
                FloatingActionButton(
                    onClick = { showGateInDialog = true },
                    containerColor = PrimaryBlue,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "New Gate In", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gate In", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Bar (Clean Minimalism Style)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚢", fontSize = 20.sp)
                    }
                    Column {
                        Text(
                            text = "Container Rental",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Depot Management & Clearance",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logged in User Profile Chip
                    Surface(
                        onClick = { showProfileDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        color = PrimaryContainer,
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser!!.fullName.take(1).uppercase(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = currentUser!!.fullName.split(" ").firstOrNull() ?: currentUser!!.username,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnPrimaryContainer
                                )
                                Text(
                                    text = currentUser!!.role,
                                    fontSize = 9.sp,
                                    color = PrimaryBlue,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Clear All / Delete Database Button
                    if (allContainersList.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearAllConfirm = true },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusErrorBg)
                        ) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear All Containers",
                                tint = StatusError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceVariant)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Stats Card (Clean Minimalism Dashboard Panel)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
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
                                text = "TOTAL PENDING RECEIVABLES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = DateUtils.formatCurrency(stats.totalPendingReceivables, settings.currencySymbol),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (stats.totalPendingReceivables > 0) StatusError else StatusSuccess
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .border(1.dp, PrimaryContainer, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("IN YARD", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("${stats.inYardCount}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlue)
                            }
                        }
                    }

                    Divider(color = SurfaceBorder.copy(alpha = 0.6f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatMiniItem("Total Registered", "${stats.totalContainers}")
                        StatMiniItem("Gated Out / Cleared", "${stats.gatedOutCount}")
                        StatMiniItem("Revenue Collected", DateUtils.formatCurrency(stats.totalRevenueCollected, settings.currencySymbol), isPositive = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Dual Operations Bar (Gate In & Gate Out)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Gate In Button
                Surface(
                    onClick = { showGateInDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = PrimaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text(
                                text = "GATE IN (آمد)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryContainer
                            )
                            Text(
                                text = "New Entry / Yard Storage",
                                fontSize = 10.sp,
                                color = PrimaryBlue
                            )
                        }
                    }
                }

                // Gate Out Button
                Surface(
                    onClick = { showGateOutSelectDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = StatusWarningBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusWarning.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StatusWarning),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "GATE OUT (روانگی)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusWarning
                            )
                            Text(
                                text = "Vehicle / Mill Dispatch",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search Container, CNIC, Transporter, NOC...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight,
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = SurfaceBorder
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    ContainerFilter.ALL to "All (${stats.totalContainers})",
                    ContainerFilter.IN_YARD to "In Yard (${stats.inYardCount})",
                    ContainerFilter.ON_VEHICLE to "On Vehicle 🚛 (${stats.onVehicleCount})",
                    ContainerFilter.AT_MILL to "At Mill 🏭 (${stats.atMillCount})",
                    ContainerFilter.PENDING_RENT to "Pending Dues",
                    ContainerFilter.GATED_OUT to "Gated Out (${stats.gatedOutCount})",
                    ContainerFilter.DAMAGED to "Damaged"
                )

                items(filters) { (filter, title) ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
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

            Spacer(modifier = Modifier.height(6.dp))

            // Container Cards List
            if (containers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("📦", fontSize = 48.sp)
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching containers found" else "No containers registered yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tap 'Gate In Entry' below to record a container into the yard.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(containers, key = { it.id }) { container ->
                        ContainerCard(
                            container = container,
                            settings = settings,
                            onClick = { selectedForDetail = container },
                            onEditClick = {
                                containerToEdit = container
                                showGateInDialog = true
                            },
                            onGateOutClick = { selectedForGateOut = container },
                            onPaymentClick = { selectedForPayment = container },
                            onSlipClick = { selectedForSlip = container }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniItem(label: String, value: String, isPositive: Boolean = false) {
    Column {
        Text(label, fontSize = 9.sp, color = TextSecondary)
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) StatusSuccess else TextPrimary
        )
    }
}
