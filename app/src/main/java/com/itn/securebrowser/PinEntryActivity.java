package com.itn.securebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.itn.securebrowser.util.PinManager;

public class PinEntryActivity extends BaseActivity {

    private String mode;
    private String subtitle;
    private String nextAction;
    private int pinLength = 0;
    private String lockType = "";
    private String firstPin = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_entry);

        mode = getIntent().getStringExtra("mode");
        subtitle = getIntent().getStringExtra("subtitle");
        nextAction = getIntent().getStringExtra("next");
        pinLength = getIntent().getIntExtra("pinLength", 0);
        lockType = getIntent().getStringExtra("lockType");

        TextView titleView = findViewById(R.id.pinTitle);
        TextView subtitleView = findViewById(R.id.pinSubtitle);
        TextView errorView = findViewById(R.id.pinError);
        EditText input = findViewById(R.id.pinInput);
        Button btnOk = findViewById(R.id.btnOk);

        if ("verify".equals(mode)) {
            titleView.setText(R.string.pin_title_enter);
            subtitleView.setText(subtitle != null ? subtitle : getString(R.string.pin_subtitle_verify));
        } else if ("set".equals(mode)) {
            titleView.setText(R.string.pin_title_new);
            if (subtitle != null) subtitleView.setText(subtitle);
        } else if ("confirm".equals(mode)) {
            titleView.setText(R.string.pin_title_confirm);
        }

        if (subtitleView.getText().toString().isEmpty()) {
            subtitleView.setVisibility(android.view.View.GONE);
        } else {
            subtitleView.setVisibility(android.view.View.VISIBLE);
        }

        if (pinLength > 0) {
            input.setMaxLength(pinLength);
            input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        } else {
            input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }

        btnOk.setOnClickListener(v -> {
            String pin = input.getText().toString();
            errorView.setVisibility(android.view.View.GONE);

            if ("verify".equals(mode)) {
                if (PinManager.verify(this, pin)) {
                    if ("settings".equals(nextAction)) {
                        startActivity(new Intent(this, SettingsActivity.class));
                    } else if ("manage_space".equals(nextAction)) {
                        startActivity(new Intent(this, ManageSpaceActivity.class));
                    } else if (getIntent().hasExtra("changeAction")) {
                        String changeAction = getIntent().getStringExtra("changeAction");
                        if ("disable".equals(changeAction)) {
                            PinManager.clear(this);
                            Toast.makeText(this, R.string.toast_cleared, Toast.LENGTH_SHORT).show();
                        } else if ("setup".equals(changeAction)) {
                            startActivity(new Intent(this, PinSetupActivity.class));
                        }
                    }
                    finish();
                } else {
                    errorView.setText(R.string.pin_error_wrong);
                    errorView.setVisibility(android.view.View.VISIBLE);
                    input.setText("");
                }
            } else if ("set".equals(mode)) {
                firstPin = pin;
                Intent intent = new Intent(this, PinEntryActivity.class);
                intent.putExtra("mode", "confirm");
                intent.putExtra("pinLength", pinLength);
                intent.putExtra("lockType", lockType);
                intent.putExtra("firstPin", firstPin);
                intent.putExtra("next", nextAction);
                startActivity(intent);
                finish();
            } else if ("confirm".equals(mode)) {
                String first = getIntent().getStringExtra("firstPin");
                if (pin.equals(first)) {
                    PinManager.savePin(this, pin);
                    Toast.makeText(this, "PIN saved", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    errorView.setText(R.string.pin_error_mismatch);
                    errorView.setVisibility(android.view.View.VISIBLE);
                    input.setText("");
                }
            }
        });
    }
}
