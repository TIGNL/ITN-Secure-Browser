package com.itn.securebrowser.ui

import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.itn.securebrowser.BrowserSession
import com.itn.securebrowser.DesignVariant
import com.itn.securebrowser.LocalDesignVariant
import com.itn.securebrowser.pinEntryRoute
import com.itn.securebrowser.util.PinManager
import kotlinx.coroutines.delay

@Composable
fun BrowserScreen(navController: NavController) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    DisposableEffect(Unit) {
        BrowserSession.onBlankTap = { focusRequester.requestFocus() }
        BrowserSession.onAppResumed()
        onDispose {
            BrowserSession.onBlankTap = null
            BrowserSession.onAppPaused()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            BrowserSession.periodicBlockCheck()
        }
    }

    BackHandler {
        val view = BrowserSession.currentWebView()
        if (view?.canGoBack() == true) BrowserSession.goBack() else navController.popBackStack()
    }

    Scaffold(
        topBar = { BrowserTopBar(navController, focusRequester) },
        bottomBar = { BrowserBottomBar(navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val tab = BrowserSession.currentTab()
            if (tab != null) {
                key(tab.id) {
                    AndroidView(
                        factory = { ctx ->
                            (tab.webView.parent as? ViewGroup)?.removeView(tab.webView)
                            tab.webView.layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            tab.webView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            if (BrowserSession.isLoading.value) {
                LinearProgressIndicator(
                    progress = { BrowserSession.progress.value / 100f },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(2.dp)
                )
            }
        }
    }
}

@Composable
private fun BrowserTopBar(navController: NavController, focusRequester: FocusRequester) {
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var focused by remember { mutableStateOf(false) }

    val submit: () -> Unit = {
        BrowserSession.navigateTo(BrowserSession.urlText.value)
        focusManager.clearFocus()
        keyboard?.hide()
    }

    val urlField: @Composable () -> Unit = {
        OutlinedTextField(
            value = BrowserSession.urlText.value,
            onValueChange = { BrowserSession.urlText.value = it },
            modifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged { state ->
                    focused = state.isFocused
                    BrowserSession.urlEditing = state.isFocused
                    if (state.isFocused) BrowserSession.urlText.value = BrowserSession.currentWebView()?.url ?: ""
                },
            singleLine = true,
            shape = CircleShape,
            placeholder = { Text("Search or enter URL") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { submit() }),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp)
        )
    }

    when (LocalDesignVariant.current) {
        DesignVariant.FULL -> Surface(tonalElevation = 3.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { BrowserSession.createNewTab(context) }) {
                    Icon(Icons.Default.Home, contentDescription = "New tab")
                }
                Box(modifier = Modifier.weight(1f)) { urlField() }
                IconButton(onClick = { BrowserSession.reloadOrStop() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }

        DesignVariant.FAITHFUL -> Surface {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { BrowserSession.createNewTab(context) },
                    modifier = Modifier.weight(0.16f)
                ) { Text("Home", fontSize = 12.sp) }
                Box(modifier = Modifier.weight(0.68f)) { urlField() }
                TextButton(
                    onClick = { BrowserSession.reloadOrStop() },
                    modifier = Modifier.weight(0.16f)
                ) {
                    Text(
                        text = if (BrowserSession.isLoading.value) "Stop" else "Refresh",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun BrowserBottomBar(navController: NavController) {
    val context = LocalContext.current
    val openSettings = {
        if (PinManager.hasPin(context)) {
            navController.navigate(
                pinEntryRoute(
                    mode = "verify",
                    subtitle = "To open settings",
                    next = "settings"
                )
            )
        } else {
            navController.navigate("settings")
        }
    }

    when (LocalDesignVariant.current) {
        DesignVariant.FULL -> NavigationBar {
            NavigationBarItem(
                selected = false,
                onClick = { BrowserSession.goBack() },
                enabled = BrowserSession.canGoBack.value,
                icon = { Icon(Icons.Default.ArrowBack, contentDescription = "Back") },
                label = { Text("Back") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { BrowserSession.goForward() },
                enabled = BrowserSession.canGoForward.value,
                icon = { Icon(Icons.Default.ArrowForward, contentDescription = "Forward") },
                label = { Text("Forward") }
            )
            NavigationBarItem(
                selected = false,
                onClick = { navController.navigate("tabs") },
                icon = { Icon(Icons.Default.List, contentDescription = "Tabs") },
                label = { Text("Tabs") }
            )
            NavigationBarItem(
                selected = false,
                onClick = openSettings,
                icon = { Icon(Icons.Default.Settings, contentDescription = "More") },
                label = { Text("More") }
            )
        }

        DesignVariant.FAITHFUL -> Surface {
            Row(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                TextButton(
                    onClick = { BrowserSession.goBack() },
                    enabled = BrowserSession.canGoBack.value,
                    modifier = Modifier.weight(1f)
                ) { Text("Back", fontSize = 12.sp) }
                TextButton(
                    onClick = { BrowserSession.goForward() },
                    enabled = BrowserSession.canGoForward.value,
                    modifier = Modifier.weight(1f)
                ) { Text("Forward", fontSize = 12.sp) }
                TextButton(
                    onClick = { navController.navigate("tabs") },
                    modifier = Modifier.weight(1f)
                ) { Text("Tabs", fontSize = 12.sp) }
                TextButton(
                    onClick = openSettings,
                    modifier = Modifier.weight(1f)
                ) { Text("More", fontSize = 12.sp) }
            }
        }
    }
}
