package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ConfirmClearAllSheet(
    private val onConfirmed: () -> Unit
) : BaseBottomSheet() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.sheet_delete_group, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text =
            getString(R.string.clear_all_title)

        view.findViewById<TextView>(R.id.message).text =
            getString(R.string.clear_all_message)

        view.findViewById<TextView>(R.id.btnConfirmDelete).text =
            getString(R.string.btn_clear_all_confirm)

        view.findViewById<View>(R.id.btnConfirmDelete).setOnClickListener {
            onConfirmed()
            dismiss()
        }

        view.findViewById<View>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }
    }
}
