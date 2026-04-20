package com.example.releaseapp;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReleaseStats {
    private static final String PREF_NAME = "release_stats_permanent";

    // 增加一次释放
    public static void addRelease(Context context) {
        String dateKey = getCurrentDateKey();
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int currentCount = prefs.getInt(dateKey, 0);
        prefs.edit().putInt(dateKey, currentCount + 1).apply();
    }

    // 获取特定日期的释放次数
    public static int getCountByDate(Context context, String dateKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(dateKey, 0);
    }

    // 获取今天的日期 Key (yyyy-MM-dd)
    public static String getCurrentDateKey() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    // 为了兼容之前的 tvTodayCount 显示
    public static int getTodayCount(Context context) {
        return getCountByDate(context, getCurrentDateKey());
    }
}