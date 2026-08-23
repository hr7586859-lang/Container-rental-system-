package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.CompanySettings

@Composable
fun SettingsDialog(
    currentSettings: CompanySettings,
    onDismiss: () -> Unit,
    onSave: (CompanySettings) -> Unit
) {
    var companyName by remember { mutableStateOf(currentSettings.companyName) }
    var yardAddress by remember { mutableStateOf(currentSettings.yardAddress) }
    var contactInfo by remember { mutableStateOf(currentSettings.contactInfo) }
    var currencySymbol by remember { mutableStateOf(currentSettings.currencySymbol) }

    var daily20ft by remember { mutableStateOf("${currentSettings.defaultDaily20ft.toInt()}") }
    var daily40ft by remember { mutableStateOf("${currentSettings.defaultDaily40ft.toInt()}") }
    var handling by remember { mutableStateOf("${currentSettings.defaultHandling.toInt()}") }
    var freeDays by remember { mutableStateOf("${currentSettings.defaultFreeDays}") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Depot & Tariff Setup", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrimaryBlue)
                        Text("Default charges, company slip header & rules", fontSize = 11.sp, color = TextSecondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Company / Terminal Details for Slips", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Depot / Company Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = yardAddress,
                            onValueChange = { yardAddress = it },
                            label = { Text("Yard Address / Location") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = contactInfo,
                                onValueChange = { contactInfo = it },
                                label = { Text("Contact Phone / Email") },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = currencySymbol,
                                onValueChange = { currencySymbol = it },
                                label = { Text("Currency (e.g. Rs. / PKR)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    item {
                        Text("Default Tariff & Charges Configuration", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = daily20ft,
                                onValueChange = { daily20ft = it },
                                label = { Text("20ft Daily Rate") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = daily40ft,
                                onValueChange = { daily40ft = it },
                                label = { Text("40ft Daily Rate") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = handling,
                                onValueChange = { handling = it },
                                label = { Text("Handling / LOLO Fee") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            OutlinedTextField(
                                value = freeDays,
                                onValueChange = { freeDays = it },
                                label = { Text("Default Free Days") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val newS = CompanySettings(
                                companyName = companyName.trim(),
                                yardAddress = yardAddress.trim(),
                                contactInfo = contactInfo.trim(),
                                currencySymbol = currencySymbol.trim(),
                                defaultDaily20ft = daily20ft.toDoubleOrNull() ?: 600.0,
                                defaultDaily40ft = daily40ft.toDoubleOrNull() ?: 1000.0,
                                defaultHandling = handling.toDoubleOrNull() ?: 2500.0,
                                defaultFreeDays = freeDays.toIntOrNull() ?: 0
                            )
                            onSave(newS)
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SAVE SETUP", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
