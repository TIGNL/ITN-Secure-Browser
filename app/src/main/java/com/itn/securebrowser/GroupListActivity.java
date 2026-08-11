package com.itn.securebrowser;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import com.itn.securebrowser.util.BlockDataStore;

public class GroupListActivity extends BaseActivity {

    private BlockDataStore dataStore;
    private ListView groupList;
    private TextView emptyText;
    private List<BlockDataStore.BlockGroup> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        dataStore = new BlockDataStore(this);
        groupList = findViewById(R.id.groupList);
        emptyText = findViewById(R.id.emptyText);
        ImageButton btnAdd = findViewById(R.id.btnAddGroup);

        btnAdd.setOnClickListener(v -> {
            Intent i = new Intent(this, GroupEditActivity.class);
            i.putExtra("isNew", true);
            startActivity(i);
        });

        groupList.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, GroupEditActivity.class);
            i.putExtra("groupName", groups.get(position).name);
            startActivity(i);
        });

        groupList.setOnItemLongClickListener((parent, view, position, id) -> {
            BlockDataStore.BlockGroup group = groups.get(position);
            new AlertDialog.Builder(this)
                .setTitle("Delete group")
                .setMessage(getString(R.string.delete_group_confirm, group.name))
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    dataStore.deleteGroup(group.name);
                    loadGroups();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroups();
    }

    private void loadGroups() {
        groups = dataStore.getGroups();
        if (groups.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            groupList.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            groupList.setVisibility(View.VISIBLE);
            List<String> names = new ArrayList<>();
            for (BlockDataStore.BlockGroup g : groups) {
                names.add(g.name + "  (" + g.domains.size() + " domains)");
            }
            groupList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names));
        }
    }
}
