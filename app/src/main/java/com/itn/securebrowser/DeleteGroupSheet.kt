package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class DeleteGroupSheet(
    private val groupName: String,
    private val onDeleteConfirmed: () -> Unit
) : BaseBottomSheet() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.sheet_delete_group, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text =
            getString(R.string.delete_group_title)

        view.findViewById<TextView>(R.id.tvDeleteMessage).text =
            getString(R.string.delete_group_message, groupName)

        view.findViewById<TextView>(R.id.btnCancelDelete).setOnClickListener { dismiss() }

        view.findViewById<TextView>(R.id.btnConfirmDelete).setOnClickListener {
            onDeleteConfirmed()
            dismiss()
        }
    }
}
