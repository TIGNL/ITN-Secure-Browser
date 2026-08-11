package com.itn.securebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.TimePicker;

public class TimePickerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_picker);

        ((TextView) findViewById(R.id.pageTitle)).setText(R.string.select_time);

        TimePicker picker = findViewById(R.id.timePicker);
        picker.setIs24HourView(true);

        int hour = getIntent().getIntExtra("hour", 12);
        int minute = getIntent().getIntExtra("minute", 0);
        picker.setHour(hour);
        picker.setMinute(minute);

        TextView btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra("hour", picker.getHour());
            result.putExtra("minute", picker.getMinute());
            setResult(RESULT_OK, result);
            finish();
        });
    }
}
