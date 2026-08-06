package com.itn.securebrowser

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.itn.securebrowser.ui.sheets.BrowserSheetContent
import com.itn.securebrowser.ui.sheets.Sheet
import com.itn.securebrowser.ui.sheets.SheetHost
import com.itn.securebrowser.ui.sheets.SheetStack
import com.itn.securebrowser.ui.theme.ITNSecureBrowserTheme

class ManageSpaceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ITNSecureBrowserTheme {
                val stack = remember { SheetStack() }

                // Initialize stack once
                LaunchedEffect(Unit) {
                    if (PinManager.hasPin(this@ManageSpaceActivity)) {
                        stack.push(
                            Sheet.PinEntry(
                                mode = "verify",
                                subtitle = getString(R.string.pin_subtitle_manage_data),
                                onVerified = {
                                    stack.pop()  // remove PinEntry
                                    stack.push(Sheet.ManageSpace)
                                },
                                onDismissed = { if (!isFinishing) finish() }
                            )
                        )
                    } else {
                        stack.push(Sheet.ManageSpace)
                    }
                }

                // Finish when stack is empty (user dismissed all sheets)
                LaunchedEffect(stack.stack.size) {
                    if (stack.stack.isEmpty() && stack.stack.size == 0) {
                        if (!isFinishing) finish()
                    }
                }

                SheetHost(stack = stack.stack) { sheet, isTop, dismiss ->
                    BrowserSheetContent(
                        sheet = sheet,
                        stack = stack.stack,
                        activity = null,
                        isTop = isTop,
                        dismiss = dismiss
                    )
                }
            }
        }
    }
}
