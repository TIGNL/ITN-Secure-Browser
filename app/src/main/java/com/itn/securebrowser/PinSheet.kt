package com.itn.securebrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * PinSheet — إدارة الـ PIN عبر ToggleBottomSheet
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * Toggle ON  → خياران: Change PIN / Delete PIN
 * Toggle OFF → لا يوجد PIN (فارغ أو رسالة توضيحية)
 */
class PinSheet : ToggleBottomSheet(
    title        = "PIN",
    toggleLabel  = "PIN",
    initialState = false, // تُحدَّث في onViewCreated
    onContent    = {},    // تُبنى ديناميكياً
    offContent   = {}
) {
    // نتجاوز initialState بعد بناء الـ view
    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildContent(view)
    }

    private fun buildContent(root: View) {
        val contentOn  = root.findViewById<ViewGroup>(R.id.contentOn)
        val contentOff = root.findViewById<ViewGroup>(R.id.contentOff)
        val toggle     = root.findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.toggleSwitch)

        // حالة أولية من PinManager
        val hasPin = PinManager.hasPin(requireContext())
        suppressToggle(toggle, hasPin)
        applyVisibility(contentOn, contentOff, hasPin)

        // بناء محتوى ON: Change PIN + Delete PIN
        contentOn.removeAllViews()
        addRow(contentOn, getString(R.string.btn_change_pin)) { handleChangePin() }
        addRow(contentOn, getString(R.string.btn_clear_pin))  { handleDeletePin() }

        // محتوى OFF: فارغ (يمكن إضافة نص لاحقاً)
        contentOff.removeAllViews()
    }

    override fun onToggleChanged(isOn: Boolean) {
        if (isOn) {
            // تفعيل PIN
            PinEntrySheet(
                mode         = PinEntrySheet.MODE_SET,
                subtitle     = getString(R.string.pin_subtitle_new),
                onPinVerified = {
                    buildContent(requireView())
                },
                onDismissed  = {
                    // إن أغلق بدون تعيين رمز أعد الـ toggle
                    if (!PinManager.hasPin(requireContext())) {
                        revertToggle(view)
                    }
                }
            ).show(parentFragmentManager, "pin_set")
        } else {
            // إيقاف PIN — يجب التحقق أولاً
            PinEntrySheet(
                mode         = PinEntrySheet.MODE_VERIFY,
                subtitle     = getString(R.string.pin_verify_first),
                onPinVerified = {
                    PinManager.clear(requireContext())
                    buildContent(requireView())
                },
                onDismissed  = {
                    if (PinManager.hasPin(requireContext())) {
                        revertToggle(view)
                    }
                }
            ).show(parentFragmentManager, "pin_verify")
        }
    }

    private fun handleChangePin() {
        PinEntrySheet(
            mode         = PinEntrySheet.MODE_VERIFY,
            subtitle     = getString(R.string.pin_verify_first),
            onPinVerified = {
                PinEntrySheet(
                    mode         = PinEntrySheet.MODE_SET,
                    subtitle     = getString(R.string.pin_subtitle_new),
                    onPinVerified = { buildContent(requireView()) }
                ).show(parentFragmentManager, "pin_set")
            }
        ).show(parentFragmentManager, "pin_verify")
    }

    private fun handleDeletePin() {
        PinEntrySheet(
            mode         = PinEntrySheet.MODE_VERIFY,
            subtitle     = getString(R.string.pin_verify_first),
            onPinVerified = {
                PinManager.clear(requireContext())
                buildContent(requireView())
            }
        ).show(parentFragmentManager, "pin_verify")
    }

    private fun addRow(container: ViewGroup, label: String, onClick: () -> Unit) {
        val row = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_list_row, container, false)
        row.findViewById<TextView>(R.id.itemLabel).text = label
        row.setOnClickListener { onClick() }
        container.addView(row)
    }

    private fun suppressToggle(
        toggle: androidx.appcompat.widget.SwitchCompat,
        checked: Boolean
    ) {
        toggle.setOnCheckedChangeListener(null)
        toggle.isChecked = checked
    }

    private fun applyVisibility(on: ViewGroup, off: ViewGroup, isOn: Boolean) {
        on.visibility  = if (isOn) View.VISIBLE else View.GONE
        off.visibility = if (isOn) View.GONE    else View.VISIBLE
    }
}
