package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TabsBottomSheet(
    private val tabs:               List<BrowserTab>,
    private val activeId:           Int,
    private val onSelect:           (BrowserTab) -> Unit,
    private val onClose:            (BrowserTab) -> Unit,
    private val onNewTab:           () -> Unit,
    private val onNewTabFromHistory: () -> Unit
) : BaseBottomSheet() {

    private lateinit var adapter: TabSheetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_tabs, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text =
            "${tabs.size} ${if (tabs.size == 1) "تبويبة" else "تبويبات"}"

        adapter = TabSheetAdapter(
            tabs     = tabs,
            activeId = activeId,
            onSelect = { tab -> onSelect(tab); dismiss() },
            onClose  = { tab -> onClose(tab);  dismiss() }
        )

        view.findViewById<RecyclerView>(R.id.sheetTabsList).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = this@TabsBottomSheet.adapter
        }

        view.findViewById<ImageButton>(R.id.btnSheetNewTab).apply {
            setOnClickListener     { onNewTab();            dismiss() }
            setOnLongClickListener { onNewTabFromHistory(); dismiss(); true }
        }
    }

    companion object {
        const val TAG = "TabsBottomSheet"
    }
}

