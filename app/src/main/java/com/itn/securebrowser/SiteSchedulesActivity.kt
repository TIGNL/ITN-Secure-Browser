package com.itn.securebrowser

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SiteSchedulesActivity : BaseActivity() {

    private lateinit var blockDataStore: BlockDataStore
    private lateinit var adapter: ScheduleAdapter
    private lateinit var headerDomain: TextView
    private lateinit var headerLimit: TextView
    private lateinit var emptyState: LinearLayout
    private lateinit var schedulesList: RecyclerView
    private lateinit var fabAdd: FloatingActionButton

    private var siteDomain: String = ""

    companion object {
        private const val EXTRA_DOMAIN = "extra_domain"

        fun start(context: Context, domain: String) {
            context.startActivity(
                Intent(context, SiteSchedulesActivity::class.java)
                    .putExtra(EXTRA_DOMAIN, domain)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_site_schedules)

        siteDomain = intent.getStringExtra(EXTRA_DOMAIN) ?: run { finish(); return }
        blockDataStore = BlockDataStore(this)

        headerDomain  = findViewById(R.id.headerDomain)
        headerLimit   = findViewById(R.id.headerLimit)
        emptyState    = findViewById(R.id.emptyState)
        schedulesList = findViewById(R.id.schedulesList)
        fabAdd        = findViewById(R.id.fabAddSchedule)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        headerDomain.text = siteDomain

        adapter = ScheduleAdapter(emptyList()) { schedule -> confirmDeleteSchedule(schedule) }
        schedulesList.layoutManager = LinearLayoutManager(this)
        schedulesList.adapter = adapter

        fabAdd.setOnClickListener { showAddScheduleDialog() }

        refresh()
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private fun getSite(): BlockSite? =
        blockDataStore.getSites().find { it.domain == siteDomain }

    private fun refresh() {
        val site = getSite() ?: run { finish(); return }

        val limitMins = site.dailyLimits.values.firstOrNull()
        headerLimit.text = if (limitMins != null) "الحد اليومي: $limitMins دقيقة"
                           else "بدون حد يومي"

        adapter.updateSchedules(site.schedules)

        val empty = site.schedules.isEmpty()
        emptyState.visibility    = if (empty) View.VISIBLE else View.GONE
        schedulesList.visibility = if (empty) View.GONE    else View.VISIBLE
    }

    // ── Add schedule dialog ───────────────────────────────────────────────────

    private fun showAddScheduleDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_schedule, null)

        // Day checkboxes
        val cbSat = dialogView.findViewById<CheckBox>(R.id.cbSaturday)
        val cbSun = dialogView.findViewById<CheckBox>(R.id.cbSunday)
        val cbMon = dialogView.findViewById<CheckBox>(R.id.cbMonday)
        val cbTue = dialogView.findViewById<CheckBox>(R.id.cbTuesday)
        val cbWed = dialogView.findViewById<CheckBox>(R.id.cbWednesday)
        val cbThu = dialogView.findViewById<CheckBox>(R.id.cbThursday)
        val cbFri = dialogView.findViewById<CheckBox>(R.id.cbFriday)

        // Time buttons
        val btnFrom = dialogView.findViewById<TextView>(R.id.btnFromTime)
        val btnTo   = dialogView.findViewById<TextView>(R.id.btnToTime)

        var fromHour = 20; var fromMin = 0
        var toHour   = 23; var toMin   = 0

        fun fmtTime(h: Int, m: Int) = "%02d:%02d".format(h, m)

        btnFrom.text = fmtTime(fromHour, fromMin)
        btnTo.text   = fmtTime(toHour,   toMin)

        btnFrom.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                fromHour = h; fromMin = m
                btnFrom.text = fmtTime(h, m)
            }, fromHour, fromMin, true).show()
        }

        btnTo.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                toHour = h; toMin = m
                btnTo.text = fmtTime(h, m)
            }, toHour, toMin, true).show()
        }

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("إضافة جدول حظر")
            .setView(dialogView)
            .setPositiveButton("إضافة") { _, _ ->
                val days = buildList {
                    if (cbSat.isChecked) add("SATURDAY")
                    if (cbSun.isChecked) add("SUNDAY")
                    if (cbMon.isChecked) add("MONDAY")
                    if (cbTue.isChecked) add("TUESDAY")
                    if (cbWed.isChecked) add("WEDNESDAY")
                    if (cbThu.isChecked) add("THURSDAY")
                    if (cbFri.isChecked) add("FRIDAY")
                }
                if (days.isEmpty()) {
                    toast("اختر يوماً واحداً على الأقل")
                    return@setPositiveButton
                }
                saveSchedule(
                    BlockSchedule(
                        days = days,
                        from = fmtTime(fromHour, fromMin),
                        to   = fmtTime(toHour,   toMin)
                    )
                )
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    // ── Save / delete ─────────────────────────────────────────────────────────

    private fun saveSchedule(schedule: BlockSchedule) {
        val site = getSite() ?: return
        try {
            blockDataStore.saveSite(site.copy(schedules = site.schedules + schedule))
            refresh()
        } catch (e: Exception) {
            toast(e.message ?: "خطأ في الحفظ")
        }
    }

    private fun confirmDeleteSchedule(schedule: BlockSchedule) {
        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("حذف جدول")
            .setMessage("هل تريد حذف هذا الجدول؟")
            .setPositiveButton("حذف") { _, _ ->
                val site = getSite() ?: return@setPositiveButton
                blockDataStore.saveSite(site.copy(schedules = site.schedules - schedule))
                refresh()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
