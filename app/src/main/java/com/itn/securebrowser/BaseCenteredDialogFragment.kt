package com.itn.securebrowser

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.DialogFragment

abstract class BaseCenteredDialogFragment : DialogFragment() {

    protected lateinit var dialogTitle: TextView
    protected lateinit var dialogContentContainer: FrameLayout
    protected lateinit var btnCancel: TextView
    protected lateinit var btnOk: TextView

    abstract val title: String
    abstract val contentLayoutRes: Int
    abstract fun onContentCreated(contentView: View)
    abstract fun onOkClicked()

    override fun onCreateDialog(savedInstanceState: Dialog?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setCanceledOnTouchOutside(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_centered, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialogTitle = view.findViewById(R.id.dialogTitle)
        dialogContentContainer = view.findViewById(R.id.dialogContentContainer)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnOk = view.findViewById(R.id.btnOk)

        dialogTitle.text = title

        // Inflate content into the container
        val contentView = LayoutInflater.from(requireContext())
            .inflate(contentLayoutRes, dialogContentContainer, false)
        dialogContentContainer.addView(contentView)

        onContentCreated(contentView)

        btnCancel.setOnClickListener { dismiss() }
        btnOk.setOnClickListener { onOkClicked() }
    }
}