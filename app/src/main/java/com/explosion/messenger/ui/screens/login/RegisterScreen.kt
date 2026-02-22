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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.clickable
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.explosion.messenger.ui.theme.*

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(x = (-100).dp, y = (-100).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(AccentBlue.copy(alpha = 0.1f), Color.Transparent)
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
                text = "CREATE ACCESS",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 4.sp
            )
            Text(
                text = "SYSTEM REGISTRATION PROTOCOL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBlue,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "RegisterState"
            ) { state ->
                when (state) {
                    is RegisterUiState.Requires2FAConfirm -> {
                        Register2FAContent(
                            secret = state.secret,
                            otpAuthUrl = state.otpAuthUrl,
                            isLoading = false,
                            error = null,
                            onSubmit = { code -> viewModel.confirmRegistration(state, code) }
                        )
                    }
                    else -> {
                        RegisterForm(
                            isLoading = state is RegisterUiState.Loading,
                            error = (state as? RegisterUiState.Error)?.message,
                            onSubmit = { u, e, p -> viewModel.setupRegistration(u, e, p) },
                            onBackClick = onBack
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RegisterForm(
    isLoading: Boolean,
    error: String?,
    onSubmit: (String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                focusedBorderColor = AccentBlue
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("EMAIL (OPTIONAL)", fontSize = 10.sp, fontWeight = FontWeight.Black) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BorderColor,
                focusedBorderColor = AccentBlue
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("PASSWORD", fontSize = 10.sp, fontWeight = FontWeight.Black) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BorderColor,
                focusedBorderColor = AccentBlue
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onSubmit(username, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "GENERATE 2FA KEY",
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onBackClick) {
            Text(
                text = "ALREADY HAVE ACCESS? REVERT TO LOGIN",
                fontSize = 10.sp,
                color = TextDim,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun Register2FAContent(
    secret: String,
    otpAuthUrl: String,
    isLoading: Boolean,
    error: String?,
    onSubmit: (String) -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (error != null) {
            Text(
                text = error,
                color = ErrorRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    
        Text(
            text = "SAVE THIS SECRET IN AUTHENTICATOR APP",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = TextDim,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = secret,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 4.sp,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .clickable {
                    clipboardManager.setText(AnnotatedString(secret))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                }
        )

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(otpAuthUrl))
                context.startActivity(intent)
            },
            modifier = Modifier
                .padding(bottom = 24.dp)
                .height(40.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BgSidebar)
        ) {
            Text("IMPORT INTO 2FA APP", color = AccentBlue, fontWeight = FontWeight.Black, fontSize = 10.sp, letterSpacing = 1.sp)
        }

        Text(
            text = "ENTER 6-DIGIT CODE TO VERIFY",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = TextDim,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        OutlinedTextField(
            value = otpCode,
            onValueChange = { otpCode = it },
            modifier = Modifier.width(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = BorderColor,
                focusedBorderColor = AccentBlue
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
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            enabled = !isLoading
        ) {
            Text("FINALIZE REGISTRATION", fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
    }
}
