package com.example.releaseapp;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvTodayCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTodayCount = findViewById(R.id.tv_main_today_count);

        // 权限检查：如果没权限，跳转设置
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        // 按钮点击事件
        findViewById(R.id.btn_mode_simple).setOnClickListener(v -> saveModeAndStart(false));
        findViewById(R.id.btn_mode_complex).setOnClickListener(v -> saveModeAndStart(true));

        refreshStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 每次回到主页（比如关掉弹窗后），自动刷新统计数字和列表
        refreshStats();
    }

    private void refreshStats() {
        // 1. 更新顶部今日释放次数数字
        int count = ReleaseStats.getTodayCount(this);
        tvTodayCount.setText(String.valueOf(count));

        // 2. 动态更新历史记录列表
        LinearLayout container = findViewById(R.id.history_list_container);
        if (container == null) return;

        container.removeAllViews();

        List<ReleaseStats.DayStat> stats = ReleaseStats.getLast7DaysStats(this);

        for (ReleaseStats.DayStat stat : stats) {
            // 使用系统自带的双行布局作为模板
            View itemView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
            TextView text1 = itemView.findViewById(android.R.id.text1);
            TextView text2 = itemView.findViewById(android.R.id.text2);

            // 设置日期文字样式
            text1.setText(stat.dateLabel);
            text1.setTextSize(15); // SP单位
            text1.setTextColor(Color.parseColor("#333333"));

            // 设置次数文字样式
            text2.setText(stat.count + " 次释放");
            text2.setTextSize(14);
            // 如果次数大于0，用绿色显示，更有成就感
            text2.setTextColor(stat.count > 0 ? Color.parseColor("#4CAF50") : Color.parseColor("#999999"));

            // 设置内边距 (px换算，这里直接写数值)
            itemView.setPadding(40, 30, 40, 30);
            container.addView(itemView);

            // 添加分割线
            View line = new View(this);
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
            lineParams.setMargins(40, 0, 40, 0); // 分割线缩进一点更美观
            line.setLayoutParams(lineParams);
            line.setBackgroundColor(Color.parseColor("#F0F0F0"));
            container.addView(line);
        }
    }

    private void saveModeAndStart(boolean isComplex) {
        // 启动前的二次检查：如果用户刚才跳去设置但没给权限就回来了
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show();
            return;
        }

        // 保存用户选中的模式（简单/深度）
        getSharedPreferences("config", MODE_PRIVATE).edit()
                .putBoolean("is_complex", isComplex).apply();

        // 启动悬浮球服务
        startService(new Intent(this, FloatingService.class));

        // 提示用户
        Toast.makeText(this, "悬浮球已启动", Toast.LENGTH_SHORT).show();

        // 将 App 推到后台，不关闭它，这样数据刷新更快
        moveTaskToBack(true);
    }
}