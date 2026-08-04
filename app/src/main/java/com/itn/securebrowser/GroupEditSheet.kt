package com.itn.securebrowser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class GroupEditSheet : BaseBottomSheet() {

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
        private const val ARG_GROUP_NAME = "arg_group_name"

        fun newInstance(groupName: String? = null): GroupEditSheet {
            return GroupEditSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_GROUP_NAME, groupName)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_group_edit, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        blockDataStore   = BlockDataStore(requireContext())
        editingGroupName = arguments?.getString(ARG_GROUP_NAME)

        view.findViewById<TextView>(R.id.sheetHeaderTitle).text =
            if (editingGroupName != null) getString(R.string.edit_group_title)
            else getString(R.string.new_group_title)

        view.findViewById<View>(R.id.btnBack).setOnClickListener { dismiss() }

        bindViews(view)
        setupListeners(view)
        loadGroupIfEditing()
    }

    private fun bindViews(root: View) {
        btnSave            = root.findViewById(R.id.btnSave)
        inputGroupName     = root.findViewById(R.id.inputGroupName)
        inputLimit         = root.findViewById(R.id.inputLimit)
        btnAddDomain       = root.findViewById(R.id.btnAddDomain)
        domainsContainer   = root.findViewById(R.id.domainsContainer)
        domainsEmptyHint   = root.findViewById(R.id.domainsEmptyHint)
        btnAddSchedule     = root.findViewById(R.id.btnAddSchedule)
        schedulesContainer = root.findViewById(R.id.schedulesContainer)
        schedulesEmptyHint = root.findViewById(R.id.schedulesEmptyHint)
    }

    private fun setupListeners(root: View) {
        btnSave.setOnClickListener        { trySave() }
        btnAddDomain.setOnClickListener   { showAddDomainDialog() }
        btnAddSchedule.setOnClickListener { showAddScheduleDialog() }
    }

    private fun loadGroupIfEditing() {
        val name = editingGroupName ?: return

        val group = blockDataStore.getGroups().find { it.name == name } ?: run {
            dismiss(); return
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
            dismiss()
        } catch (e: IllegalStateException) {
            toast(e.message ?: getString(R.string.err_save))
        }
    }

    private fun showAddDomainDialog() {
        val input = EditText(requireContext()).apply {
            hint = getString(R.string.add_domain_hint)
            inputType     = android.text.InputType.TYPE_TEXT_VARIATION_URI
            textSize      = 15f
            setPadding(48, 32, 48, 32)
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF555577.toInt())
            backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFE94560.toInt())
        }

        AlertDialog.Builder(requireContext(), R.style.DialogTheme)
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
            val row = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_domain_chip, domainsContainer, false)
            row.findViewById<TextView>(R.id.domainText).text = domain
            row.findViewById<ImageButton>(R.id.btnRemoveDomain).setOnClickListener {
                domains.removeAt(index)
                refreshDomainsView()
            }
            domainsContainer.addView(row)
        }
    }

    private fun showAddScheduleDialog() {
        AddBlockScheduleDialog { schedule ->
            schedules.add(schedule)
            refreshSchedulesView()
        }.show(requireActivity().supportFragmentManager, "add_schedule")
    }

    private fun refreshSchedulesView() {
        schedulesContainer.removeAllViews()
        schedulesEmptyHint.visibility = if (schedules.isEmpty()) View.VISIBLE else View.GONE

        schedules.forEachIndexed { index, schedule ->
            val row = LayoutInflater.from(requireContext())
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
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
