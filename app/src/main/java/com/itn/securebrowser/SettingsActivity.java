package com.itn.securebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import com.itn.securebrowser.util.PinManager;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ((TextView) findViewById(R.id.pageTitle)).setText(R.string.settings_title);

        TextView rowPin = findViewById(R.id.rowPinLock);
        TextView rowGroups = findViewById(R.id.rowGroups);

        rowPin.setOnClickListener(v -> {
            if (PinManager.hasPin(this)) {
                startActivity(new Intent(this, PinOptionsActivity.class));
            } else {
                startActivity(new Intent(this, PinSetupActivity.class));
            }
        });

        rowGroups.setOnClickListener(v ->
            startActivity(new Intent(this, GroupListActivity.class))
        );
    }
}
