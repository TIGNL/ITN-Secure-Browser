package com.itn.securebrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import com.itn.securebrowser.util.BlockDataStore;

public class GroupListActivity extends BaseActivity {

    private static final int REQ_DELETE = 1;
    private BlockDataStore dataStore;
    private ListView groupList;
    private TextView emptyText;
    private List<BlockDataStore.BlockGroup> groups;
    private int deletePosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        ((TextView) findViewById(R.id.pageTitle)).setText(R.string.groups_title);

        dataStore = new BlockDataStore(this);
        groupList = findViewById(R.id.groupList);
        emptyText = findViewById(R.id.emptyText);

        groupList.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, GroupEditActivity.class);
            i.putExtra("groupName", groups.get(position).name);
            startActivity(i);
        });

        groupList.setOnItemLongClickListener((parent, view, position, id) -> {
            deletePosition = position;
            BlockDataStore.BlockGroup group = groups.get(position);
            Intent i = new Intent(this, ConfirmActivity.class);
            i.putExtra("title", "Delete group");
            i.putExtra("message", getString(R.string.delete_group_confirm, group.name));
            i.putExtra("confirmText", getString(R.string.btn_delete));
            startActivityForResult(i, REQ_DELETE);
            return true;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DELETE && resultCode == Activity.RESULT_OK && deletePosition >= 0) {
            dataStore.deleteGroup(groups.get(deletePosition).name);
            deletePosition = -1;
            loadGroups();
        }
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
