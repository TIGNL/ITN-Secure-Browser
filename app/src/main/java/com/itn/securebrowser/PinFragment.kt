package com.itn.securebrowser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class PinFragment : Fragment() {

    private lateinit var statusText: TextView
    private lateinit var btnSet:     Button
    private lateinit var btnClear:   Button

    // لانتظار نتيجة PinEntryActivity عند تعيين رمز جديد
    private val setLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PinEntryActivity.RESULT_PIN_OK) refresh()
    }

    // لانتظار نتيجة التحقق قبل الحذف
    private val verifyForClear = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PinEntryActivity.RESULT_PIN_OK) {
            PinManager.clear(requireContext())
            refresh()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_pin, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusText = view.findViewById(R.id.pinStatusText)
        btnSet     = view.findViewById(R.id.btnSetPin)
        btnClear   = view.findViewById(R.id.btnClearPin)

        btnSet.setOnClickListener {
            val intent = PinEntryActivity.intentSet(requireContext())
            setLauncher.launch(intent)
        }

        btnClear.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.DialogTheme)
                .setTitle("حذف الرمز السري")
                .setMessage("سيتم حذف الرمز السري وإزالة الحماية. هل تريد المتابعة؟")
                .setPositiveButton("حذف") { _, _ ->
                    // اطلب التحقق أولاً قبل الحذف
                    verifyForClear.launch(
                        PinEntryActivity.intentVerify(requireContext(), "تحقق من هويتك أولاً")
                    )
                }
                .setNegativeButton("إلغاء", null)
                .show()
        }

        refresh()
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val hasPin = PinManager.hasPin(requireContext())
        statusText.text = if (hasPin) "الرمز السري مفعّل ✓" else "لا يوجد رمز سري مفعّل"
        btnSet.text     = if (hasPin) "تغيير الرمز السري" else "تعيين رمز سري"
        btnClear.visibility = if (hasPin) View.VISIBLE else View.GONE
    }
}
