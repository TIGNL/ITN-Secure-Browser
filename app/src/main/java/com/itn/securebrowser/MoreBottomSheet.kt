package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MoreBottomSheet(
    private val isDesktopMode: Boolean,
    private val onDesktopModeToggled: (Boolean) -> Unit,
    private val onOpenParentalSettings: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_more, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── 1. وضع سطح المكتب ──────────────────────────────────────────────
        val switchDesktop = view.findViewById<SwitchCompat>(R.id.switchDesktopMode)
        switchDesktop.isChecked = isDesktopMode
        switchDesktop.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
            onDesktopModeToggled(checked)
            dismiss()
        }
        // الضغط على الصف بأكمله يُبدّل الـ Switch
        view.findViewById<View>(R.id.rowDesktopMode).setOnClickListener {
            switchDesktop.toggle()
        }

        // ── 2. إعدادات الرقابة ─────────────────────────────────────────────
        view.findViewById<View>(R.id.rowParentalSettings).setOnClickListener {
            onOpenParentalSettings()
            dismiss()
        }

        // ── 3. الإعدادات العامة (placeholder) ──────────────────────────────
        view.findViewById<View>(R.id.rowGeneralSettings).setOnClickListener {
            // سيُنفَّذ لاحقاً
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as? BottomSheetDialog ?: return
        val sheet  = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val screenHeight = resources.displayMetrics.heightPixels
        sheet.layoutParams.height = (screenHeight * 0.75).toInt()

        val behavior = BottomSheetBehavior.from(sheet)
        behavior.peekHeight    = (screenHeight * 0.75).toInt()
        behavior.state         = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    companion object {
        const val TAG = "MoreBottomSheet"
    }
}
