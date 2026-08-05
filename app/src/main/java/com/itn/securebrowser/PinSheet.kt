package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class PinSheet : ToggleBottomSheet(
    toggleLabel  = "PIN",
    initialState = false,
    onContent    = {},
    offContent   = {}
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh(view)
    }

    private fun refresh(root: View) {
        val hasPin     = PinManager.hasPin(requireContext())
        val toggle     = root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.toggleSwitch)
        val contentOn  = root.findViewById<ViewGroup>(R.id.contentOn)
        val contentOff = root.findViewById<ViewGroup>(R.id.contentOff)

        toggle.setOnCheckedChangeListener(null)
        toggle.isChecked = hasPin

        contentOn.removeAllViews()
        contentOff.removeAllViews()

        if (hasPin) {
            addRow(contentOn, getString(R.string.btn_change_pin)) { handleChangePin() }
            addRow(contentOn, getString(R.string.btn_clear_pin))  { handleDeletePin() }
        }

        contentOn.visibility  = if (hasPin) View.VISIBLE else View.GONE
        contentOff.visibility = if (hasPin) View.GONE    else View.VISIBLE

        toggle.setOnCheckedChangeListener { _, checked -> onToggleChanged(checked) }
    }

    override fun onToggleChanged(isOn: Boolean) {
        if (isOn) {
            PinEntrySheet(
                mode          = PinEntrySheet.MODE_SET,
                subtitle      = getString(R.string.pin_subtitle_new),
                onPinVerified = { refresh(requireView()) },
                onDismissed   = { if (!PinManager.hasPin(requireContext())) revertToggle(view) }
            ).show(parentFragmentManager, "pin_set")
        } else {
            PinEntrySheet(
                mode          = PinEntrySheet.MODE_VERIFY,
                subtitle      = getString(R.string.pin_verify_first),
                onPinVerified = { PinManager.clear(requireContext()); refresh(requireView()) },
                onDismissed   = { if (PinManager.hasPin(requireContext())) revertToggle(view) }
            ).show(parentFragmentManager, "pin_verify")
        }
    }

    private fun handleChangePin() {
        PinEntrySheet(
            mode          = PinEntrySheet.MODE_VERIFY,
            subtitle      = getString(R.string.pin_verify_first),
            onPinVerified = {
                PinEntrySheet(
                    mode          = PinEntrySheet.MODE_SET,
                    subtitle      = getString(R.string.pin_subtitle_new),
                    onPinVerified = { refresh(requireView()) }
                ).show(parentFragmentManager, "pin_set")
            }
        ).show(parentFragmentManager, "pin_verify")
    }

    private fun handleDeletePin() {
        PinEntrySheet(
            mode          = PinEntrySheet.MODE_VERIFY,
            subtitle      = getString(R.string.pin_verify_first),
            onPinVerified = { PinManager.clear(requireContext()); refresh(requireView()) }
        ).show(parentFragmentManager, "pin_verify")
    }

    private fun addRow(container: ViewGroup, label: String, onClick: () -> Unit) {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_list_row, container, false)
        row.findViewById<TextView>(R.id.itemLabel).text = label
        row.setOnClickListener { onClick() }
        container.addView(row)
    }
}
