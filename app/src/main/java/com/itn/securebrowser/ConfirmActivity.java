package com.itn.securebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ConfirmActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        ((TextView) findViewById(R.id.pageTitle)).setText(R.string.btn_confirm);

        String message = getIntent().getStringExtra("message");
        String confirmText = getIntent().getStringExtra("confirmText");

        TextView messageView = findViewById(R.id.confirmMessage);
        TextView btnOk = findViewById(R.id.btnOk);
        TextView btnCancel = findViewById(R.id.btnCancel);

        if (message != null) messageView.setText(message);
        if (confirmText != null) btnOk.setText(confirmText);

        btnOk.setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent());
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
