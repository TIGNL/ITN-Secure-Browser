package com.itn.securebrowser;

import android.content.Intent;
import android.os.Bundle;

public class PinSetupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setup);

        findViewById(R.id.optionPassword).setOnClickListener(v -> launchSet("password", 0));
        findViewById(R.id.optionPin4).setOnClickListener(v -> launchSet("pin4", 4));
        findViewById(R.id.optionPin6).setOnClickListener(v -> launchSet("pin6", 6));
    }

    private void launchSet(String lockType, int pinLength) {
        Intent intent = new Intent(this, PinEntryActivity.class);
        intent.putExtra("mode", "set");
        intent.putExtra("lockType", lockType);
        intent.putExtra("pinLength", pinLength);
        startActivity(intent);
        finish();
    }
}
