package com.explosion.messenger.ui.screens.login

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.explosion.messenger.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Background Glow
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = 200.dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentGreen.copy(alpha = 0.1f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "NODE ACCESS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 4.sp
            )
            Text(
                text = "AUTHENTICATION REQUIRED",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AccentGreen,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "AuthStates"
            ) { state ->
                when (state) {
                    is AuthUiState.Requires2FA -> {
                        TwoFAContent(
                            username = state.username,
                            isPasswordless = state.isPasswordless,
                            isLoading = false,
                            onSubmit = { code -> viewModel.verify2fa(state.username, code, state.isPasswordless) }
                        )
                    }
                    else -> {
                        LoginForm(
                            isLoading = state is AuthUiState.Loading,
                            error = (state as? AuthUiState.Error)?.message,
                            onSubmit = { u, p, passwordless -> viewModel.login(u, p, passwordless) },
                            onRegisterClick = onRegisterClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginForm(
    isLoading: Boolean,
    error: String?,
    onSubmit: (String, String, Boolean) -> Unit,
    onRegisterClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordless by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (error != null) {
            Text(
                text = error,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("USERNAME", fontSize = 10.sp, fontWeight = FontWeight.Black) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BorderColor,
                focusedBorderColor = AccentGreen
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isPasswordless) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("PASSWORD", fontSize = 10.sp, fontWeight = FontWeight.Black) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = BorderColor,
                    focusedBorderColor = AccentGreen
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSubmit(username, password, isPasswordless) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isPasswordless) "REQUEST CODE" else "INITIALIZE",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        TextButton(onClick = { isPasswordless = !isPasswordless }) {
            Text(
                text = if (isPasswordless) "BACK TO PASSWORD" else "USE 2FA NEURAL BYPASS",
                fontSize = 10.sp,
                color = TextDim,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onRegisterClick) {
            Text(
                text = "SYSTEM ACCESS NOT GRANTED? CREATE ACCOUNT",
                fontSize = 10.sp,
                color = AccentGreen,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun TwoFAContent(
    username: String,
    isPasswordless: Boolean,
    isLoading: Boolean,
    onSubmit: (String) -> Unit
) {
    var otpCode by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "ENTER SECURE CODE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = TextDim,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        OutlinedTextField(
            value = otpCode,
            onValueChange = { otpCode = it },
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BorderColor,
                focusedBorderColor = AccentGreen
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 24.sp, letterSpacing = 8.sp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSubmit(otpCode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
            enabled = !isLoading
        ) {
            Text("VERIFY IDENTITY", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
    }
}
