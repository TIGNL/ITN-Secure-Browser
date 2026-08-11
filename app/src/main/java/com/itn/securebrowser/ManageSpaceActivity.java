package com.itn.securebrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.widget.TextView;
import android.widget.Toast;
import com.itn.securebrowser.util.PinManager;
import java.io.File;

public class ManageSpaceActivity extends BaseActivity {

    private static final int REQ_CLEAR_ALL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (PinManager.hasPin(this)) {
            startActivity(new android.content.Intent(this, PinEntryActivity.class)
                .putExtra("mode", "verify")
                .putExtra("subtitle", "To manage app data")
                .putExtra("next", "manage_space"));
            finish();
            return;
        }

        setupUI();
    }

    private void setupUI() {
        setContentView(R.layout.activity_manage_space);

        TextView btnBrowsing = findViewById(R.id.btnClearBrowsing);
        TextView btnTracking = findViewById(R.id.btnClearTracking);
        TextView btnBlocking = findViewById(R.id.btnClearBlocking);
        TextView btnAll = findViewById(R.id.btnClearAll);

        btnBrowsing.setOnClickListener(v -> {
            CookieManager.getInstance().removeAllCookies(null);
            WebStorage.getInstance().deleteAllData();
            deleteCache(getCacheDir());
            Toast.makeText(this, R.string.toast_cleared, Toast.LENGTH_SHORT).show();
        });

        btnTracking.setOnClickListener(v -> {
            getSharedPreferences("itn_time_tracker", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, R.string.toast_cleared, Toast.LENGTH_SHORT).show();
        });

        btnBlocking.setOnClickListener(v -> {
            getSharedPreferences("itn_block_data", MODE_PRIVATE).edit().clear().apply();
            Toast.makeText(this, R.string.toast_cleared, Toast.LENGTH_SHORT).show();
        });

        btnAll.setOnClickListener(v -> {
            Intent i = new Intent(this, ConfirmActivity.class);
            i.putExtra("title", getString(R.string.clear_all));
            i.putExtra("message", getString(R.string.clear_all_confirm));
            i.putExtra("confirmText", getString(R.string.btn_delete));
            startActivityForResult(i, REQ_CLEAR_ALL);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CLEAR_ALL && resultCode == Activity.RESULT_OK) {
            CookieManager.getInstance().removeAllCookies(null);
            WebStorage.getInstance().deleteAllData();
            deleteCache(getCacheDir());
            getSharedPreferences("itn_time_tracker", MODE_PRIVATE).edit().clear().apply();
            getSharedPreferences("itn_block_data", MODE_PRIVATE).edit().clear().apply();
            PinManager.clear(this);
            Toast.makeText(this, R.string.toast_cleared, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void deleteCache(File dir) {
        if (dir == null) return;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) deleteCache(f);
            }
        }
        dir.delete();
    }
}
