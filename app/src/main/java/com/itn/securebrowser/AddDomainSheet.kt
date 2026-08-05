package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class AddDomainSheet(
    private val existingDomains: List<String>,
    private val onDomainAdded: (String) -> Unit
) : BaseBottomSheet() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.sheet_add_domain, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text =
            getString(R.string.add_domain_title)

        val inputDomain = view.findViewById<EditText>(R.id.inputDomain)

        view.findViewById<TextView>(R.id.btnCancelDomain).setOnClickListener { dismiss() }

        view.findViewById<TextView>(R.id.btnAddDomainConfirm).setOnClickListener {
            val raw = inputDomain.text.toString().trim()
                .removePrefix("https://").removePrefix("http://")
                .removePrefix("www.").trimEnd('/').lowercase()
            when {
                raw.isBlank()           -> toast(getString(R.string.err_domain_blank))
                !raw.contains('.')     -> toast(getString(R.string.err_domain_invalid))
                raw in existingDomains  -> toast(getString(R.string.err_domain_duplicate))
                else -> {
                    onDomainAdded(raw)
                    dismiss()
                }
            }
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
