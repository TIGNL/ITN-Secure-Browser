package com.itn.securebrowser.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.itn.securebrowser.pinEntryRoute
import com.itn.securebrowser.util.PinManager
import kotlinx.coroutines.launch

@Composable
fun PinEntryScreen(
    navController: NavController,
    mode: String,
    subtitle: String,
    next: String,
    pinLength: Int,
    lockType: String,
    firstPin: String,
    changeAction: String
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val input = remember { mutableStateOf("") }

    val title = when (mode) {
        "set" -> "Enter new PIN"
        "confirm" -> "Re-enter to confirm"
        else -> "Enter PIN"
    }
    val supporting = when {
        mode == "verify" && subtitle.isNotBlank() -> subtitle
        mode == "verify" -> "Verify your identity first"
        subtitle.isNotBlank() -> subtitle
        else -> ""
    }

    Scaffold(
        topBar = { ScreenTopBar(title, onBack = { navController.popBackStack() }) },
        bottomBar = {
            PinnedBottomBar {
                PrimaryButton(
                    text = "OK",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val pin = input.value
                        when (mode) {
                            "verify" -> {
                                if (PinManager.verify(context, pin)) {
                                    when {
                                        next == "settings" -> navController.navigate("settings")
                                        next == "manage_space" -> activity?.finish()
                                        changeAction == "disable" -> {
                                            PinManager.clear(context)
                                            navController.popBackStack("settings", false)
                                        }
                                        changeAction == "setup" -> {
                                            navController.navigate("pin_setup")
                                        }
                                        else -> navController.popBackStack()
                                    }
                                } else {
                                    input.value = ""
                                    scope.launch { snackbar.showSnackbar("Wrong PIN, try again") }
                                }
                            }

                            "set" -> {
                                navController.navigate(
                                    pinEntryRoute(
                                        mode = "confirm",
                                        pinLength = pinLength,
                                        lockType = lockType,
                                        firstPin = pin,
                                        next = next,
                                        changeAction = changeAction
                                    )
                                )
                            }

                            "confirm" -> {
                                if (pin == firstPin) {
                                    PinManager.savePin(context, pin)
                                    navController.popBackStack("settings", false)
                                } else {
                                    input.value = ""
                                    scope.launch { snackbar.showSnackbar("PINs don't match, start over") }
                                }
                            }
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (supporting.isNotBlank()) {
                Text(
                    text = supporting,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
            }
            OutlinedTextField(
                value = input.value,
                onValueChange = {
                    if (pinLength <= 0 || it.length <= pinLength) input.value = it
                },
                label = { Text(if (pinLength > 0) "$pinLength-digit PIN" else "Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (pinLength > 0) KeyboardType.NumberPassword else KeyboardType.Password
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
