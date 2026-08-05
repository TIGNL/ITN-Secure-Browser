package com.itn.securebrowser.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.itn.securebrowser.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    activity: com.itn.securebrowser.MainActivity
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var isFocused by remember { mutableStateOf(false) }

    val onNavigate: (String) -> Unit = { input ->
        keyboardController?.hide()
        focusManager.clearFocus()
        activity.navigateTo(input)
    }

    // Reflect external URL updates from WebView
    LaunchedEffect(activity.url) {
        if (!isFocused && textState.text != activity.url) {
            textState = TextFieldValue(activity.url)
        }
    }

    // Request focus when webview blank page tapped
    LaunchedEffect(activity.focusRequestToken) {
        if (activity.focusRequestToken > 0) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Column(Modifier.fillMaxSize()) {
        // ── Top bar ──
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textState,
                    onValueChange = { textState = it },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            val wasFocused = isFocused
                            isFocused = state.isFocused
                            if (state.isFocused && !wasFocused) {
                                textState = textState.copy(selection = TextRange(0, textState.text.length))
                            }
                        },
                    placeholder = { Text(stringResource(R.string.hint_search)) },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            onNavigate(textState.text.trim())
                            textState = textState.copy(selection = TextRange(0, 0))
                        }
                    )
                )

                IconButton(onClick = { activity.toggleRefreshStop() }) {
                    Icon(
                        imageVector = if (activity.isLoading) Icons.Filled.Close else Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.cd_refresh_stop)
                    )
                }
            }
        }

        // ── Progress bar ──
        if (activity.isLoading) {
            LinearProgressIndicator(
                progress = { activity.progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
            )
        }

        // ── WebView container ──
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopStart
        ) {
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { activity.webViewContainer },
                modifier = Modifier.fillMaxSize()
            )
        }

        // ── Bottom navigation bar ──
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    enabled = activity.canGoBack(),
                    onClick = { activity.goBack() }
                )
                NavButton(
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.cd_forward),
                    enabled = activity.canGoForward(),
                    onClick = { activity.goForward() }
                )
                NavButton(
                    icon = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.cd_home),
                    enabled = true,
                    onClick = { activity.goHome() }
                )
                NavButton(
                    icon = Icons.Filled.Tab,
                    contentDescription = stringResource(R.string.cd_tabs),
                    enabled = true,
                    onClick = { activity.showTabs() }
                )
                NavButton(
                    icon = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.cd_more),
                    enabled = true,
                    onClick = { activity.showMore() }
                )
            }
        }
    }
}

@Composable
private fun NavButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.35f),
            modifier = Modifier.size(24.dp)
        )
    }
}
