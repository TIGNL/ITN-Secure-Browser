package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

class MoreBottomSheet(
    private val isDesktopMode:          Boolean,
    private val onDesktopModeToggled:   (Boolean) -> Unit,
    private val onOpenParentalSettings: () -> Unit
) : BaseBottomSheet() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_more, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text = "الخيارات"

        // ── 1. وضع سطح المكتب ──────────────────────────────────────────────
        val switchDesktop = view.findViewById<SwitchCompat>(R.id.switchDesktopMode)
        switchDesktop.isChecked = isDesktopMode
        switchDesktop.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
            onDesktopModeToggled(checked)
            dismiss()
        }
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
            dismiss()
        }
    }

    companion object {
        const val TAG = "MoreBottomSheet"
    }
}

