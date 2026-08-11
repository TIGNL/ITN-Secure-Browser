package com.itn.securebrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.itn.securebrowser.util.BrowserTab;
import java.util.ArrayList;
import java.util.List;

public class TabListActivity extends BaseActivity {

    private static final int REQ_CLOSE = 1;
    private ListView tabList;
    private int closeTabId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_list);

        ((TextView) findViewById(R.id.pageTitle)).setText(R.string.tabs_title);

        tabList = findViewById(R.id.tabList);
        loadTabs();
    }

    private void loadTabs() {
        List<String> titles = new ArrayList<>();
        final List<BrowserTab> tabSnapshot = new ArrayList<>(MainActivity.tabs);

        for (BrowserTab tab : tabSnapshot) {
            String title = tab.title != null && !tab.title.isEmpty() ? tab.title : "New Tab";
            if (tab.id == MainActivity.currentTabId) title = "\u25B6 " + title;
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
                closeTabId = tabSnapshot.get(position).id;
                Intent i = new Intent(this, ConfirmActivity.class);
                i.putExtra("message", "Close \"" + tabSnapshot.get(position).title + "\"?");
                startActivityForResult(i, REQ_CLOSE);
            }
            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CLOSE && resultCode == Activity.RESULT_OK && closeTabId >= 0) {
            for (int i = 0; i < MainActivity.tabs.size(); i++) {
                if (MainActivity.tabs.get(i).id == closeTabId) {
                    MainActivity.tabs.get(i).webView.destroy();
                    MainActivity.tabs.remove(i);
                    break;
                }
            }
            closeTabId = -1;
            loadTabs();
        }
    }
}
