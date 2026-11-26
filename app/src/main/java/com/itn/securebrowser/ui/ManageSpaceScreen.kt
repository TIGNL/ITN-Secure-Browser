package com.itn.securebrowser.ui

import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.itn.securebrowser.util.PinManager
import java.io.File

@Composable
fun ManageSpaceScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val confirmClearAll = remember { mutableStateOf(false) }

    ScreenBody(title = "Manage App Data", onBack = { activity?.finish() ?: navController.popBackStack() }) {
        SettingRow(title = "Clear cookies and cache", onClick = {
            clearBrowsing(context)
            showToast(context, "Data cleared")
        })
        SettingRow(title = "Clear time tracking data", onClick = {
            context.getSharedPreferences("itn_time_tracker", android.content.Context.MODE_PRIVATE)
                .edit().clear().apply()
            showToast(context, "Data cleared")
        })
        SettingRow(title = "Clear blocked sites and schedules", onClick = {
            context.getSharedPreferences("itn_block_data", android.content.Context.MODE_PRIVATE)
                .edit().clear().apply()
            showToast(context, "Data cleared")
        })
        SettingRow(title = "Clear everything", onClick = { confirmClearAll.value = true })
    }

    if (confirmClearAll.value) {
        ConfirmDialog(
            message = "This will delete all data including PIN. Continue?",
            onConfirm = {
                clearBrowsing(context)
                context.getSharedPreferences("itn_time_tracker", android.content.Context.MODE_PRIVATE)
                    .edit().clear().apply()
                context.getSharedPreferences("itn_block_data", android.content.Context.MODE_PRIVATE)
                    .edit().clear().apply()
                PinManager.clear(context)
                showToast(context, "Data cleared")
                confirmClearAll.value = false
                activity?.finish() ?: navController.popBackStack()
            },
            onDismiss = { confirmClearAll.value = false }
        )
    }
}

private fun clearBrowsing(context: android.content.Context) {
    CookieManager.getInstance().removeAllCookies(null)
    WebStorage.getInstance().deleteAllData()
    deleteCache(context.cacheDir)
}

private fun deleteCache(dir: File?) {
    if (dir == null) return
    if (dir.isDirectory) {
        dir.listFiles()?.forEach { deleteCache(it) }
    }
    dir.delete()
}

private fun showToast(context: android.content.Context, message: String) {
    android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
}
