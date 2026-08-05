package com.itn.securebrowser

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * الكلاس الأساسي لجميع الصفحات المنبثقة في التطبيق.
 * يضمن: ارتفاع 75% — skipCollapsed — STATE_EXPANDED.
 */
abstract class BaseBottomSheet : BottomSheetDialogFragment() {

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        val sheet  = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val screenHeight = resources.displayMetrics.heightPixels
        sheet.layoutParams.height = (screenHeight * 0.75).toInt()

        BottomSheetBehavior.from(sheet).apply {
            peekHeight    = (screenHeight * 0.75).toInt()
            state         = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
        }
    }
}
