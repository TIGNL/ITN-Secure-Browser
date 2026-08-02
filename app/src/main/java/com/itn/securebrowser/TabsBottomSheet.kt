package com.itn.securebrowser

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class TabsBottomSheet(
    private val tabs:        List<BrowserTab>,
    private val activeId:    Int,
    private val onSelect:    (BrowserTab) -> Unit,
    private val onClose:     (BrowserTab) -> Unit,
    private val onNewTab:    () -> Unit,
    private val onNewTabFromHistory: () -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var adapter: TabSheetAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_tabs, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ── Tab count header ───────────────────────────────────────────────
        view.findViewById<TextView>(R.id.sheetTabCount).text =
            "${tabs.size} ${if (tabs.size == 1) "تبويبة" else "تبويبات"}"

        // ── RecyclerView ───────────────────────────────────────────────────
        adapter = TabSheetAdapter(
            tabs     = tabs,
            activeId = activeId,
            onSelect = { tab ->
                onSelect(tab)
                dismiss()
            },
            onClose  = { tab ->
                onClose(tab)
                dismiss()
            }
        )

        view.findViewById<RecyclerView>(R.id.sheetTabsList).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = this@TabsBottomSheet.adapter
        }

        // ── New tab button ─────────────────────────────────────────────────
        view.findViewById<TextView>(R.id.btnSheetNewTab).apply {
            // Short press → new blank tab
            setOnClickListener {
                onNewTab()
                dismiss()
            }
            // Long press → open history picker
            setOnLongClickListener {
                onNewTabFromHistory()
                dismiss()
                true
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Force sheet to 75% of screen height
        val dialog = dialog as? BottomSheetDialog ?: return
        val sheet  = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        ) ?: return

        val screenHeight = resources.displayMetrics.heightPixels
        sheet.layoutParams.height = (screenHeight * 0.75).toInt()

        val behavior = BottomSheetBehavior.from(sheet)
        behavior.peekHeight    = (screenHeight * 0.75).toInt()
        behavior.state         = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    companion object {
        const val TAG = "TabsBottomSheet"
    }
}
