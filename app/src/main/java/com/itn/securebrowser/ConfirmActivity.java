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

        TextView messageView = findViewById(R.id.confirmMessage);
        if (message != null) messageView.setText(message);

        findViewById(R.id.btnOk).setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent());
            finish();
        });

        findViewById(R.id.btnCancel).setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
