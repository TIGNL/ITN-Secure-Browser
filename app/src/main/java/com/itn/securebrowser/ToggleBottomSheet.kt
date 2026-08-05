package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/**
 * ToggleBottomSheet — قالب عام لصفحة منبثقة تحتوي toggle switch
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * مستقلة عن BaseBottomSheet — نفس الخصائص (75%، skipCollapsed)
 * لكن بدون header — الـ toggle مباشرة في الأعلى.
 *
 * الهيكل:
 *   • صف toggle (64dp) في الأعلى مباشرة
 *   • Divider
 *   • منطقة محتوى ON (تظهر عند تشغيل الـ toggle)
 *   • منطقة محتوى OFF (تظهر عند إيقاف الـ toggle)
 *
 * الاستخدام:
 *   class MySheet : ToggleBottomSheet(
 *       toggleLabel  = "اسم الخيار",
 *       initialState = false,
 *       onContent    = { container -> },
 *       offContent   = { container -> }
 *   )
 */
abstract class ToggleBottomSheet(
    private val toggleLabel:  String,
    private val initialState: Boolean,
    private val onContent:    (ViewGroup) -> Unit,
    private val offContent:   (ViewGroup) -> Unit = {}
) : BottomSheetDialogFragment() {

    // نفس خصائص BaseBottomSheet (75%، skipCollapsed)
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

    private var suppressListener = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_toggle_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.toggleLabel).text = toggleLabel

        val toggleSwitch = view.findViewById<SwitchCompat>(R.id.toggleSwitch)
        val contentOn    = view.findViewById<ViewGroup>(R.id.contentOn)
        val contentOff   = view.findViewById<ViewGroup>(R.id.contentOff)

        onContent(contentOn)
        offContent(contentOff)

        suppressListener = true
        toggleSwitch.isChecked = initialState
        suppressListener = false
        applyState(initialState, contentOn, contentOff)

        view.findViewById<View>(R.id.toggleRow).setOnClickListener {
            toggleSwitch.performClick()
        }

        toggleSwitch.setOnCheckedChangeListener { _: CompoundButton, checked: Boolean ->
            if (!suppressListener) {
                onToggleChanged(checked)
                applyState(checked, contentOn, contentOff)
            }
        }
    }

    private fun applyState(on: Boolean, contentOn: ViewGroup, contentOff: ViewGroup) {
        contentOn.visibility  = if (on) View.VISIBLE else View.GONE
        contentOff.visibility = if (on) View.GONE    else View.VISIBLE
    }

    open fun onToggleChanged(isOn: Boolean) {}

    protected fun revertToggle(view: View?) {
        val toggleSwitch = view?.findViewById<SwitchCompat>(R.id.toggleSwitch) ?: return
        suppressListener = true
        toggleSwitch.isChecked = !toggleSwitch.isChecked
        suppressListener = false
    }
}
