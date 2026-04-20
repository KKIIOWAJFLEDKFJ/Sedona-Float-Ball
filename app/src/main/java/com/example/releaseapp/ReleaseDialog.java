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
    private int releaseMode;

    private TextView question;
    private Button btn1, btn2, btn3;

//    private int step = 0;
    private boolean isComplexMode; // 模式标识

//    private TextView question;
//    private Button btn1, btn2, btn3;
    private TextView todayCount;
    private TextView mockToast;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public ReleaseDialog(Context context) {

        super(context);
        // 从存储中读取用户选择的模式
        releaseMode = context.getSharedPreferences("config", Context.MODE_PRIVATE)
                .getInt("release_mode", 1);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_release_dialog);

        Window window = getWindow();
        if (window != null) {
            // 1. 设置 Dialog 弹出动画（可选，增加丝滑感）
            window.setWindowAnimations(android.R.style.Animation_Dialog);

            // 2. 获取窗口参数
            WindowManager.LayoutParams lp = window.getAttributes();

            // 3. 将宽度设置为屏幕宽度的 90% 或 95%，避免紧贴边缘
            // 或者直接用 MATCH_PARENT，然后在 XML 里的外层 LinearLayout 加 padding
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            window.setAttributes(lp);
        }


        // 初始化控件
        question = findViewById(R.id.question);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        todayCount = findViewById(R.id.today_count);
        mockToast = findViewById(R.id.mock_toast);

        // 1. 找到显示目标的 TextView
        TextView tvTargetDisplay = findViewById(R.id.tv_target_display);

// 2. 从存储中获取目标
        String savedTarget = getContext().getSharedPreferences("config", Context.MODE_PRIVATE)
                .getString("current_target", "");

// 3. 逻辑判断：如果目标为空，则隐藏控件
        if (savedTarget != null && !savedTarget.trim().isEmpty()) {
            tvTargetDisplay.setVisibility(View.VISIBLE);
            tvTargetDisplay.setText("🎯 目标：" + savedTarget);
        } else {
            // 💡 强迫症必备：设为 GONE，这样它就不会占据布局空间，下面的问题会顶上来
            tvTargetDisplay.setVisibility(View.GONE);
        }

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
        question.setAlpha(0f);
        question.animate().alpha(1f).setDuration(200);

        // 💡 修改判断逻辑
        if (releaseMode == 0) {
            updateStepDirect();
        } else if (releaseMode == 1) {
            updateStepSimple();
        } else {
            updateStepComplex();
        }
    }

    private void updateStepDirect() {
        // 无论第几步（其实只有一步），都显示三大想要
        question.setText("现在是什么想要？");
        btn3.setVisibility(View.VISIBLE);
        btn1.setText("想要控制");
        btn2.setText("想要被认同");
        btn3.setText("想要安全");
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
        if (releaseMode == 0) {
            // 直释版：点任何按钮都直接算释放成功
            processSuccess();
        } else if (releaseMode == 1) {
            handleAnswerSimple(buttonIndex);
        } else {
            handleAnswerComplex(buttonIndex);
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