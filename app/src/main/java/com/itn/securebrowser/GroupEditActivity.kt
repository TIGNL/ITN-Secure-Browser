package com.itn.securebrowser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog

class GroupEditActivity : BaseListActivity() {

    private lateinit var btnSave:            TextView
    private lateinit var inputGroupName:     EditText
    private lateinit var inputLimit:         EditText
    private lateinit var btnAddDomain:       TextView
    private lateinit var domainsContainer:   LinearLayout
    private lateinit var domainsEmptyHint:   TextView
    private lateinit var btnAddSchedule:     TextView
    private lateinit var schedulesContainer: LinearLayout
    private lateinit var schedulesEmptyHint: TextView

    private lateinit var blockDataStore: BlockDataStore
    private val domains   = mutableListOf<String>()
    private val schedules = mutableListOf<BlockSchedule>()
    private var editingGroupName: String? = null

    companion object {
        private const val EXTRA_GROUP_NAME = "extra_group_name"

        fun startNew(context: Context) {
            context.startActivity(Intent(context, GroupEditActivity::class.java))
        }

        fun startEdit(context: Context, groupName: String) {
            context.startActivity(
                Intent(context, GroupEditActivity::class.java)
                    .putExtra(EXTRA_GROUP_NAME, groupName)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        blockDataStore   = BlockDataStore(this)
        editingGroupName = intent.getStringExtra(EXTRA_GROUP_NAME)

        setPageTitle(
            if (editingGroupName != null) getString(R.string.edit_group_title)
            else getString(R.string.new_group_title)
        )

        val content = LayoutInflater.from(this)
            .inflate(R.layout.activity_group_edit, listContainer, false)

        bindViews(content)
        setupListeners(content)
        loadGroupIfEditing()

        listContainer.addView(content)
    }

    private fun bindViews(content: View) {
        btnSave            = content.findViewById(R.id.btnSave)
        inputGroupName     = content.findViewById(R.id.inputGroupName)
        inputLimit         = content.findViewById(R.id.inputLimit)
        btnAddDomain       = content.findViewById(R.id.btnAddDomain)
        domainsContainer   = content.findViewById(R.id.domainsContainer)
        domainsEmptyHint   = content.findViewById(R.id.domainsEmptyHint)
        btnAddSchedule     = content.findViewById(R.id.btnAddSchedule)
        schedulesContainer = content.findViewById(R.id.schedulesContainer)
        schedulesEmptyHint = content.findViewById(R.id.schedulesEmptyHint)
    }

    private fun setupListeners(content: View) {
        btnSave.setOnClickListener        { trySave() }
        btnAddDomain.setOnClickListener   { showAddDomainDialog() }
        btnAddSchedule.setOnClickListener { showAddScheduleDialog() }
    }

    private fun loadGroupIfEditing() {
        val name = editingGroupName ?: return

        val group = blockDataStore.getGroups().find { it.name == name } ?: run {
            finish(); return
        }

        inputGroupName.setText(group.name)
        inputGroupName.isEnabled = false

        val limitMins = group.dailyLimits.values.firstOrNull()
        if (limitMins != null) inputLimit.setText(limitMins.toString())

        domains.addAll(group.domains)
        schedules.addAll(group.schedules)

        refreshDomainsView()
        refreshSchedulesView()
    }

    // ── Save ───────────────────────────────────────────────────────────────

    private fun trySave() {
        val name = inputGroupName.text.toString().trim()
        if (name.isBlank()) { toast(getString(R.string.err_enter_group_name)); return }

        val rawLimit = inputLimit.text.toString().trim()
        val limitMins: Int? = if (rawLimit.isBlank()) null else {
            val n = rawLimit.toIntOrNull()
            if (n == null || n <= 0) { toast(getString(R.string.err_daily_limit_positive)); return }
            n
        }

        val allDays = listOf("SATURDAY","SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY")
        val dailyLimits: Map<String, Int> =
            if (limitMins != null) allDays.associateWith { limitMins } else emptyMap()

        try {
            blockDataStore.saveGroup(
                BlockGroup(
                    name        = name,
                    domains     = domains.toList(),
                    dailyLimits = dailyLimits,
                    schedules   = schedules.toList()
                )
            )
            finish()
        } catch (e: IllegalStateException) {
            toast(e.message ?: getString(R.string.err_save))
        }
    }

    // ── Domains ────────────────────────────────────────────────────────────

    private fun showAddDomainDialog() {
        val input = EditText(this).apply {
            hint = getString(R.string.add_domain_hint)
            inputType     = android.text.InputType.TYPE_TEXT_VARIATION_URI
            textSize      = 15f
            setPadding(48, 32, 48, 32)
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF555577.toInt())
            backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE94560.toInt())
        }

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle(getString(R.string.add_domain_title))
            .setView(input)
            .setPositiveButton(getString(R.string.btn_add)) { _, _ ->
                val raw = input.text.toString().trim()
                    .removePrefix("https://").removePrefix("http://")
                    .removePrefix("www.").trimEnd('/').lowercase()
                when {
                    raw.isBlank()      -> toast(getString(R.string.err_domain_blank))
                    !raw.contains('.') -> toast(getString(R.string.err_domain_invalid))
                    raw in domains     -> toast(getString(R.string.err_domain_duplicate))
                    else -> {
                        domains.add(raw)
                        refreshDomainsView()
                    }
                }
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun refreshDomainsView() {
        domainsContainer.removeAllViews()
        domainsEmptyHint.visibility = if (domains.isEmpty()) View.VISIBLE else View.GONE

        domains.forEachIndexed { index, domain ->
            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_domain_chip, domainsContainer, false)
            row.findViewById<TextView>(R.id.domainText).text = domain
            row.findViewById<ImageButton>(R.id.btnRemoveDomain).setOnClickListener {
                domains.removeAt(index)
                refreshDomainsView()
            }
            domainsContainer.addView(row)
        }
    }

    // ── Schedules ──────────────────────────────────────────────────────────

    private fun showAddScheduleDialog() {
        AddBlockScheduleDialog { schedule ->
            schedules.add(schedule)
            refreshSchedulesView()
        }.show(supportFragmentManager, "add_schedule")
    }

    private fun refreshSchedulesView() {
        schedulesContainer.removeAllViews()
        schedulesEmptyHint.visibility = if (schedules.isEmpty()) View.VISIBLE else View.GONE

        schedules.forEachIndexed { index, schedule ->
            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_schedule_inline, schedulesContainer, false)

            row.findViewById<TextView>(R.id.scheduleDays).text =
                schedule.days.joinToString(", ") { dayAr(it) }
            row.findViewById<TextView>(R.id.scheduleTime).text =
                "${schedule.from} — ${schedule.to}"
            row.findViewById<ImageButton>(R.id.btnRemoveSchedule).setOnClickListener {
                schedules.removeAt(index)
                refreshSchedulesView()
            }
            schedulesContainer.addView(row)
        }
    }

    // ── Helper ─────────────────────────────────────────────────────────────

    private fun dayAr(d: String) = when (d) {
        "SATURDAY"  -> getString(R.string.day_saturday)
        "SUNDAY"    -> getString(R.string.day_sunday)
        "MONDAY"    -> getString(R.string.day_monday)
        "TUESDAY"   -> getString(R.string.day_tuesday)
        "WEDNESDAY" -> getString(R.string.day_wednesday)
        "THURSDAY"  -> getString(R.string.day_thursday)
        "FRIDAY"    -> getString(R.string.day_friday)
        else        -> d
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}