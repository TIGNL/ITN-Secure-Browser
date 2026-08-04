package com.itn.securebrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GroupAdapter(
    private var groups: List<BlockGroup>,
    private val onItemClick: (BlockGroup) -> Unit,
    private val onDelete:    (BlockGroup) -> Unit
) : RecyclerView.Adapter<GroupAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name:      TextView    = v.findViewById(R.id.groupName)
        val domains:   TextView    = v.findViewById(R.id.groupDomains)
        val btnDelete: ImageButton = v.findViewById(R.id.btnDeleteGroup)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false))

    override fun getItemCount() = groups.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val g = groups[position]

        holder.name.text = g.name

        holder.domains.text = when (g.domains.size) {
            0    -> "No domains"
            1    -> g.domains[0]
            2    -> "${g.domains[0]}, ${g.domains[1]}"
            else -> "${g.domains[0]}, ${g.domains[1]} +${g.domains.size - 2}"
        }

        holder.itemView.setOnClickListener { onItemClick(g) }
        holder.btnDelete.setOnClickListener { onDelete(g) }
    }

    fun updateGroups(list: List<BlockGroup>) {
        groups = list
        notifyDataSetChanged()
    }
}