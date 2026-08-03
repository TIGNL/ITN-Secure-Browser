package com.itn.securebrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SiteAdapter(
    private var sites: List<BlockSite>,
    private val onItemClick: (BlockSite) -> Unit,
    private val onDelete: (BlockSite) -> Unit
) : RecyclerView.Adapter<SiteAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val domain: TextView       = view.findViewById(R.id.siteDomain)
        val limitInfo: TextView    = view.findViewById(R.id.siteLimitInfo)
        val scheduleInfo: TextView = view.findViewById(R.id.siteScheduleInfo)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteSite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_site, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val site = sites[position]

        holder.domain.text = site.domain

        holder.limitInfo.text = if (site.dailyLimits.isEmpty()) {
            "No daily limit"
        } else {
            val mins = site.dailyLimits.values.first()
            "$mins min/day"
        }

        holder.scheduleInfo.text = when (site.schedules.size) {
            0    -> "No block schedules  ←"
            1    -> "1 block schedule  ←"
            else -> "${site.schedules.size} block schedules  ←"
        }

        holder.itemView.setOnClickListener { onItemClick(site) }
        holder.btnDelete.setOnClickListener { onDelete(site) }
    }

    override fun getItemCount(): Int = sites.size

    fun updateSites(newSites: List<BlockSite>) {
        sites = newSites
        notifyDataSetChanged()
    }
}
