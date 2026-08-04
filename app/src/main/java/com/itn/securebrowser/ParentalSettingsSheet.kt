package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AlertDialog

class ParentalSettingsSheet : BaseListSheet() {

    private var pinSwitch: Switch? = null
    private var suppressSwitchListener = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPageTitle(getString(R.string.parental_settings))

        addPinToggleItem()

        addListItem(R.drawable.ic_pin, label = getString(R.string.btn_change_pin)) {
            handleChangePin()
        }

        addListItem(R.drawable.ic_close, label = getString(R.string.btn_disable_pin)) {
            handleDisablePin()
        }

        addListItem(R.drawable.ic_clock, label = getString(R.string.tab_groups)) {
            showFragment(GroupsFragment())
        }

        refreshPinSwitch()
    }

    override fun onResume() {
        super.onResume()
        refreshPinSwitch()
    }

    private fun addPinToggleItem() {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_list_row, listContainer, false) as android.widget.LinearLayout

        row.findViewById<ImageView>(R.id.itemIcon)
            .setImageResource(R.drawable.ic_pin)
        row.findViewById<TextView>(R.id.itemLabel).text = getString(R.string.tab_pin)

        pinSwitch = Switch(requireContext()).apply {
            thumbTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.accent))
            trackTintList = android.content.res.ColorStateList.valueOf(requireContext().getColor(R.color.bg_field))
            setPadding(0, 0, 0, 0)
        }
        row.addView(pinSwitch)

        row.setOnClickListener { pinSwitch?.performClick() }

        pinSwitch?.setOnCheckedChangeListener { _, _ ->
            if (!suppressSwitchListener) handlePinToggle()
        }

        listContainer.addView(row)
    }

    private fun handlePinToggle() {
        val hasPin = PinManager.hasPin(requireContext())
        if (hasPin) {
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle(getString(R.string.tab_pin))
                .setMessage(getString(R.string.delete_pin_message))
                .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                    PinManager.clear(requireContext())
                    refreshPinSwitch()
                }
                .setNegativeButton(getString(R.string.btn_cancel)) { _, _ ->
                    refreshPinSwitch()
                }
                .setOnCancelListener { refreshPinSwitch() }
                .show()
        } else {
            PinEntrySheet(
                mode = PinEntrySheet.MODE_SET,
                subtitle = getString(R.string.pin_subtitle_new),
                onPinVerified = { refreshPinSwitch() }
            ).show(parentFragmentManager, "pin")
        }
    }

    private fun handleChangePin() {
        if (PinManager.hasPin(requireContext())) {
            PinEntrySheet(
                mode = PinEntrySheet.MODE_VERIFY,
                subtitle = getString(R.string.pin_verify_first),
                onPinVerified = {
                    PinEntrySheet(
                        mode = PinEntrySheet.MODE_SET,
                        subtitle = getString(R.string.pin_subtitle_new),
                        onPinVerified = { refreshPinSwitch() }
                    ).show(parentFragmentManager, "pin")
                }
            ).show(parentFragmentManager, "pin")
        } else {
            PinEntrySheet(
                mode = PinEntrySheet.MODE_SET,
                subtitle = getString(R.string.pin_subtitle_new),
                onPinVerified = { refreshPinSwitch() }
            ).show(parentFragmentManager, "pin")
        }
    }

    private fun handleDisablePin() {
        if (PinManager.hasPin(requireContext())) {
            PinEntrySheet(
                mode = PinEntrySheet.MODE_VERIFY,
                subtitle = getString(R.string.pin_verify_first),
                onPinVerified = {
                    PinManager.clear(requireContext())
                    refreshPinSwitch()
                }
            ).show(parentFragmentManager, "pin")
        }
    }

    private fun refreshPinSwitch() {
        suppressSwitchListener = true
        pinSwitch?.isChecked = PinManager.hasPin(requireContext())
        suppressSwitchListener = false
    }
}
