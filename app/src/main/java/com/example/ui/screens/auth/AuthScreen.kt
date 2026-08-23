package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.UserEntity
import com.example.ui.theme.*

enum class AuthTab {
    LOGIN,
    SIGN_UP
}

@Composable
fun AuthScreen(
    onLogin: (credential: String, pass: String, rememberMe: Boolean) -> Unit,
    onSignUp: (username: String, fullName: String, email: String, pass: String, role: String, phone: String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onClearError: () -> Unit,
    savedLastUsername: String = ""
) {
    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }

    // Login Form State
    var loginCredential by remember { mutableStateOf(savedLastUsername.ifBlank { "admin" }) }
    var loginPassword by remember { mutableStateOf("password123") }
    var loginPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Sign Up Form State
    var regUsername by remember { mutableStateOf("") }
    var regFullName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    var regRole by remember { mutableStateOf("Yard Master") }
    var regPhone by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        // Decorative top subtle header backdrop
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            PrimaryBlue,
                            Color(0xFF1E3A8A),
                            BackgroundLight
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Brand Logo & Title
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(2.dp, PrimaryContainer, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsBoat,
                    contentDescription = "Depot Logo",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "CONTAINER DEPOT & RENTALS",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.2.sp
            )

            Text(
                text = "Secure Local Terminal Access & Yard Auth",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Container Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceLight),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.verticalGradient(
                        colors = listOf(SurfaceBorder, SurfaceBorder.copy(alpha = 0.4f))
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Modern Tab Switcher (Login vs Sign Up)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceVariant)
                            .padding(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            // Login Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        if (selectedTab == AuthTab.LOGIN) Color.White else Color.Transparent
                                    )
                                    .clickable {
                                        onClearError()
                                        selectedTab = AuthTab.LOGIN
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (selectedTab == AuthTab.LOGIN) PrimaryBlue else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Login / لاگ ان",
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTab == AuthTab.LOGIN) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == AuthTab.LOGIN) PrimaryBlue else TextSecondary
                                    )
                                }
                            }

                            // Sign Up Tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(11.dp))
                                    .background(
                                        if (selectedTab == AuthTab.SIGN_UP) Color.White else Color.Transparent
                                    )
                                    .clickable {
                                        onClearError()
                                        selectedTab = AuthTab.SIGN_UP
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = if (selectedTab == AuthTab.SIGN_UP) PrimaryBlue else TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Sign Up / رجسٹر",
                                        fontSize = 13.sp,
                                        fontWeight = if (selectedTab == AuthTab.SIGN_UP) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == AuthTab.SIGN_UP) PrimaryBlue else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Error Message Banner
                    AnimatedVisibility(visible = errorMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = StatusWarningBg,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusWarning.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusWarning, modifier = Modifier.size(18.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB71C1C),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = onClearError,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    // Success Message Banner
                    AnimatedVisibility(visible = successMessage != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = StatusSuccessBg,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(18.dp))
                                Text(
                                    text = successMessage ?: "",
                                    fontSize = 11.sp,
                                    color = StatusSuccess,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Form Fields Content with Animated Tab Transition
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            if (targetState == AuthTab.SIGN_UP) {
                                (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                            } else {
                                (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                            }
                        },
                        label = "AuthTabTransition"
                    ) { tab ->
                        if (tab == AuthTab.LOGIN) {
                            // --- LOGIN VIEW ---
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = loginCredential,
                                    onValueChange = {
                                        loginCredential = it
                                        onClearError()
                                    },
                                    label = { Text("Username or Email / یوزر نیم") },
                                    placeholder = { Text("e.g. admin or officer") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Person, contentDescription = null, tint = PrimaryBlue)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_username_input"),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                                )

                                OutlinedTextField(
                                    value = loginPassword,
                                    onValueChange = {
                                        loginPassword = it
                                        onClearError()
                                    },
                                    label = { Text("Password / پاس ورڈ") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = PrimaryBlue)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                            Icon(
                                                imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (loginPasswordVisible) "Hide password" else "Show password",
                                                tint = TextSecondary
                                            )
                                        }
                                    },
                                    visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("login_password_input"),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = {
                                        focusManager.clearFocus()
                                        onLogin(loginCredential, loginPassword, rememberMe)
                                    })
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                                    ) {
                                        Checkbox(
                                            checked = rememberMe,
                                            onCheckedChange = { rememberMe = it },
                                            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                                        )
                                        Text(
                                            text = "Remember Me (محفوظ رکھیں)",
                                            fontSize = 12.sp,
                                            color = TextPrimary
                                        )
                                    }

                                    Text(
                                        text = "Local Storage DB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        onLogin(loginCredential, loginPassword, rememberMe)
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("login_submit_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "SIGN IN TO YARD SYSTEM",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                // Quick Demo Accounts Selector (Single Tap Test)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "QUICK TEST CREDENTIALS (ایک کلک لاگ ان)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary,
                                    letterSpacing = 0.8.sp,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            loginCredential = "admin"
                                            loginPassword = "password123"
                                            onClearError()
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("👑 Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            loginCredential = "officer"
                                            loginPassword = "pass123"
                                            onClearError()
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("👮 Gate Officer", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            loginCredential = "yardmaster"
                                            loginPassword = "pass123"
                                            onClearError()
                                        },
                                        modifier = Modifier.weight(1f).height(38.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 6.dp)
                                    ) {
                                        Text("🏗️ Yard Master", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            // --- SIGN UP VIEW ---
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = regFullName,
                                    onValueChange = {
                                        regFullName = it
                                        onClearError()
                                    },
                                    label = { Text("Full Name (پورا نام) *") },
                                    placeholder = { Text("e.g. Hassan Baloch") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Badge, contentDescription = null, tint = PrimaryBlue)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_fullname_input"),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = regUsername,
                                        onValueChange = {
                                            regUsername = it
                                            onClearError()
                                        },
                                        label = { Text("Username *") },
                                        placeholder = { Text("e.g. hassan12") },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Person, contentDescription = null, tint = PrimaryBlue)
                                        },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("signup_username_input"),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                                    )

                                    OutlinedTextField(
                                        value = regPhone,
                                        onValueChange = {
                                            regPhone = it
                                            onClearError()
                                        },
                                        label = { Text("Cell / Phone") },
                                        placeholder = { Text("0300-1234567") },
                                        leadingIcon = {
                                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = PrimaryBlue)
                                        },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                                    )
                                }

                                OutlinedTextField(
                                    value = regEmail,
                                    onValueChange = {
                                        regEmail = it
                                        onClearError()
                                    },
                                    label = { Text("Email Address (اختیاری)") },
                                    placeholder = { Text("user@yard.pk") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Email, contentDescription = null, tint = PrimaryBlue)
                                    },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_email_input"),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                                )

                                OutlinedTextField(
                                    value = regPassword,
                                    onValueChange = {
                                        regPassword = it
                                        onClearError()
                                    },
                                    label = { Text("Password (پاس ورڈ) *") },
                                    leadingIcon = {
                                        Icon(Icons.Outlined.Lock, contentDescription = null, tint = PrimaryBlue)
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                            Icon(
                                                imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = if (regPasswordVisible) "Hide password" else "Show password",
                                                tint = TextSecondary
                                            )
                                        }
                                    },
                                    visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_password_input"),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {
                                        focusManager.clearFocus()
                                        onSignUp(regUsername, regFullName, regEmail, regPassword, regRole, regPhone)
                                    })
                                )

                                // Terminal Role Selector
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "ASSIGN YARD ROLE / ذمہ داری منتخب کریں:",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                    val roles = listOf("Yard Master", "Gate Incharge", "Billing Officer", "Admin")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        roles.forEach { roleOption ->
                                            val isSelected = regRole == roleOption
                                            Surface(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable { regRole = roleOption },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) PrimaryContainer else SurfaceVariant,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isSelected) PrimaryBlue else SurfaceBorder
                                                )
                                            ) {
                                                Text(
                                                    text = roleOption,
                                                    fontSize = 10.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) PrimaryBlue else TextPrimary,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        focusManager.clearFocus()
                                        onSignUp(regUsername, regFullName, regEmail, regPassword, regRole, regPhone)
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("signup_submit_button"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "CREATE ACCOUNT & SAVE TO DB",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Information: Offline Local Database
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SurfaceLight.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                    Text(
                        text = "100% Offline Local Storage • SQLite Room DB • Encrypted Session",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
