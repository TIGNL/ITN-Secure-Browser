package com.itn.securebrowser.ui.sheets

import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itn.securebrowser.PinManager
import com.itn.securebrowser.R

@Composable
fun ManageSpaceSheet(dismiss: () -> Unit, push: (Sheet) -> Unit) {
    val context = LocalContext.current

    fun clearBrowsing() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        context.cacheDir.deleteRecursively()
        Toast.makeText(
            context,
            context.getString(R.string.toast_cleared_browsing),
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    fun clearTracking() {
        context.getSharedPreferences("itn_time_tracker", 0).edit().clear().apply()
        Toast.makeText(
            context,
            context.getString(R.string.toast_cleared_tracking),
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    fun clearBlocking() {
        context.getSharedPreferences("itn_block_data", 0).edit().clear().apply()
        Toast.makeText(
            context,
            context.getString(R.string.toast_cleared_blocking),
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    fun clearAll() {
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()
        WebStorage.getInstance().deleteAllData()
        context.cacheDir.deleteRecursively()
        context.getSharedPreferences("itn_time_tracker", 0).edit().clear().apply()
        context.getSharedPreferences("itn_block_data", 0).edit().clear().apply()
        PinManager.clear(context)
        Toast.makeText(
            context,
            context.getString(R.string.toast_cleared_all),
            Toast.LENGTH_SHORT
        ).show()
        dismiss()
    }

    SheetScaffold(
        title = stringResource(R.string.manage_space_title),
        onClose = dismiss,
        items = listOf(
            SheetItem.Row(
                icon = Icons.Filled.Close,
                title = stringResource(R.string.clear_browsing),
                onClick = { clearBrowsing() }
            ),
            SheetItem.Divider,
            SheetItem.Row(
                icon = Icons.Filled.Timer,
                title = stringResource(R.string.clear_tracking),
                onClick = { clearTracking() }
            ),
            SheetItem.Divider,
            SheetItem.Row(
                icon = Icons.Filled.Block,
                title = stringResource(R.string.clear_blocking),
                onClick = { clearBlocking() }
            ),
            SheetItem.Divider,
            SheetItem.Row(
                icon = Icons.Filled.DeleteSweep,
                title = stringResource(R.string.clear_all),
                onClick = { push(Sheet.ConfirmClearAll(onConfirm = ::clearAll)) }
            )
        )
    )
}

@Composable
fun ConfirmClearAllSheet(sheet: Sheet.ConfirmClearAll, dismiss: () -> Unit) {
    SheetScaffold(
        title = stringResource(R.string.clear_all_title),
        onClose = dismiss,
        items = listOf(
            SheetItem.InfoBlock {
                Text(
                    stringResource(R.string.clear_all_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            },
            SheetItem.Divider,
            SheetItem.Row(
                icon = Icons.Filled.DeleteSweep,
                title = stringResource(R.string.btn_clear_all_confirm),
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { sheet.onConfirm(); dismiss() }
            )
        )
    )
}
