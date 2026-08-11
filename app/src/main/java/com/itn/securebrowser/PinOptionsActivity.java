package com.itn.securebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.itn.securebrowser.util.PinManager;

public class PinOptionsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_options);

        TextView btnChange = findViewById(R.id.optionChangePin);
        TextView btnDisable = findViewById(R.id.optionDisablePin);

        btnChange.setOnClickListener(v -> {
            Intent i = new Intent(this, PinEntryActivity.class);
            i.putExtra("mode", "verify");
            i.putExtra("subtitle", "Verify first");
            i.putExtra("changeAction", "setup");
            startActivity(i);
            finish();
        });

        btnDisable.setOnClickListener(v -> {
            PinManager.clear(this);
            Toast.makeText(this, R.string.toast_cleared, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
