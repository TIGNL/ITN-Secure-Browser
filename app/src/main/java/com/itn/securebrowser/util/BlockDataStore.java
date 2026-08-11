package com.itn.securebrowser.util;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlockDataStore {

    private static final String KEY_GROUPS = "block_groups";
    private static final String KEY_SITES = "block_sites";
    private final SharedPreferences prefs;

    public BlockDataStore(Context context) {
        prefs = context.getSharedPreferences("itn_block_data", Context.MODE_PRIVATE);
    }

    // --- Data classes ---

    public static class BlockSchedule {
        public final List<String> days;
        public final String from;
        public final String to;

        public BlockSchedule(List<String> days, String from, String to) {
            this.days = days;
            this.from = from;
            this.to = to;
        }
    }

    public static class BlockGroup {
        public final String name;
        public final List<String> domains;
        public final HashMap<String, Integer> dailyLimits;
        public final List<BlockSchedule> schedules;

        public BlockGroup(String name, List<String> domains, HashMap<String, Integer> dailyLimits, List<BlockSchedule> schedules) {
            this.name = name;
            this.domains = domains;
            this.dailyLimits = dailyLimits;
            this.schedules = schedules;
        }
    }

    public static class BlockSite {
        public final String domain;
        public final HashMap<String, Integer> dailyLimits;
        public final List<BlockSchedule> schedules;

        public BlockSite(String domain, HashMap<String, Integer> dailyLimits, List<BlockSchedule> schedules) {
            this.domain = domain;
            this.dailyLimits = dailyLimits;
            this.schedules = schedules;
        }
    }

    public static abstract class BlockMatch {
        public static class GroupMatch extends BlockMatch {
            public final BlockGroup group;
            public GroupMatch(BlockGroup group) { this.group = group; }
        }
        public static class SiteMatch extends BlockMatch {
            public final BlockSite site;
            public SiteMatch(BlockSite site) { this.site = site; }
        }
    }

    // --- Groups ---

    public void saveGroup(BlockGroup group) {
        for (BlockSite s : getSites()) {
            if (group.domains.contains(s.domain)) {
                throw new IllegalStateException("Domain '" + s.domain + "' is an individual site. Remove it first.");
            }
        }
        List<BlockGroup> list = getGroups();
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).name.equals(group.name)) { idx = i; break; }
        }
        if (idx >= 0) list.set(idx, group); else list.add(group);
        prefs.edit().putString(KEY_GROUPS, serializeGroups(list)).apply();
    }

    public List<BlockGroup> getGroups() {
        String json = prefs.getString(KEY_GROUPS, null);
        if (json == null) return new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            List<BlockGroup> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                list.add(deserializeGroup(arr.getJSONObject(i)));
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void deleteGroup(String name) {
        List<BlockGroup> list = new ArrayList<>();
        for (BlockGroup g : getGroups()) {
            if (!g.name.equals(name)) list.add(g);
        }
        prefs.edit().putString(KEY_GROUPS, serializeGroups(list)).apply();
    }

    // --- Sites ---

    public void saveSite(BlockSite site) {
        for (BlockGroup g : getGroups()) {
            if (g.domains.contains(site.domain)) {
                throw new IllegalStateException("Domain '" + site.domain + "' is in group '" + g.name + "'.");
            }
        }
        List<BlockSite> list = getSites();
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).domain.equals(site.domain)) { idx = i; break; }
        }
        if (idx >= 0) list.set(idx, site); else list.add(site);
        prefs.edit().putString(KEY_SITES, serializeSites(list)).apply();
    }

    public List<BlockSite> getSites() {
        String json = prefs.getString(KEY_SITES, null);
        if (json == null) return new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            List<BlockSite> list = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                list.add(deserializeSite(arr.getJSONObject(i)));
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void deleteSite(String domain) {
        List<BlockSite> list = new ArrayList<>();
        for (BlockSite s : getSites()) {
            if (!s.domain.equals(domain)) list.add(s);
        }
        prefs.edit().putString(KEY_SITES, serializeSites(list)).apply();
    }

    // --- Lookup ---

    public BlockMatch findDomain(String domain) {
        for (BlockGroup g : getGroups()) {
            if (g.domains.contains(domain)) return new BlockMatch.GroupMatch(g);
        }
        for (BlockSite s : getSites()) {
            if (s.domain.equals(domain)) return new BlockMatch.SiteMatch(s);
        }
        String[] parts = domain.split("\\.");
        for (int i = 1; i < parts.length - 1; i++) {
            StringBuilder parent = new StringBuilder();
            for (int j = i; j < parts.length; j++) {
                if (j > i) parent.append(".");
                parent.append(parts[j]);
            }
            String p = parent.toString();
            for (BlockGroup g : getGroups()) {
                if (g.domains.contains(p)) return new BlockMatch.GroupMatch(g);
            }
            for (BlockSite s : getSites()) {
                if (s.domain.equals(p)) return new BlockMatch.SiteMatch(s);
            }
        }
        return null;
    }

    // --- Clear ---

    public void clearAll() {
        prefs.edit().clear().apply();
    }

    // --- JSON serialization ---

    private JSONObject serializeSchedule(BlockSchedule s) throws Exception {
        JSONObject o = new JSONObject();
        o.put("days", new JSONArray(s.days));
        o.put("from", s.from);
        o.put("to", s.to);
        return o;
    }

    private JSONObject serializeLimits(HashMap<String, Integer> limits) throws Exception {
        JSONObject o = new JSONObject();
        for (HashMap.Entry<String, Integer> e : limits.entrySet()) {
            o.put(e.getKey(), e.getValue());
        }
        return o;
    }

    private String serializeGroups(List<BlockGroup> list) {
        try {
            JSONArray arr = new JSONArray();
            for (BlockGroup g : list) {
                JSONObject o = new JSONObject();
                o.put("name", g.name);
                o.put("domains", new JSONArray(g.domains));
                o.put("dailyLimits", serializeLimits(g.dailyLimits));
                JSONArray schedArr = new JSONArray();
                for (BlockSchedule s : g.schedules) schedArr.put(serializeSchedule(s));
                o.put("schedules", schedArr);
                arr.put(o);
            }
            return arr.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private String serializeSites(List<BlockSite> list) {
        try {
            JSONArray arr = new JSONArray();
            for (BlockSite s : list) {
                JSONObject o = new JSONObject();
                o.put("domain", s.domain);
                o.put("dailyLimits", serializeLimits(s.dailyLimits));
                JSONArray schedArr = new JSONArray();
                for (BlockSchedule sc : s.schedules) schedArr.put(serializeSchedule(sc));
                o.put("schedules", schedArr);
                arr.put(o);
            }
            return arr.toString();
        } catch (Exception e) {
            return "[]";
        }
    }

    private BlockSchedule deserializeSchedule(JSONObject o) throws Exception {
        JSONArray daysArr = o.getJSONArray("days");
        List<String> days = new ArrayList<>();
        for (int i = 0; i < daysArr.length(); i++) days.add(daysArr.getString(i));
        return new BlockSchedule(days, o.getString("from"), o.getString("to"));
    }

    private HashMap<String, Integer> deserializeLimits(JSONObject o) throws Exception {
        HashMap<String, Integer> map = new HashMap<>();
        java.util.Iterator<String> keys = o.keys();
        while (keys.hasNext()) {
            String k = keys.next();
            map.put(k, o.getInt(k));
        }
        return map;
    }

    private BlockGroup deserializeGroup(JSONObject o) throws Exception {
        JSONArray domainsArr = o.getJSONArray("domains");
        List<String> domains = new ArrayList<>();
        for (int i = 0; i < domainsArr.length(); i++) domains.add(domainsArr.getString(i));
        JSONArray schedArr = o.getJSONArray("schedules");
        List<BlockSchedule> schedules = new ArrayList<>();
        for (int i = 0; i < schedArr.length(); i++) schedules.add(deserializeSchedule(schedArr.getJSONObject(i)));
        return new BlockGroup(o.getString("name"), domains, deserializeLimits(o.getJSONObject("dailyLimits")), schedules);
    }

    private BlockSite deserializeSite(JSONObject o) throws Exception {
        JSONArray schedArr = o.getJSONArray("schedules");
        List<BlockSchedule> schedules = new ArrayList<>();
        for (int i = 0; i < schedArr.length(); i++) schedules.add(deserializeSchedule(schedArr.getJSONObject(i)));
        return new BlockSite(o.getString("domain"), deserializeLimits(o.getJSONObject("dailyLimits")), schedules);
    }
}
