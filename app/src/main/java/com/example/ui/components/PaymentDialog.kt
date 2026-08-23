package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entity.ContainerEntity
import com.example.model.RentCalculation
import com.example.ui.theme.*
import com.example.ui.viewmodel.CompanySettings
import com.example.util.DateUtils

@Composable
fun PaymentDialog(
    container: ContainerEntity,
    settings: CompanySettings,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, method: String, ref: String, receivedBy: String, notes: String) -> Unit
) {
    val calc = RentCalculation.calculate(container)
    var amountText by remember { mutableStateOf(if (calc.pendingAmount > 0) "${calc.pendingAmount.toInt()}" else "0") }
    var paymentMethod by remember { mutableStateOf("CASH") }
    var referenceNo by remember { mutableStateOf("RCP-${System.currentTimeMillis() % 100000}") }
    var receivedBy by remember { mutableStateOf("Accounts Desk") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Record Rent Payment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                        Text("Container: ${container.containerNo}", fontSize = 12.sp, color = TextSecondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Balance summary box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceVariant)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Billed", fontSize = 10.sp, color = TextSecondary)
                            Text(DateUtils.formatCurrency(calc.grossTotal, settings.currencySymbol), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Paid So Far", fontSize = 10.sp, color = TextSecondary)
                            Text(DateUtils.formatCurrency(calc.paidAmount, settings.currencySymbol), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                        }
                        Column {
                            Text("Outstanding Due", fontSize = 10.sp, color = TextSecondary)
                            Text(DateUtils.formatCurrency(calc.pendingAmount, settings.currencySymbol), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = StatusError)
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payment Amount (${settings.currencySymbol}) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = referenceNo,
                        onValueChange = { referenceNo = it },
                        label = { Text("Receipt / Ref No") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = receivedBy,
                        onValueChange = { receivedBy = it },
                        label = { Text("Received By") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
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
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else TextPrimary
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Payment Notes / Remarks") },
                    placeholder = { Text("e.g. Month 1 & 2 rent cleared") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull()
                            if (amt == null || amt <= 0) {
                                errorMessage = "Please enter valid payment amount"
                                return@Button
                            }
                            onConfirm(amt, paymentMethod, referenceNo, receivedBy, notes)
                        },
                        modifier = Modifier.weight(1.3f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SAVE RECEIPT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
