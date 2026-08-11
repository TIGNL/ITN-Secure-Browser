package com.itn.securebrowser;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.itn.securebrowser.util.BrowserTab;
import java.util.ArrayList;
import java.util.List;

public class TabListActivity extends BaseActivity {

    private ListView tabList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_list);

        tabList = findViewById(R.id.tabList);
        loadTabs();
    }

    private void loadTabs() {
        List<String> titles = new ArrayList<>();
        final List<BrowserTab> tabSnapshot = new ArrayList<>(MainActivity.tabs);

        for (BrowserTab tab : tabSnapshot) {
            String title = tab.title != null && !tab.title.isEmpty() ? tab.title : "New Tab";
            if (tab.id == MainActivity.currentTabId) title = "▶ " + title;
            titles.add(title);
        }

        tabList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, titles));

        tabList.setOnItemClickListener((parent, view, position, id) -> {
            if (position < tabSnapshot.size()) {
                MainActivity.currentTabId = tabSnapshot.get(position).id;
            }
            finish();
        });

        tabList.setOnItemLongClickListener((parent, view, position, id) -> {
            if (position < tabSnapshot.size() && tabSnapshot.size() > 1) {
                int tabId = tabSnapshot.get(position).id;
                new AlertDialog.Builder(this)
                    .setTitle("Close tab")
                    .setMessage("Close \"" + tabSnapshot.get(position).title + "\"?")
                    .setPositiveButton("Close", (d, w) -> {
                        // Find and close in MainActivity
                        for (int i = 0; i < MainActivity.tabs.size(); i++) {
                            if (MainActivity.tabs.get(i).id == tabId) {
                                MainActivity.tabs.get(i).webView.destroy();
                                MainActivity.tabs.remove(i);
                                break;
                            }
                        }
                        if (MainActivity.tabs.isEmpty()) {
                            // Will create new tab when returning to MainActivity
                        }
                        loadTabs();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
            return true;
        });
    }
}
