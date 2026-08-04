package com.itn.securebrowser

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

class ParentalSettingsActivity : BaseListActivity() {

    private val pinLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PinEntryActivity.RESULT_PIN_OK) {
            // PIN was set/verified successfully
            updateLockSwitch()
        }
    }

    private var lockSwitch: Switch? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPageTitle(getString(R.string.parental_settings))

        // Add lock settings checkbox at the top
        addLockSettingsItem()

        // Add "Limit websites time" (groups) option
        addListItem(R.drawable.ic_groups, label = getString(R.string.tab_groups)) {
            showFragment(GroupsFragment())
        }

        // Add PIN option
        addListItem(R.drawable.ic_pin, label = getString(R.string.tab_pin)) {
            showFragment(PinFragment())
        }
    }

    override fun onResume() {
        super.onResume()
        updateLockSwitch()
    }

    private fun addLockSettingsItem() {
        val row = LayoutInflater.from(this)
            .inflate(R.layout.item_lock_settings, listContainer, false)

        row.findViewById<TextView>(R.id.itemLabel).text = getString(R.string.lock_settings)

        lockSwitch = row.findViewById(R.id.lockSwitch)
        updateLockSwitch()

        lockSwitch?.setOnCheckedChangeListener { _, isChecked ->
            handleLockToggle(isChecked)
        }

        listContainer.addView(row)
    }

    private fun handleLockToggle(isChecked: Boolean) {
        if (isChecked) {
            // If turning ON: open PIN set page
            if (!PinManager.hasPin(this)) {
                pinLauncher.launch(PinEntryActivity.intentSet(this))
            }
        } else {
            // If turning OFF: verify PIN first, then clear it
            if (PinManager.hasPin(this)) {
                AlertDialog.Builder(this, R.style.DialogTheme)
                    .setTitle(getString(R.string.lock_settings))
                    .setMessage(getString(R.string.delete_pin_message))
                    .setPositiveButton(getString(R.string.btn_delete)) { _, _ ->
                        PinManager.clear(this)
                        updateLockSwitch()
                    }
                    .setNegativeButton(getString(R.string.btn_cancel), null)
                    .show()
            }
        }
    }

    private fun updateLockSwitch() {
        val hasPin = PinManager.hasPin(this)
        lockSwitch?.isChecked = hasPin
    }
}