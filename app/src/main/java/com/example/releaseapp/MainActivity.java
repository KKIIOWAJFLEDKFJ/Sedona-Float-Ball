package com.example.releaseapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView tvSelectedDateStat;
    private EditText etTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 初始化控件 (注意类型必须是 MaterialCalendarView)
        calendarView = findViewById(R.id.calendar_view);
        tvSelectedDateStat = findViewById(R.id.tv_selected_date_stat);
        etTarget = findViewById(R.id.et_release_target); // 别忘了在布局里加这个ID

        // 权限检查
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        // 按钮点击事件
        findViewById(R.id.btn_mode_direct).setOnClickListener(v -> saveModeAndStart(0));
        findViewById(R.id.btn_mode_simple).setOnClickListener(v -> saveModeAndStart(1));
        findViewById(R.id.btn_mode_complex).setOnClickListener(v -> saveModeAndStart(2));

        // 2. MaterialCalendarView 的监听器和原生不一样，改成这个：
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            String dateKey = String.format(Locale.getDefault(), "%04d-%02d-%02d",
                    date.getYear(), date.getMonth(), date.getDay());
            int count = ReleaseStats.getCountByDate(MainActivity.this, dateKey);

            if (count > 0) {
                tvSelectedDateStat.setText(dateKey + " 释放了 " + count + " 次");
                tvSelectedDateStat.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                tvSelectedDateStat.setText(dateKey + " 尚未进行释放练习");
                tvSelectedDateStat.setTextColor(Color.parseColor("#999999"));
            }
        });

        refreshCalendar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCalendar();
    }

    private void refreshCalendar() {
        // 💡 重点：先清空旧的装饰器，防止重复叠加
        calendarView.removeDecorators();
        SharedPreferences prefs = getSharedPreferences("release_stats_permanent", MODE_PRIVATE);

        // 用 Map 存储，方便 Decorator 读取对应的次数
        Map<CalendarDay, Integer> allData = new HashMap<>();


//        SharedPreferences prefs = getSharedPreferences("release_stats_permanent", MODE_PRIVATE);

        // 💡 必须先声明这些 List，否则会报 NullPointerException
        List<CalendarDay> level1 = new ArrayList<>();
        List<CalendarDay> level2 = new ArrayList<>();
        List<CalendarDay> level3 = new ArrayList<>();
        List<CalendarDay> level4 = new ArrayList<>();

        Map<String, ?> allEntries = prefs.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            // 过滤掉目标数据，只处理日期数据
            if (key.contains("_") || !(entry.getValue() instanceof Integer)) continue;

            int count = (Integer) entry.getValue();
            String[] parts = key.split("-");
            if (parts.length == 3) {
                try {
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);

                    CalendarDay calendarDay = CalendarDay.from(year, month, day);

                    if (count >= 1 && count <= 20) level1.add(calendarDay);
                    else if (count <= 50) level2.add(calendarDay);
                    else if (count <= 100) level3.add(calendarDay);
                    else if (count > 150) level4.add(calendarDay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 3. 添加分级颜色装饰器
        calendarView.addDecorators(
                new CustomLevelDecorator(Color.parseColor("#C6E48B"), Color.DKGRAY, level1),  // 浅色用深字
                new CustomLevelDecorator(Color.parseColor("#7BC96F"), Color.DKGRAY, level2),
                new CustomLevelDecorator(Color.parseColor("#239A3B"), Color.WHITE, level3),   // 深色用白字
                new CustomLevelDecorator(Color.parseColor("#196127"), Color.WHITE, level4)    // 墨绿用白字
        );
    }

    class CustomLevelDecorator implements DayViewDecorator {
        private final int bgColor;
        private final int txtColor;
        private final HashSet<CalendarDay> dates;

        public CustomLevelDecorator(int bgColor, int txtColor, List<CalendarDay> dates) {
            this.bgColor = bgColor;
            this.txtColor = txtColor;
            this.dates = new HashSet<>(dates);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setBackgroundDrawable(new ColorDrawable(bgColor));
            // 设置日期文字颜色
            view.addSpan(new ForegroundColorSpan(txtColor));
            // 💡 强迫症的细节：这里可以加一个 DotSpan 或者特殊的 Span 来代表次数
            // 如果要显示具体数字，建议在 CalendarView 外部的 TextView 显示（即你已有的 tv_selected_date_stat）
        }
    }

    private void saveModeAndStart(int mode) {
        // 获取输入框内容
        String target = "";
        if (etTarget != null) {
            target = etTarget.getText().toString().trim();
        }

        // 保存到本地
        getSharedPreferences("config", MODE_PRIVATE).edit()
                .putInt("release_mode", mode)
                .putString("current_target", target) // 存入内容（如果是空字符串，Dialog那边就会隐藏）
                .apply();

        // 启动服务
        startService(new Intent(this, FloatingService.class));
        moveTaskToBack(true);
    }

}