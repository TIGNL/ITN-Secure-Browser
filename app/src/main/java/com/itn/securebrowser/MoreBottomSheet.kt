package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView

class MoreBottomSheet(
    private val isDesktopMode:            Boolean,
    private val onDesktopModeToggled:     (Boolean) -> Unit,
    private val onOpenParentalSettings:   () -> Unit,
    private val onOpenGeneralSettings:    () -> Unit
) : BaseBottomSheet() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_more, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text = getString(R.string.sheet_options)

        // ── 1. وضع سطح المكتب ──────────────────────────────────────────────
        val checkDesktop = view.findViewById<CheckBox>(R.id.switchDesktopMode)
        checkDesktop.isChecked = isDesktopMode
        checkDesktop.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
            onDesktopModeToggled(checked)
            dismiss()
        }
        view.findViewById<View>(R.id.rowDesktopMode).setOnClickListener {
            checkDesktop.toggle()
        }

        // ── 2. إعدادات الرقابة — تبقى MoreBottomSheet مفتوحة خلفها ────────
        view.findViewById<View>(R.id.rowParentalSettings).setOnClickListener {
            onOpenParentalSettings()
        }

        // ── 3. الإعدادات العامة — تبقى MoreBottomSheet مفتوحة خلفها ───────
        view.findViewById<View>(R.id.rowGeneralSettings).setOnClickListener {
            onOpenGeneralSettings()
        }
    }

    companion object {
        const val TAG = "MoreBottomSheet"
    }
}
