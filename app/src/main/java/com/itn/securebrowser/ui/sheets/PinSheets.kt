package com.itn.securebrowser.ui.sheets

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.itn.securebrowser.PinManager
import com.itn.securebrowser.R

@Composable
fun PinSheet(dismiss: () -> Unit, push: (Sheet) -> Unit) {
    val context = LocalContext.current
    var hasPin by remember { mutableStateOf(PinManager.hasPin(context)) }

    fun pushVerifyThenClear() {
        push(
            Sheet.PinEntry(
                mode = MODE_VERIFY,
                subtitle = context.getString(R.string.pin_verify_first),
                onVerified = {
                    PinManager.clear(context)
                    hasPin = false
                },
                onDismissed = { hasPin = PinManager.hasPin(context) }
            )
        )
    }

    SheetScaffold(title = "PIN", onClose = dismiss) {
        SheetRow(
            icon = null,
            title = "PIN",
            onClick = { if (hasPin) pushVerifyThenClear() else push(setPinEntry(context) { hasPin = PinManager.hasPin(context) }) },
            trailing = {
                Switch(
                    checked = hasPin,
                    onCheckedChange = { enabled ->
                        if (enabled) push(setPinEntry(context) { hasPin = PinManager.hasPin(context) })
                        else pushVerifyThenClear()
                    }
                )
            }
        )
        SheetDivider()

        if (hasPin) {
            SheetRow(icon = null, title = stringResource(R.string.btn_change_pin), onClick = {
                push(
                    Sheet.PinEntry(
                        mode = MODE_VERIFY,
                        subtitle = context.getString(R.string.pin_verify_first),
                        onVerified = {
                            push(setPinEntry(context) { hasPin = PinManager.hasPin(context) })
                        }
                    )
                )
            })
            SheetDivider()
            SheetRow(icon = null, title = stringResource(R.string.btn_clear_pin), onClick = { pushVerifyThenClear() })
        }
    }
}

private fun setPinEntry(context: android.content.Context, onVerified: () -> Unit) = Sheet.PinEntry(
    mode = MODE_SET,
    subtitle = context.getString(R.string.pin_subtitle_new),
    onVerified = onVerified
)

@Composable
fun PinEntrySheet(sheet: Sheet.PinEntry, dismiss: () -> Unit, onDismissed: () -> Unit = sheet.onDismissed) {
    val context = LocalContext.current
    var entered by remember { mutableStateOf("") }
    var firstPin by remember { mutableStateOf("") }
    var titleText by remember {
        mutableStateOf(
            when (sheet.mode) {
                MODE_SET -> context.getString(R.string.pin_title_new)
                else -> context.getString(R.string.pin_title_enter)
            }
        )
    }
    var subtitleText by remember { mutableStateOf(sheet.subtitle) }
    var errorText by remember { mutableStateOf("") }
    var showSuccessSpacer by remember { mutableStateOf(true) }
    var pinSucceeded by remember { mutableStateOf(false) }

    // Call onDismissed when disposed without success (user swiped away / pressed back)
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { if (!pinSucceeded) onDismissed() }
    }

    fun showError(msg: String) {
        errorText = msg
        showSuccessSpacer = false
    }

    fun shakeAndClear() {
        entered = ""
    }

    fun handleComplete(pin: String) {
        when (sheet.mode) {
            MODE_VERIFY -> {
                if (PinManager.verify(context, pin)) {
                    pinSucceeded = true
                    sheet.onVerified()
                    dismiss()
                } else {
                    showError(context.getString(R.string.pin_error_wrong))
                    shakeAndClear()
                }
            }
            MODE_SET -> {
                if (firstPin.isEmpty()) {
                    firstPin = pin
                    entered = ""
                    titleText = context.getString(R.string.pin_title_confirm)
                    subtitleText = ""
                } else {
                    if (pin == firstPin) {
                        pinSucceeded = true
                        PinManager.savePin(context, pin)
                        sheet.onVerified()
                        dismiss()
                    } else {
                        firstPin = ""
                        showError(context.getString(R.string.pin_error_mismatch))
                        titleText = context.getString(R.string.pin_title_new)
                        subtitleText = ""
                        shakeAndClear()
                    }
                }
            }
        }
    }

    fun pressDigit(digit: Char) {
        if (entered.length >= 6) return
        entered += digit
        errorText = ""
        showSuccessSpacer = true
        if (entered.length == 6) handleComplete(entered)
    }

    fun pressBackspace() {
        if (entered.isEmpty()) return
        entered = entered.dropLast(1)
        errorText = ""
        showSuccessSpacer = true
    }

    SheetScaffold(title = titleText, onClose = dismiss) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Subtitle
            if (subtitleText.isNotEmpty()) {
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 36.dp)
                )
            }

            // PIN dots
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                repeat(6) { index ->
                    Box(
                        Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < entered.length)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Error or spacer
            if (errorText.isNotEmpty()) {
                Text(
                    text = errorText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 32.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(Modifier.height(32.dp))
            }

            // Numpad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("cancel", "0", "back")
            )

            keys.forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    row.forEach { key ->
                        when (key) {
                            "cancel" -> {
                                IconButton(onClick = { dismiss() }, modifier = Modifier.size(80.dp)) {
                                    Text("✕", fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            "back" -> {
                                IconButton(onClick = { pressBackspace() }, modifier = Modifier.size(80.dp)) {
                                    Icon(Icons.Filled.Backspace, contentDescription = "Backspace", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
                                }
                            }
                            else -> {
                                Box(
                                    Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { pressDigit(key[0]) }
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(key, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
