package com.itn.securebrowser.ui.sheets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.itn.securebrowser.MainActivity

/**
 * Routes every [Sheet] entry to its Compose content.
 * Called by [SheetHost] for each entry on the stack.
 */
@Composable
fun BrowserSheetContent(
    sheet: Sheet,
    stack: SnapshotStateList<Sheet>,
    activity: com.itn.securebrowser.MainActivity?,
    isTop: Boolean,
    dismiss: () -> Unit
) {
    val push: (Sheet) -> Unit = { stack.add(it) }

    when (sheet) {
        is Sheet.Tabs       -> if (activity != null) TabsSheet(sheet, activity, dismiss)
        is Sheet.More       -> if (activity != null) MoreSheet(activity, dismiss, push)
        is Sheet.Parental   -> ParentalSheet(dismiss, push)
        is Sheet.General    -> GeneralSheet(dismiss)
        is Sheet.Groups     -> GroupsSheet(dismiss, push, isTop)
        is Sheet.GroupEdit  -> GroupEditSheet(sheet, dismiss, push)
        is Sheet.AddDomain  -> AddDomainSheet(sheet, dismiss)
        is Sheet.AddSchedule-> AddScheduleSheet(sheet, dismiss)
        is Sheet.DeleteGroup-> DeleteGroupSheet(sheet, dismiss)
        is Sheet.Pin        -> PinSheet(dismiss, push)
        is Sheet.PinEntry   -> PinEntrySheet(sheet, dismiss)
        is Sheet.ManageSpace-> ManageSpaceSheet(dismiss, push)
        is Sheet.ConfirmClearAll -> ConfirmClearAllSheet(sheet, dismiss)
    }
}
