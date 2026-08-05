package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

/**
 * ToggleBottomSheet — قالب عام لصفحة منبثقة تحتوي toggle switch
 * ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 * الهيكل:
 *   • Header مشترك (من bottom_sheet_base)
 *   • صف toggle مع عنوان وswitch
 *   • منطقة محتوى ديناميكية تتغير بحسب حالة الـ toggle
 *
 * الاستخدام:
 *   class MySheet : ToggleBottomSheet(
 *       title       = "عنوان الصفحة",
 *       toggleLabel = "اسم الخيار",
 *       initialState = false,
 *       onContent   = { container -> /* أضف views لحالة ON */ },
 *       offContent  = { container -> /* أضف views لحالة OFF */ }
 *   )
 */
abstract class ToggleBottomSheet(
    private val title:        String,
    private val toggleLabel:  String,
    private val initialState: Boolean,
    private val onContent:    (ViewGroup) -> Unit,
    private val offContent:   (ViewGroup) -> Unit = {}
) : BaseBottomSheet() {

    private var suppressListener = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_toggle_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text = title
        view.findViewById<TextView>(R.id.toggleLabel).text      = toggleLabel

        val toggleSwitch   = view.findViewById<SwitchCompat>(R.id.toggleSwitch)
        val contentOn      = view.findViewById<ViewGroup>(R.id.contentOn)
        val contentOff     = view.findViewById<ViewGroup>(R.id.contentOff)

        // بناء المحتوى مرة واحدة
        onContent(contentOn)
        offContent(contentOff)

        // الحالة الأولية
        suppressListener = true
        toggleSwitch.isChecked = initialState
        suppressListener = false
        applyState(initialState, contentOn, contentOff)

        // صف الـ toggle كاملاً قابل للنقر
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

    /**
     * يُستدعى عند تغيير حالة الـ toggle.
     * الكلاس الوارث يمكنه تجاوزه لإضافة منطق (مثل طلب PIN).
     * إن أراد إلغاء التغيير يستدعي [revertToggle].
     */
    open fun onToggleChanged(isOn: Boolean) {}

    /** يُعيد الـ toggle لحالته السابقة بدون استدعاء onToggleChanged */
    protected fun revertToggle(view: View?) {
        val toggleSwitch = view?.findViewById<SwitchCompat>(R.id.toggleSwitch) ?: return
        suppressListener = true
        toggleSwitch.isChecked = !toggleSwitch.isChecked
        suppressListener = false
    }
}
