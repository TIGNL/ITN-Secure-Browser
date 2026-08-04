package com.itn.securebrowser

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class PinFragment : Fragment() {

    private lateinit var statusText: TextView
    private lateinit var btnSet:     TextView
    private lateinit var btnClear:   TextView

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
                .setTitle(getString(R.string.delete_pin_title))
                .setMessage(getString(R.string.delete_pin_message))
                .setPositiveButton("حذف") { _, _ ->
                    // اطلب التحقق أولاً قبل الحذف
                    verifyForClear.launch(
                        PinEntryActivity.intentVerify(requireContext(), getString(R.string.pin_verify_first))
                    )
                }
                .setNegativeButton(getString(R.string.btn_cancel), null)
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
        statusText.text = getString(if (hasPin) R.string.pin_status_active else R.string.pin_status_none)
        btnSet.text     = getString(if (hasPin) R.string.btn_change_pin else R.string.btn_set_pin)
        btnClear.visibility = if (hasPin) View.VISIBLE else View.GONE
    }
}
