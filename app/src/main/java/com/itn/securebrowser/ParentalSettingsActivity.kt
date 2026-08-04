package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

class ParentalSettingsActivity : BaseListActivity() {

    private var pinSwitch: Switch? = null
    private var suppressSwitchListener = false

    private val setPinLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PinEntryActivity.RESULT_PIN_OK) {
            refreshPinSwitch()
        }
    }

    private val verifyForChangePin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PinEntryActivity.RESULT_PIN_OK) {
            setPinLauncher.launch(PinEntryActivity.intentSet(this))
        }
    }

    private val verifyForDisablePin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PinEntryActivity.RESULT_PIN_OK) {
            PinManager.clear(this)
            refreshPinSwitch()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageTitle(getString(R.string.parental_settings))

        addPinToggleItem()

        addListItem(R.drawable.ic_pin, label = getString(R.string.btn_change_pin)) {
            handleChangePin()
        }

        addListItem(R.drawable.ic_close, label = getString(R.string.btn_disable_pin)) {
            handleDisablePin()
        }

        addListItem(R.drawable.ic_clock, label = getString(R.string.tab_groups), showDivider = false) {
            showFragment(GroupsFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPinSwitch()
    }

    private fun addPinToggleItem() {
        val row = LayoutInflater.from(this)
            .inflate(R.layout.item_list_row, listContainer, false) as android.widget.LinearLayout

        row.findViewById<ImageView>(R.id.itemIcon)
            .setImageResource(R.drawable.ic_pin)
        row.findViewById<TextView>(R.id.itemLabel).text = getString(R.string.tab_pin)

        pinSwitch = Switch(this).apply {
            thumbTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.accent))
            trackTintList = android.content.res.ColorStateList.valueOf(getColor(R.color.bg_field))
            setPadding(0, 0, 0, 0)
        }
        row.addView(pinSwitch)

        row.setOnClickListener { pinSwitch?.performClick() }

        pinSwitch?.setOnCheckedChangeListener { _, _ ->
            if (!suppressSwitchListener) handlePinToggle()
        }

        refreshPinSwitch()
        listContainer.addView(row)

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            ).apply { marginStart = 20 }
            setBackgroundColor(getColor(R.color.divider))
        }
        listContainer.addView(divider)
    }

    private fun handlePinToggle() {
        val hasPin = PinManager.hasPin(this)
        if (hasPin) {
            AlertDialog.Builder(this, R.style.DialogTheme)
                .setTitle(getString(R.string.tab_pin))
                .setMessage(getString(R.string.delete_pin_message))
                .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                    PinManager.clear(this)
                    refreshPinSwitch()
                }
                .setNegativeButton(getString(R.string.btn_cancel)) { _, _ ->
                    refreshPinSwitch()
                }
                .setOnCancelListener { refreshPinSwitch() }
                .show()
        } else {
            setPinLauncher.launch(PinEntryActivity.intentSet(this))
        }
    }

    private fun handleChangePin() {
        if (PinManager.hasPin(this)) {
            verifyForChangePin.launch(
                PinEntryActivity.intentVerify(this, getString(R.string.pin_verify_first))
            )
        } else {
            setPinLauncher.launch(PinEntryActivity.intentSet(this))
        }
    }

    private fun handleDisablePin() {
        if (PinManager.hasPin(this)) {
            verifyForDisablePin.launch(
                PinEntryActivity.intentVerify(this, getString(R.string.pin_verify_first))
            )
        }
    }

    private fun refreshPinSwitch() {
        suppressSwitchListener = true
        pinSwitch?.isChecked = PinManager.hasPin(this)
        suppressSwitchListener = false
    }
}