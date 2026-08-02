package com.itn.securebrowser

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class GroupEditActivity : AppCompatActivity() {

    // ── Views ──────────────────────────────────────────────────────────────
    private lateinit var headerTitle:        TextView
    private lateinit var btnSave:            TextView
    private lateinit var inputGroupName:     EditText
    private lateinit var inputLimit:         EditText
    private lateinit var btnAddDomain:       TextView
    private lateinit var domainsContainer:   LinearLayout
    private lateinit var domainsEmptyHint:   TextView
    private lateinit var btnAddSchedule:     TextView
    private lateinit var schedulesContainer: LinearLayout
    private lateinit var schedulesEmptyHint: TextView

    // ── State ──────────────────────────────────────────────────────────────
    private lateinit var blockDataStore: BlockDataStore
    private val domains   = mutableListOf<String>()
    private val schedules = mutableListOf<BlockSchedule>()
    private var editingGroupName: String? = null   // null = new group

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

    // ── Lifecycle ──────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_edit)

        blockDataStore   = BlockDataStore(this)
        editingGroupName = intent.getStringExtra(EXTRA_GROUP_NAME)

        bindViews()
        setupListeners()
        loadGroupIfEditing()
    }

    private fun bindViews() {
        headerTitle        = findViewById(R.id.headerTitle)
        btnSave            = findViewById(R.id.btnSave)
        inputGroupName     = findViewById(R.id.inputGroupName)
        inputLimit         = findViewById(R.id.inputLimit)
        btnAddDomain       = findViewById(R.id.btnAddDomain)
        domainsContainer   = findViewById(R.id.domainsContainer)
        domainsEmptyHint   = findViewById(R.id.domainsEmptyHint)
        btnAddSchedule     = findViewById(R.id.btnAddSchedule)
        schedulesContainer = findViewById(R.id.schedulesContainer)
        schedulesEmptyHint = findViewById(R.id.schedulesEmptyHint)
    }

    private fun setupListeners() {
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        btnSave.setOnClickListener        { trySave() }
        btnAddDomain.setOnClickListener   { showAddDomainDialog() }
        btnAddSchedule.setOnClickListener { showAddScheduleDialog() }
    }

    private fun loadGroupIfEditing() {
        val name = editingGroupName ?: run {
            headerTitle.text = "مجموعة جديدة"
            return
        }
        headerTitle.text = "تعديل المجموعة"

        val group = blockDataStore.getGroups().find { it.name == name } ?: run {
            finish(); return
        }

        inputGroupName.setText(group.name)
        inputGroupName.isEnabled = false   // الاسم لا يتغير عند التعديل

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
        if (name.isBlank()) { toast("أدخل اسم المجموعة"); return }

        val rawLimit = inputLimit.text.toString().trim()
        val limitMins: Int? = if (rawLimit.isBlank()) null else {
            val n = rawLimit.toIntOrNull()
            if (n == null || n <= 0) { toast("الحد اليومي يجب أن يكون رقماً موجباً"); return }
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
            toast(e.message ?: "خطأ في الحفظ")
        }
    }

    // ── Domains ────────────────────────────────────────────────────────────

    private fun showAddDomainDialog() {
        val input = EditText(this).apply {
            hint          = "مثال: instagram.com"
            inputType     = android.text.InputType.TYPE_TEXT_VARIATION_URI
            textSize      = 15f
            setPadding(48, 32, 48, 32)
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF555577.toInt())
            backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE94560.toInt())
        }

        AlertDialog.Builder(this, R.style.DialogTheme)
            .setTitle("إضافة نطاق")
            .setView(input)
            .setPositiveButton("إضافة") { _, _ ->
                val raw = input.text.toString().trim()
                    .removePrefix("https://").removePrefix("http://")
                    .removePrefix("www.").trimEnd('/').lowercase()
                when {
                    raw.isBlank()          -> toast("أدخل النطاق")
                    !raw.contains('.')     -> toast("النطاق غير صحيح")
                    raw in domains         -> toast("النطاق مضاف مسبقاً")
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
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null)

        val cbSat = dialogView.findViewById<CheckBox>(R.id.cbSaturday)
        val cbSun = dialogView.findViewById<CheckBox>(R.id.cbSunday)
        val cbMon = dialogView.findViewById<CheckBox>(R.id.cbMonday)
        val cbTue = dialogView.findViewById<CheckBox>(R.id.cbTuesday)
        val cbWed = dialogView.findViewById<CheckBox>(R.id.cbWednesday)
        val cbThu = dialogView.findViewById<CheckBox>(R.id.cbThursday)
        val cbFri = dialogView.findViewById<CheckBox>(R.id.cbFriday)

        val btnFrom = dialogView.findViewById<TextView>(R.id.btnFromTime)
        val btnTo   = dialogView.findViewById<TextView>(R.id.btnToTime)

        var fromHour = 20; var fromMin = 0
        var toHour   = 23; var toMin   = 0

        fun fmt(h: Int, m: Int) = "%02d:%02d".format(h, m)
        btnFrom.text = fmt(fromHour, fromMin)
        btnTo.text   = fmt(toHour,   toMin)

        btnFrom.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                fromHour = h; fromMin = m; btnFrom.text = fmt(h, m)
            }, fromHour, fromMin, true).show()
        }
        btnTo.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                toHour = h; toMin = m; btnTo.text = fmt(h, m)
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
                if (days.isEmpty()) { toast("اختر يوماً واحداً على الأقل"); return@setPositiveButton }
                schedules.add(BlockSchedule(days = days, from = fmt(fromHour, fromMin), to = fmt(toHour, toMin)))
                refreshSchedulesView()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    private fun refreshSchedulesView() {
        schedulesContainer.removeAllViews()
        schedulesEmptyHint.visibility = if (schedules.isEmpty()) View.VISIBLE else View.GONE

        schedules.forEachIndexed { index, schedule ->
            val row = LayoutInflater.from(this)
                .inflate(R.layout.item_schedule_inline, schedulesContainer, false)

            row.findViewById<TextView>(R.id.scheduleDays).text =
                schedule.days.joinToString("، ") { dayAr(it) }
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
        "SATURDAY"  -> "السبت"
        "SUNDAY"    -> "الأحد"
        "MONDAY"    -> "الاثنين"
        "TUESDAY"   -> "الثلاثاء"
        "WEDNESDAY" -> "الأربعاء"
        "THURSDAY"  -> "الخميس"
        "FRIDAY"    -> "الجمعة"
        else        -> d
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
