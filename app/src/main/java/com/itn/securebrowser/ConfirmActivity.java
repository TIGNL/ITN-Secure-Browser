package com.itn.securebrowser;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ConfirmActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);

        String title = getIntent().getStringExtra("title");
        String message = getIntent().getStringExtra("message");
        String confirmText = getIntent().getStringExtra("confirmText");
        String cancelText = getIntent().getStringExtra("cancelText");

        TextView titleView = findViewById(R.id.confirmTitle);
        TextView messageView = findViewById(R.id.confirmMessage);
        Button btnConfirm = findViewById(R.id.btnConfirm);
        Button btnCancel = findViewById(R.id.btnCancel);

        if (title != null) titleView.setText(title);
        if (message != null) messageView.setText(message);
        if (confirmText != null) btnConfirm.setText(confirmText);
        if (cancelText != null) btnCancel.setText(cancelText);

        btnConfirm.setOnClickListener(v -> {
            setResult(RESULT_OK, new Intent());
            finish();
        });

        btnCancel.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }
}
