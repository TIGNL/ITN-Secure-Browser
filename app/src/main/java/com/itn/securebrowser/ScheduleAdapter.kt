package com.itn.securebrowser

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScheduleAdapter(
    private var schedules: List<BlockSchedule>,
    private val onDelete: (BlockSchedule) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val days:   TextView    = v.findViewById(R.id.scheduleDays)
        val time:   TextView    = v.findViewById(R.id.scheduleTime)
        val delete: ImageButton = v.findViewById(R.id.scheduleDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule, parent, false))

    override fun getItemCount() = schedules.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val s = schedules[position]
        holder.days.text   = s.days.joinToString(", ") { dayToEnglish(it) }
        holder.time.text   = "${s.from} — ${s.to}"
        holder.delete.setOnClickListener { onDelete(s) }
    }

    fun updateSchedules(list: List<BlockSchedule>) {
        schedules = list
        notifyDataSetChanged()
    }

    private fun dayToEnglish(day: String) = when (day) {
        "SATURDAY"  -> "Sat"
        "SUNDAY"    -> "Sun"
        "MONDAY"    -> "Mon"
        "TUESDAY"   -> "Tue"
        "WEDNESDAY" -> "Wed"
        "THURSDAY"  -> "Thu"
        "FRIDAY"    -> "Fri"
        else        -> day
    }
}

