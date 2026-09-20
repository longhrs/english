package com.szprimary.english.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** 统一的配色、字号和控件工厂：整个 App 的界面都用代码构建，不依赖 XML 资源。 */
public final class Ui {

    public static final int PRIMARY = Color.parseColor("#1B6FE3");
    public static final int PRIMARY_DARK = Color.parseColor("#14539F");
    public static final int ACCENT = Color.parseColor("#FF8A3D");
    public static final int BG = Color.parseColor("#F4F6FB");
    public static final int CARD = Color.parseColor("#FFFFFF");
    public static final int TEXT = Color.parseColor("#1F2430");
    public static final int SUB = Color.parseColor("#6B7280");
    public static final int LINE = Color.parseColor("#E3E7EF");
    public static final int SOFT = Color.parseColor("#E8EEFB");
    public static final int GREEN = Color.parseColor("#1EA672");
    public static final int RED = Color.parseColor("#E5484D");
    public static final int GOLD = Color.parseColor("#F5A623");

    private Ui() {
    }

    public static int dp(Context context, float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                context.getResources().getDisplayMetrics()));
    }

    public static GradientDrawable rounded(Context context, int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable outlined(Context context, int stroke, float radiusDp) {
        GradientDrawable drawable = rounded(context, Color.TRANSPARENT, radiusDp);
        drawable.setStroke(dp(context, 1.5f), stroke);
        return drawable;
    }

    public static LinearLayout column(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return layout;
    }

    public static LinearLayout row(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return layout;
    }

    /** 白色圆角卡片，带默认内边距和下外边距。 */
    public static LinearLayout card(Context context) {
        LinearLayout layout = column(context);
        layout.setBackground(rounded(context, CARD, 14));
        int pad = dp(context, 16);
        layout.setPadding(pad, pad, pad, pad);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(context, 12);
        layout.setLayoutParams(params);
        return layout;
    }

    public static TextView text(Context context, String value, float sizeSp, int color, boolean bold) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        view.setTextColor(color);
        view.setTypeface(null, bold ? Typeface.BOLD : Typeface.NORMAL);
        view.setLineSpacing(dp(context, 3), 1f);
        return view;
    }

    public static TextView title(Context context, String value) {
        return text(context, value, 20, TEXT, true);
    }

    public static TextView body(Context context, String value) {
        return text(context, value, 15, TEXT, false);
    }

    public static TextView hint(Context context, String value) {
        return text(context, value, 13, SUB, false);
    }

    public static Button button(Context context, String label, int background, int textColor) {
        Button button = new Button(context);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        button.setTextColor(textColor);
        button.setBackground(rounded(context, background, 12));
        button.setPadding(dp(context, 16), dp(context, 12), dp(context, 16), dp(context, 12));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(context, 8);
        button.setLayoutParams(params);
        button.setStateListAnimator(null);
        return button;
    }

    public static Button primary(Context context, String label) {
        return button(context, label, PRIMARY, Color.WHITE);
    }

    public static Button soft(Context context, String label) {
        return button(context, label, SOFT, PRIMARY);
    }

    public static ProgressBar progress(Context context, int percent) {
        ProgressBar bar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        bar.setProgress(Math.max(0, Math.min(100, percent)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(context, 8));
        params.topMargin = dp(context, 8);
        bar.setLayoutParams(params);
        return bar;
    }

    public static View spacer(Context context, int heightDp) {
        View view = new View(context);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(context, heightDp)));
        return view;
    }

    public static View divider(Context context) {
        View view = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Math.max(1, dp(context, 0.7f)));
        params.topMargin = dp(context, 10);
        params.bottomMargin = dp(context, 10);
        view.setLayoutParams(params);
        view.setBackgroundColor(LINE);
        return view;
    }

    /** 顶部标题栏：返回箭头 + 标题 + 可选右侧说明。 */
    public static LinearLayout header(final Activity activity, String titleText, String right) {
        LinearLayout bar = row(activity);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(PRIMARY);
        int pad = dp(activity, 14);
        bar.setPadding(pad, dp(activity, 14), pad, dp(activity, 14));

        TextView back = text(activity, "←", 22, 0xFFFFFFFF, true);
        back.setPadding(0, 0, dp(activity, 14), 0);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.finish();
            }
        });
        bar.addView(back);

        TextView label = text(activity, titleText, 18, 0xFFFFFFFF, true);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        label.setLayoutParams(labelParams);
        bar.addView(label);

        if (right != null) {
            bar.addView(text(activity, right, 14, 0xCCFFFFFF, false));
        }
        return bar;
    }

    /** 整页骨架：标题栏 + 可滚动内容区，返回内容容器供调用方填充。 */
    public static LinearLayout page(Activity activity, String titleText, String right) {
        LinearLayout root = column(activity);
        root.setBackgroundColor(BG);
        if (titleText != null) {
            root.addView(header(activity, titleText, right));
        }
        ScrollView scroll = new ScrollView(activity);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        scroll.setFillViewport(true);
        LinearLayout content = column(activity);
        int pad = dp(activity, 14);
        content.setPadding(pad, pad, pad, dp(activity, 28));
        scroll.addView(content);
        root.addView(scroll);
        activity.setContentView(root);
        return content;
    }

    public static void toast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static String stars(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append(i < count ? "★" : "☆");
        }
        return sb.toString();
    }
}
