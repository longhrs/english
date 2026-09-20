package com.szprimary.english.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/** 关于与设置：使用说明、内容说明、清空进度。 */
public final class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Ui.PRIMARY_DARK);
        build();
    }

    private void build() {
        final AppRepo repo = AppRepo.get(this);
        LinearLayout content = Ui.page(this, "关于与设置", null);

        LinearLayout about = Ui.card(this);
        about.addView(Ui.text(this, "深圳小学英语", 18, Ui.TEXT, true));
        about.addView(Ui.spacer(this, 4));
        about.addView(Ui.hint(this, "版本 1.0 · 完全离线运行"));
        about.addView(Ui.spacer(this, 10));
        about.addView(Ui.body(this, "面向深圳小学一至六年级学生的英语自学工具，分为两部分："
                + "「知识引导」按话题单元讲解词汇、音标、核心句型和语法；"
                + "「练习验证」自动出题并即时判分，错题自动进入错题本和间隔复习队列。"));
        content.addView(about);

        LinearLayout usage = Ui.card(this);
        usage.addView(Ui.text(this, "建议的使用顺序", 16, Ui.TEXT, true));
        usage.addView(Ui.spacer(this, 8));
        usage.addView(Ui.body(this, "1. 在「知识引导」里逐个学习词卡，听发音、看例句，学会的点「标记已掌握」。"));
        usage.addView(Ui.spacer(this, 4));
        usage.addView(Ui.body(this, "2. 读一遍「句型」和「语法」，理解这一单元要会说的句子。"));
        usage.addView(Ui.spacer(this, 4));
        usage.addView(Ui.body(this, "3. 做一次「练习验证」，目标 90 分以上（三颗星）。"));
        usage.addView(Ui.spacer(this, 4));
        usage.addView(Ui.body(this, "4. 第二天打开「今日复习」和「错题本」，把生词巩固掉。"));
        content.addView(usage);

        LinearLayout content2 = Ui.card(this);
        content2.addView(Ui.text(this, "内容说明", 16, Ui.TEXT, true));
        content2.addView(Ui.spacer(this, 8));
        content2.addView(Ui.body(this, "本应用的词汇、句型和语法按深圳小学常见的话题顺序自行编写整理，"
                + "覆盖问候、颜色数字、家庭、校园、时间、职业、服装、饮食、城市、健康、购物、"
                + "爱好、节日、旅行、运动、科技、环保、文化、安全、小升初等话题，"
                + "并非任何出版社教材的原文摘录，也不隶属于任何学校或机构。"
                + "具体以学校所用教材和老师的要求为准。"));
        content.addView(content2);

        LinearLayout privacy = Ui.card(this);
        privacy.addView(Ui.text(this, "隐私", 16, Ui.TEXT, true));
        privacy.addView(Ui.spacer(this, 8));
        privacy.addView(Ui.body(this, "本应用不申请网络权限，不收集任何个人信息。"
                + "学习进度只保存在本机，卸载应用即删除。"));
        content.addView(privacy);

        LinearLayout tts = Ui.card(this);
        tts.addView(Ui.text(this, "朗读功能", 16, Ui.TEXT, true));
        tts.addView(Ui.spacer(this, 8));
        tts.addView(Ui.body(this, repo.speaker().isReady()
                ? "已检测到系统英语语音引擎，可以使用单词和句子朗读。"
                : "未检测到英语语音引擎。可在系统「设置 → 语言和输入法 → 文字转语音」中安装英语语音后使用朗读。"));
        content.addView(tts);

        Button reset = Ui.button(this, "清空全部学习进度", Ui.RED, 0xFFFFFFFF);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(AboutActivity.this)
                        .setTitle("确认清空？")
                        .setMessage("已学标记、练习成绩、错题本和复习记录都会被删除，且无法恢复。")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确认清空", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                repo.progress().reset();
                                Ui.toast(AboutActivity.this, "学习进度已清空");
                                finish();
                            }
                        })
                        .show();
            }
        });
        content.addView(reset);
    }
}
