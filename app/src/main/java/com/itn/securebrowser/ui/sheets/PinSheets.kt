package com.itn.securebrowser.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    val items = buildList {
        add(
            SheetItem.Row(
                icon = null,
                title = "PIN",
                onClick = {
                    if (hasPin) pushVerifyThenClear()
                    else push(setPinEntry(context) { hasPin = PinManager.hasPin(context) })
                },
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
        )
        add(SheetItem.Divider)
        if (hasPin) {
            add(
                SheetItem.Row(
                    icon = null,
                    title = stringResource(R.string.btn_change_pin),
                    onClick = {
                        push(
                            Sheet.PinEntry(
                                mode = MODE_VERIFY,
                                subtitle = context.getString(R.string.pin_verify_first),
                                onVerified = {
                                    push(
                                        setPinEntry(context) { hasPin = PinManager.hasPin(context) }
                                    )
                                }
                            )
                        )
                    }
                )
            )
            add(SheetItem.Divider)
            add(
                SheetItem.Row(
                    icon = null,
                    title = stringResource(R.string.btn_clear_pin),
                    onClick = { pushVerifyThenClear() }
                )
            )
        }
    }

    SheetScaffold(
        title = "PIN",
        onClose = dismiss,
        items = items
    )
}

private fun setPinEntry(context: android.content.Context, onVerified: () -> Unit) =
    Sheet.PinEntry(
        mode = MODE_SET,
        subtitle = context.getString(R.string.pin_subtitle_new),
        onVerified = onVerified
    )

@Composable
fun PinEntrySheet(
    sheet: Sheet.PinEntry,
    dismiss: () -> Unit,
    onDismissed: () -> Unit = sheet.onDismissed
) {
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

    val pinFocusRequester = remember { FocusRequester() }

    val items = buildList {
        add(SheetItem.InfoBlock {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (subtitleText.isNotEmpty()) {
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 24.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(6) { index ->
                        Box(
                            Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < entered.length) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        })
        add(SheetItem.Divider)
        add(
            SheetItem.TextField(
                label = "",
                value = entered,
                onValueChange = { new ->
                    if (new.length <= 6 && new.all { it.isDigit() }) {
                        entered = new
                        errorText = ""
                        showSuccessSpacer = true
                        if (new.length == 6) handleComplete(new)
                    }
                },
                placeholder = "● ● ● ● ● ●",
                keyboardType = KeyboardType.NumberPassword,
                isPassword = true,
                focusRequester = pinFocusRequester
            )
        )
    }

    SheetScaffold(title = titleText, onClose = dismiss, items = items)
}
