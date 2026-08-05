package com.itn.securebrowser

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast

class AddBlockScheduleSheet(
    private val onScheduleAdded: (BlockSchedule) -> Unit
) : BaseListSheet() {

    private lateinit var cbSat: CheckBox
    private lateinit var cbSun: CheckBox
    private lateinit var cbMon: CheckBox
    private lateinit var cbTue: CheckBox
    private lateinit var cbWed: CheckBox
    private lateinit var cbThu: CheckBox
    private lateinit var cbFri: CheckBox
    private lateinit var btnFrom: TextView
    private lateinit var btnTo: TextView

    private var fromHour = 20
    private var fromMin = 0
    private var toHour = 23
    private var toMin = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setPageTitle(getString(R.string.add_schedule_title))

        val formView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_schedule, listContainer, false)

        cbSat = formView.findViewById(R.id.cbSaturday)
        cbSun = formView.findViewById(R.id.cbSunday)
        cbMon = formView.findViewById(R.id.cbMonday)
        cbTue = formView.findViewById(R.id.cbTuesday)
        cbWed = formView.findViewById(R.id.cbWednesday)
        cbThu = formView.findViewById(R.id.cbThursday)
        cbFri = formView.findViewById(R.id.cbFriday)
        btnFrom = formView.findViewById(R.id.btnFromTime)
        btnTo = formView.findViewById(R.id.btnToTime)

        fun fmt(h: Int, m: Int) = "%02d:%02d".format(h, m)
        btnFrom.text = fmt(fromHour, fromMin)
        btnTo.text = fmt(toHour, toMin)

        btnFrom.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                fromHour = h; fromMin = m; btnFrom.text = fmt(h, m)
            }, fromHour, fromMin, true).show()
        }
        btnTo.setOnClickListener {
            TimePickerDialog(requireContext(), { _, h, m ->
                toHour = h; toMin = m; btnTo.text = fmt(h, m)
            }, toHour, toMin, true).show()
        }

        listContainer.addView(formView)

        addListItem(R.drawable.ic_add, label = getString(R.string.btn_ok)) {
            onOkClicked()
        }
    }

    private fun onOkClicked() {
        fun fmt(h: Int, m: Int) = "%02d:%02d".format(h, m)

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
            Toast.makeText(requireContext(), getString(R.string.err_select_day), Toast.LENGTH_SHORT).show()
            return
        }

        onScheduleAdded(
            BlockSchedule(
                days = days,
                from = fmt(fromHour, fromMin),
                to = fmt(toHour, toMin)
            )
        )
        dismiss()
    }
}
