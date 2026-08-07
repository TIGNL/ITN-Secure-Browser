package com.itn.securebrowser.ui.sheets

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.itn.securebrowser.BlockSchedule
import com.itn.securebrowser.BrowserTab

const val MODE_VERIFY = "verify"
const val MODE_SET = "set"

enum class LockType { PASSWORD, PIN_4, PIN_6 }

/**
 * Every modal sheet shown in the app is represented by one of these entries.
 * Sheets are pushed on a stack; a new sheet covers the one below and the
 * lower sheet stays composed (and visible again after dismissal).
 */
sealed interface Sheet {
    data class Tabs(val tabs: List<BrowserTab>, val activeId: Int) : Sheet
    data object More : Sheet
    data object Parental : Sheet
    data object General : Sheet
    data object Groups : Sheet
    data class GroupEdit(val groupName: String?) : Sheet
    data class AddDomain(val existing: List<String>, val onAdd: (String) -> Unit) : Sheet
    data class AddSchedule(val onAdd: (BlockSchedule) -> Unit) : Sheet
    data class DeleteGroup(val groupName: String, val onConfirm: () -> Unit) : Sheet
    data object Pin : Sheet
    data class LockMethod(val onSelected: (LockType) -> Unit) : Sheet
    data class PinEntry(
        val mode: String,
        val subtitle: String,
        val pinLength: Int = 0,
        val onVerified: () -> Unit,
        val onDismissed: () -> Unit = {}
    ) : Sheet
    data object ManageSpace : Sheet
    data class ConfirmClearAll(val onConfirm: () -> Unit) : Sheet
}

/** Observable stack of open sheets (Compose state). */
class SheetStack {
    private val _stack = mutableStateListOf<Sheet>()
    val stack: SnapshotStateList<Sheet> get() = _stack
    val top: Sheet? get() = _stack.lastOrNull()

    fun push(sheet: Sheet) { _stack.add(sheet) }
    fun pop() { if (_stack.isNotEmpty()) _stack.removeAt(_stack.size - 1) }
}
