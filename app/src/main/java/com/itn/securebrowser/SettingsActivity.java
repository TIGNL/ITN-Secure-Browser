package com.itn.securebrowser;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.itn.securebrowser.util.PinManager;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView rowPin = findViewById(R.id.rowPinLock);
        TextView rowGroups = findViewById(R.id.rowGroups);

        rowPin.setOnClickListener(v -> {
            if (PinManager.hasPin(this)) {
                new AlertDialog.Builder(this)
                    .setTitle("PIN Lock")
                    .setItems(new CharSequence[]{"Change PIN", "Disable PIN"}, (d, which) -> {
                        if (which == 0) {
                            Intent i = new Intent(this, PinEntryActivity.class);
                            i.putExtra("mode", "verify");
                            i.putExtra("subtitle", "Verify first");
                            i.putExtra("changeAction", "setup");
                            startActivity(i);
                        } else {
                            Intent i = new Intent(this, PinEntryActivity.class);
                            i.putExtra("mode", "verify");
                            i.putExtra("subtitle", "Verify first");
                            i.putExtra("changeAction", "disable");
                            startActivity(i);
                        }
                    })
                    .show();
            } else {
                startActivity(new Intent(this, PinSetupActivity.class));
            }
        });

        rowGroups.setOnClickListener(v ->
            startActivity(new Intent(this, GroupListActivity.class))
        );
    }
}
