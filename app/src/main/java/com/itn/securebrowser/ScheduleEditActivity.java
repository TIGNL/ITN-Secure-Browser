package com.itn.securebrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScheduleEditActivity extends BaseActivity {

    private static final int REQ_FROM = 1;
    private static final int REQ_TO = 2;

    private final boolean[] selectedDays = new boolean[7];
    private TextView timeFrom, timeTo, dayError;
    private int fromHour = 8, fromMin = 0, toHour = 22, toMin = 0;
    private final String[] dayIds = {"SATURDAY","SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_edit);

        ((TextView) findViewById(R.id.pageTitle)).setText("Add Block Schedule");

        timeFrom = findViewById(R.id.timeFrom);
        timeTo = findViewById(R.id.timeTo);
        dayError = findViewById(R.id.dayError);
        TextView btnSave = findViewById(R.id.btnSaveSchedule);

        int[] dayViewIds = {R.id.daySat, R.id.daySun, R.id.dayMon, R.id.dayTue, R.id.dayWed, R.id.dayThu, R.id.dayFri};
        for (int i = 0; i < 7; i++) {
            final int idx = i;
            TextView dayView = findViewById(dayViewIds[i]);
            dayView.setOnClickListener(v -> {
                selectedDays[idx] = !selectedDays[idx];
                dayView.setTextColor(selectedDays[idx] ? 0xFF00AA00 : 0xFF666666);
            });
        }

        timeFrom.setOnClickListener(v -> {
            Intent i = new Intent(this, TimePickerActivity.class);
            i.putExtra("hour", fromHour);
            i.putExtra("minute", fromMin);
            startActivityForResult(i, REQ_FROM);
        });

        timeTo.setOnClickListener(v -> {
            Intent i = new Intent(this, TimePickerActivity.class);
            i.putExtra("hour", toHour);
            i.putExtra("minute", toMin);
            startActivityForResult(i, REQ_TO);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) return;
        int hour = data.getIntExtra("hour", 0);
        int minute = data.getIntExtra("minute", 0);
        if (requestCode == REQ_FROM) {
            fromHour = hour;
            fromMin = minute;
            timeFrom.setText(String.format(Locale.US, "%02d:%02d", hour, minute));
        } else if (requestCode == REQ_TO) {
            toHour = hour;
            toMin = minute;
            timeTo.setText(String.format(Locale.US, "%02d:%02d", hour, minute));
        }
    }
}
