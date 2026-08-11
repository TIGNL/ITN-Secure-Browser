package com.itn.securebrowser.util;

import java.util.Calendar;
import java.util.List;

public class BlockEngine {

    private final BlockDataStore dataStore;
    private final TimeTracker timeTracker;

    public BlockEngine(BlockDataStore dataStore, TimeTracker timeTracker) {
        this.dataStore = dataStore;
        this.timeTracker = timeTracker;
    }

    public static abstract class BlockReason {
        public static class ScheduleBlock extends BlockReason {}
        public static class LimitReached extends BlockReason {
            public final int limitMinutes;
            public final int usedMinutes;
            public LimitReached(int limitMinutes, int usedMinutes) {
                this.limitMinutes = limitMinutes;
                this.usedMinutes = usedMinutes;
            }
        }
    }

    public BlockReason check(String domain) {
        BlockDataStore.BlockMatch match = dataStore.findDomain(domain);
        if (match == null) return null;

        java.util.HashMap<String, Integer> dailyLimits;
        List<BlockDataStore.BlockSchedule> schedules;

        if (match instanceof BlockDataStore.BlockMatch.GroupMatch) {
            BlockDataStore.BlockGroup g = ((BlockDataStore.BlockMatch.GroupMatch) match).group;
            dailyLimits = g.dailyLimits;
            schedules = g.schedules;
        } else {
            BlockDataStore.BlockSite s = ((BlockDataStore.BlockMatch.SiteMatch) match).site;
            dailyLimits = s.dailyLimits;
            schedules = s.schedules;
        }

        if (isInBlockSchedule(schedules)) return new BlockReason.ScheduleBlock();

        Integer limitMins = dailyLimits.get(todayDayName());
        if (limitMins == null) return null;
        int usedMins = (int) (timeTracker.getTodaySeconds(domain) / 60L);
        if (usedMins >= limitMins) return new BlockReason.LimitReached(limitMins, usedMins);

        return null;
    }

    private boolean isInBlockSchedule(List<BlockDataStore.BlockSchedule> schedules) {
        Calendar cal = Calendar.getInstance();
        String today = dayName(cal.get(Calendar.DAY_OF_WEEK));
        int nowMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);

        Calendar yCal = Calendar.getInstance();
        yCal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = dayName(yCal.get(Calendar.DAY_OF_WEEK));

        for (BlockDataStore.BlockSchedule s : schedules) {
            int from = parseTime(s.from);
            int to = parseTime(s.to);
            if (from <= to) {
                if (s.days.contains(today) && nowMins >= from && nowMins <= to) return true;
            } else {
                if ((s.days.contains(today) && nowMins >= from) ||
                    (s.days.contains(yesterday) && nowMins <= to)) return true;
            }
        }
        return false;
    }

    private int parseTime(String hhmm) {
        String[] parts = hhmm.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private String todayDayName() {
        return dayName(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
    }

    private String dayName(int calDay) {
        switch (calDay) {
            case Calendar.SATURDAY: return "SATURDAY";
            case Calendar.SUNDAY: return "SUNDAY";
            case Calendar.MONDAY: return "MONDAY";
            case Calendar.TUESDAY: return "TUESDAY";
            case Calendar.WEDNESDAY: return "WEDNESDAY";
            case Calendar.THURSDAY: return "THURSDAY";
            case Calendar.FRIDAY: return "FRIDAY";
            default: return "MONDAY";
        }
    }

    public static String buildBlockPage(String domain, BlockReason reason) {
        String title, message, icon;
        if (reason instanceof BlockReason.ScheduleBlock) {
            title = "Scheduled block";
            message = "This site is blocked during this time.";
            icon = "\uD83D\uDD50";
        } else {
            BlockReason.LimitReached lr = (BlockReason.LimitReached) reason;
            title = "Daily limit reached";
            message = "You used " + lr.usedMinutes + " of " + lr.limitMinutes + " allowed minutes today.";
            icon = "\u23F1\uFE0F";
        }
        return "<!DOCTYPE html><html dir=\"ltr\" lang=\"en\"><head>" +
            "<meta charset=\"UTF-8\"/><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"/>" +
            "<title>Blocked</title><style>" +
            "*{margin:0;padding:0;box-sizing:border-box}" +
            "body{min-height:100vh;display:flex;align-items:center;justify-content:center;background:#0f0f0f;font-family:'Segoe UI',Tahoma,sans-serif;color:#e0e0e0}" +
            ".card{background:#1a1a1a;border:1px solid #2a2a2a;border-radius:20px;padding:40px 32px;max-width:360px;width:90%;text-align:center;box-shadow:0 8px 32px rgba(0,0,0,0.5)}" +
            ".icon{font-size:64px;margin-bottom:20px;display:block}" +
            ".title{font-size:22px;font-weight:700;color:#fff;margin-bottom:12px}" +
            ".domain{font-size:14px;color:#888;margin-bottom:20px}" +
            ".message{font-size:15px;color:#aaa;line-height:1.7}" +
            ".divider{width:40px;height:2px;background:linear-gradient(90deg,#444,transparent);margin:24px auto;border-radius:2px}" +
            "</style></head><body><div class=\"card\">" +
            "<span class=\"icon\">" + icon + "</span>" +
            "<div class=\"title\">" + title + "</div>" +
            "<div class=\"domain\">" + domain + "</div>" +
            "<div class=\"divider\"></div>" +
            "<div class=\"message\">" + message + "</div>" +
            "</div></body></html>";
    }
}
