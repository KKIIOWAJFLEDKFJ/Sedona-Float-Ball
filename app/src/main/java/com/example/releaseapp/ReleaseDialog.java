package com.example.releaseapp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

public class ReleaseDialog extends Dialog {

    private int step = 0;
    private boolean isComplexMode; // 模式标识

    private TextView question;
    private Button btn1, btn2, btn3;
    private TextView todayCount;
    private TextView mockToast;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ReleaseDialog(Context context) {
        super(context);
        // 从存储中读取用户选择的模式
        isComplexMode = context.getSharedPreferences("config", Context.MODE_PRIVATE)
                .getBoolean("is_complex", true);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_release_dialog);

        // 初始化控件
        question = findViewById(R.id.question);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        todayCount = findViewById(R.id.today_count);
        mockToast = findViewById(R.id.mock_toast);

        // 初始化显示
        updateStep();
        updateCount();

        // 按钮监听
        btn1.setOnClickListener(v -> handleAnswer(1));
        btn2.setOnClickListener(v -> handleAnswer(2));
        btn3.setOnClickListener(v -> handleAnswer(3));
    }

    private void updateCount() {
        todayCount.setText("今天释放：" + ReleaseStats.getTodayCount(getContext()) + "次");
    }

    private void updateStep() {
        // 简单的文字切换动画
        question.setAlpha(0f);
        question.animate().alpha(1f).setDuration(200);

        if (isComplexMode) {
            updateStepComplex();
        } else {
            updateStepSimple();
        }
    }

    /**
     * 简单版逻辑 (3步)
     */
    private void updateStepSimple() {
        switch (step) {
            case 0:
                question.setText("现在是什么想要？");
                btn3.setVisibility(View.VISIBLE);
                btn1.setText("想要控制");
                btn2.setText("想要被认同");
                btn3.setText("想要安全");
                break;
            case 1:
                question.setText("允许它离开吗？");
                btn3.setVisibility(View.GONE);
                btn1.setText("允许");
                btn2.setText("不允许");
                break;
            case 2:
                question.setText("离开了吗？");
                btn3.setVisibility(View.VISIBLE);
                btn1.setText("离开了");
                btn2.setText("还有一些");
                btn3.setText("卡住了");
                break;
        }
    }

    /**
     * 复杂版逻辑 (6步)
     */
    private void updateStepComplex() {
        switch (step) {
            case 0:
                question.setText("现在是什么想要？");
                btn3.setVisibility(View.VISIBLE);
                btn1.setText("想要控制");
                btn2.setText("想要被认同");
                btn3.setText("想要安全");
                break;
            case 1:
                question.setText("允许它存在吗？");
                btn3.setVisibility(View.GONE);
                btn1.setText("允许");
                btn2.setText("不允许");
                break;
            case 2:
                question.setText("能放下吗？");
                btn1.setText("能");
                btn2.setText("不能");
                break;
            case 3:
                question.setText("愿意放下吗？");
                btn1.setText("愿意");
                btn2.setText("不愿意");
                break;
            case 4:
                question.setText("什么时候放下？");
                btn1.setText("现在");
                btn2.setText("再等等");
                break;
            case 5:
                question.setText("现在感觉还在吗？");
                btn3.setVisibility(View.VISIBLE);
                btn1.setText("完全释放");
                btn2.setText("还有一些");
                btn3.setText("卡住了");
                break;
        }
    }

    private void handleAnswer(int buttonIndex) {
        if (isComplexMode) {
            handleAnswerComplex(buttonIndex);
        } else {
            handleAnswerSimple(buttonIndex);
        }
    }

    private void handleAnswerSimple(int buttonIndex) {
        if (step == 2) {
            if (buttonIndex == 1) { // 离开了
                processSuccess();
                return;
            } else if (buttonIndex == 2) { // 还有一些
                step = 1;
            } else { // 卡住了
                step = 0;
            }
        } else {
            step++;
        }
        updateStep();
    }

    private void handleAnswerComplex(int buttonIndex) {
        if (step == 5) {
            if (buttonIndex == 1) { // 完全释放
                processSuccess();
                return;
            } else if (buttonIndex == 2) { // 还有一些
                step = 1;
            } else { // 卡住了
                step = 0;
            }
        } else {
            step++;
        }
        updateStep();
    }

    /**
     * 成功释放的统一处理
     */
    private void processSuccess() {
        ReleaseStats.addRelease(getContext());
        setButtonsEnabled(false);

        // 传入跳转逻辑作为回调
        showSuccessEffect(() -> {
            step = 0;
            updateCount();
            updateStep();
            setButtonsEnabled(true);
        });
    }

    private void setButtonsEnabled(boolean enabled) {
        btn1.setEnabled(enabled);
        btn2.setEnabled(enabled);
        btn3.setEnabled(enabled);
    }

    private void showSuccessEffect(Runnable onFinished) {
        Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null) v.vibrate(50);

        if (mockToast != null) {
            mockToast.setVisibility(View.VISIBLE);
            mockToast.setAlpha(0f);
            mockToast.setTranslationY(20f);

            mockToast.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        // 停留 800 毫秒后开始消失
                        mainHandler.postDelayed(() -> {
                            mockToast.animate()
                                    .alpha(0f)
                                    .setDuration(300)
                                    .withEndAction(() -> {
                                        mockToast.setVisibility(View.GONE);
                                        // 💡 动画彻底结束后执行跳转逻辑
                                        if (onFinished != null) onFinished.run();
                                    })
                                    .start();
                        }, 800);
                    })
                    .start();
        }
    }
}