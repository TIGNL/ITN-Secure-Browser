package com.itn.securebrowser

import android.app.TimePickerDialog
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast

class AddBlockScheduleDialog(
    private val onScheduleAdded: (BlockSchedule) -> Unit
) : BaseCenteredDialogFragment() {

    override val title: String get() = getString(R.string.add_schedule_title)
    override val contentLayoutRes: Int = R.layout.dialog_add_schedule

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

    override fun onContentCreated(contentView: View) {
        cbSat = contentView.findViewById(R.id.cbSaturday)
        cbSun = contentView.findViewById(R.id.cbSunday)
        cbMon = contentView.findViewById(R.id.cbMonday)
        cbTue = contentView.findViewById(R.id.cbTuesday)
        cbWed = contentView.findViewById(R.id.cbWednesday)
        cbThu = contentView.findViewById(R.id.cbThursday)
        cbFri = contentView.findViewById(R.id.cbFriday)
        btnFrom = contentView.findViewById(R.id.btnFromTime)
        btnTo = contentView.findViewById(R.id.btnToTime)

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
    }

    override fun onOkClicked() {
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