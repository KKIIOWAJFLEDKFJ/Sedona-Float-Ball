package com.example.releaseapp;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReleaseStats {

    private static final String PREF_NAME = "release_stats";
    private static final String KEY_PREFIX = "release_count_";

    // 获取今天的日期字符串 (例如: 2023-10-27)
    private static String getTodayDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
    }

    // 增加一次释放
    public static void addRelease(Context context) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String today = getTodayDate();
        int currentCount = sp.getInt(KEY_PREFIX + today, 0);
        sp.edit().putInt(KEY_PREFIX + today, currentCount + 1).apply();
    }

    // 获取指定日期的释放次数
    public static int getCountByDate(Context context, String date) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sp.getInt(KEY_PREFIX + date, 0);
    }

    // 获取今日释放次数
    public static int getTodayCount(Context context) {
        return getCountByDate(context, getTodayDate());
    }

    // 获取过去7天的数据模型
    public static List<DayStat> getLast7DaysStats(Context context) {
        List<DayStat> stats = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd", Locale.getDefault());
        SimpleDateFormat keySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (int i = 0; i < 7; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -i);

            String displayDate = (i == 0) ? "今天" : sdf.format(cal.getTime());
            String keyDate = keySdf.format(cal.getTime());
            int count = getCountByDate(context, keyDate);

            stats.add(new DayStat(displayDate, count));
        }
        return stats;
    }

    // 简单的数据结构
    public static class DayStat {
        public String dateLabel;
        public int count;
        public DayStat(String dateLabel, int count) {
            this.dateLabel = dateLabel;
            this.count = count;
        }
    }
}