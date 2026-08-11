package com.itn.securebrowser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.itn.securebrowser.util.BlockDataStore;

public class GroupEditActivity extends BaseActivity {

    private static final int REQ_DELETE = 1;
    private static final int REQ_SCHEDULE = 2;

    private BlockDataStore dataStore;
    private String existingName;
    private boolean isNew;

    private EditText inputName, inputLimit, inputDomain;
    private TextView domainError, noSchedules;
    private ListView domainList, scheduleList;
    private TextView btnSave, btnDelete, btnAddSchedule, btnAddDomain;

    private List<String> domains = new ArrayList<>();
    private List<BlockDataStore.BlockSchedule> schedules = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_edit);

        dataStore = new BlockDataStore(this);
        existingName = getIntent().getStringExtra("groupName");
        isNew = getIntent().getBooleanExtra("isNew", false);

        ((TextView) findViewById(R.id.pageTitle)).setText(isNew ? R.string.btn_add : R.string.btn_save);

        inputName = findViewById(R.id.inputName);
        inputLimit = findViewById(R.id.inputLimit);
        inputDomain = findViewById(R.id.inputDomain);
        domainError = findViewById(R.id.domainError);
        domainList = findViewById(R.id.domainList);
        scheduleList = findViewById(R.id.scheduleList);
        noSchedules = findViewById(R.id.noSchedules);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnAddDomain = findViewById(R.id.btnAddDomain);
        btnAddSchedule = findViewById(R.id.btnAddSchedule);

        if (!isNew && existingName != null) {
            for (BlockDataStore.BlockGroup g : dataStore.getGroups()) {
                if (g.name.equals(existingName)) {
                    inputName.setText(g.name);
                    inputName.setEnabled(false);
                    domains.addAll(g.domains);
                    if (!g.dailyLimits.isEmpty()) {
                        Integer first = g.dailyLimits.values().iterator().next();
                        inputLimit.setText(String.valueOf(first));
                    }
                    schedules.addAll(g.schedules);
                    btnDelete.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }

        refreshDomainList();
        refreshScheduleList();

        btnAddDomain.setOnClickListener(v -> addDomain());
        btnAddSchedule.setOnClickListener(v -> {
            Intent i = new Intent(this, ScheduleEditActivity.class);
            startActivityForResult(i, REQ_SCHEDULE);
        });

        btnSave.setOnClickListener(v -> saveGroup());
        btnDelete.setOnClickListener(v -> {
            Intent i = new Intent(this, ConfirmActivity.class);
            i.putExtra("message", getString(R.string.delete_group_confirm, existingName));
            i.putExtra("confirmText", getString(R.string.btn_delete));
            startActivityForResult(i, REQ_DELETE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DELETE && resultCode == Activity.RESULT_OK) {
            dataStore.deleteGroup(existingName);
            finish();
        } else if (requestCode == REQ_SCHEDULE && resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> days = data.getStringArrayListExtra("days");
            String from = data.getStringExtra("from");
            String to = data.getStringExtra("to");
            if (days != null && from != null && to != null) {
                schedules.add(new BlockDataStore.BlockSchedule(days, from, to));
                refreshScheduleList();
            }
        }
    }

    private void addDomain() {
        String domain = inputDomain.getText().toString().trim().toLowerCase();
        domainError.setVisibility(View.GONE);

        if (domain.isEmpty()) {
            domainError.setText(R.string.err_domain_blank);
            domainError.setVisibility(View.VISIBLE);
            return;
        }
        if (!domain.contains(".")) {
            domainError.setText(R.string.err_domain_invalid);
            domainError.setVisibility(View.VISIBLE);
            return;
        }
        if (domains.contains(domain)) {
            domainError.setText(R.string.err_domain_duplicate);
            domainError.setVisibility(View.VISIBLE);
            return;
        }

        domains.add(domain);
        inputDomain.setText("");
        refreshDomainList();
    }

    private void refreshDomainList() {
        domainList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, domains));
        domainList.setOnItemLongClickListener((parent, view, position, id) -> {
            domains.remove(position);
            refreshDomainList();
            return true;
        });
    }

    private void refreshScheduleList() {
        List<String> items = new ArrayList<>();
        for (BlockDataStore.BlockSchedule s : schedules) {
            items.add(String.join(", ", s.days) + "  " + s.from + " - " + s.to);
        }
        scheduleList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        noSchedules.setVisibility(schedules.isEmpty() ? View.VISIBLE : View.GONE);
        scheduleList.setOnItemLongClickListener((parent, view, position, id) -> {
            schedules.remove(position);
            refreshScheduleList();
            return true;
        });
    }

    private void saveGroup() {
        String name = inputName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.err_enter_group_name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (domains.isEmpty()) {
            Toast.makeText(this, "Add at least one domain", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Integer> limits = new HashMap<>();
        String limitStr = inputLimit.getText().toString().trim();
        if (!limitStr.isEmpty()) {
            try {
                int mins = Integer.parseInt(limitStr);
                if (mins <= 0) {
                    Toast.makeText(this, R.string.err_daily_limit_positive, Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] allDays = {"SATURDAY","SUNDAY","MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY"};
                for (String d : allDays) limits.put(d, mins);
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.err_daily_limit_positive, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        try {
            dataStore.saveGroup(new BlockDataStore.BlockGroup(name, domains, limits, schedules));
            finish();
        } catch (IllegalStateException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
