package com.itn.securebrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TabSheetAdapter(
    private var tabs: List<BrowserTab>,
    private val activeId: Int,
    private val onSelect: (BrowserTab) -> Unit,
    private val onClose:  (BrowserTab) -> Unit
) : RecyclerView.Adapter<TabSheetAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView    = v.findViewById(R.id.tabSheetTitle)
        val close: ImageButton = v.findViewById(R.id.tabSheetClose)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tab_sheet, parent, false))

    override fun getItemCount() = tabs.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val tab = tabs[position]

        holder.title.text = tab.title.ifBlank { holder.itemView.context.getString(R.string.new_tab) }

        // Active tab → brighter text
        holder.title.alpha = if (tab.id == activeId) 1f else 0.55f

        // Active indicator — left padding accent
        holder.itemView.setBackgroundResource(
            if (tab.id == activeId) R.drawable.tab_sheet_active_bg
            else android.R.color.transparent
        )

        holder.itemView.setOnClickListener { onSelect(tab) }
        holder.close.setOnClickListener   { onClose(tab)  }
    }

    fun update(newTabs: List<BrowserTab>) {
        tabs = newTabs
        notifyDataSetChanged()
    }
}
