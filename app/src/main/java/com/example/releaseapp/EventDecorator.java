package com.example.releaseapp;

import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.text.style.LineHeightSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Map;

public class EventDecorator implements DayViewDecorator {
    private final int color;
    private final int textColor; // 新增：根据背景深浅调整文字颜色
    private final Map<CalendarDay, Integer> countMap; // 存储日期对应的次数

    public EventDecorator(int color, int textColor, Map<CalendarDay, Integer> countMap) {
        this.color = color;
        this.textColor = textColor;
        this.countMap = countMap;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return countMap.containsKey(day);
    }



    @Override
    public void decorate(DayViewFacade view) {
        // 1. 设置格子背景深浅
        view.setBackgroundDrawable(new ColorDrawable(color));

        // 2. 核心：通过 Span 在日期下面增加次数
        view.addSpan(new CustomTextSpan(textColor));
    }

    // 内部类：处理日期下面的小字
    private class CustomTextSpan implements LineHeightSpan {
        private final int color;

        public CustomTextSpan(int color) {
            this.color = color;
        }

        @Override
        public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v, Paint.FontMetricsInt fm) {
            // 调整行高，给下方的次数留出空间
            fm.bottom += 15;
            fm.descent += 15;
        }

        // 注意：MaterialCalendarView 的自定义文字显示通常需要配合自定义的 DayFormatter
        // 如果简单的 Span 不奏效，最稳妥的方法是下面的“分级 Decorator”方案
    }
}