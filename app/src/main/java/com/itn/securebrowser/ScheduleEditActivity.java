package com.itn.securebrowser;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduleEditActivity extends BaseActivity {

    private final boolean[] selectedDays = new boolean[7];
    private TextView timeFrom, timeTo, dayError;
    private int fromHour = 8, fromMin = 0, toHour = 22, toMin = 0;
    private String[] dayIds = {"SATURDAY","SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_edit);

        timeFrom = findViewById(R.id.timeFrom);
        timeTo = findViewById(R.id.timeTo);
        dayError = findViewById(R.id.dayError);
        Button btnSave = findViewById(R.id.btnSaveSchedule);

        int[] dayIds = {R.id.daySat, R.id.daySun, R.id.dayMon, R.id.dayTue, R.id.dayWed, R.id.dayThu, R.id.dayFri};
        for (int i = 0; i < 7; i++) {
            final int idx = i;
            TextView dayView = findViewById(dayIds[i]);
            dayView.setOnClickListener(v -> {
                selectedDays[idx] = !selectedDays[idx];
                dayView.setBackgroundColor(selectedDays[idx] ? 0xFFE94560 : 0xFF252545);
                dayView.setTextColor(selectedDays[idx] ? 0xFFFFFFFF : 0xFF888888);
            });
        }

        timeFrom.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, h, m) -> {
                fromHour = h; fromMin = m;
                timeFrom.setText(String.format(Locale.US, "%02d:%02d", h, m));
            }, fromHour, fromMin, true).show();
        });

        timeTo.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, h, m) -> {
                toHour = h; toMin = m;
                timeTo.setText(String.format(Locale.US, "%02d:%02d", h, m));
            }, toHour, toMin, true).show();
        });

        btnSave.setOnClickListener(v -> {
            ArrayList<String> days = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                if (selectedDays[i]) days.add(dayIds[i]);
            }
            if (days.isEmpty()) {
                dayError.setVisibility(View.VISIBLE);
                return;
            }
            dayError.setVisibility(View.GONE);

            Intent result = new Intent();
            result.putStringArrayListExtra("days", days);
            result.putExtra("from", String.format(Locale.US, "%02d:%02d", fromHour, fromMin));
            result.putExtra("to", String.format(Locale.US, "%02d:%02d", toHour, toMin));
            setResult(RESULT_OK, result);
            finish();
        });
    }
}
