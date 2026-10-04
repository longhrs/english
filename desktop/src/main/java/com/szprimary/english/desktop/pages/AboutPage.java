package com.szprimary.english.desktop.pages;

import com.szprimary.english.desktop.DesktopRepo;
import com.szprimary.english.desktop.ui.Navigator;
import com.szprimary.english.desktop.ui.Card;
import com.szprimary.english.desktop.ui.Ui;
import com.szprimary.english.desktop.ui.VBox;

import javax.swing.JOptionPane;

/** 关于与设置：使用说明、内容说明、隐私、朗读状态、清空进度。 */
public final class AboutPage extends BasePage {

    public AboutPage(Navigator nav) {
        super(nav);
    }

    @Override
    public String title() {
        return "关于与设置";
    }

    @Override
    public void build(VBox content) {
        Card about = Ui.card();
        about.add(Ui.text("深圳小学英语 PC 版", 18, Ui.TEXT, true));
        about.add(Ui.spacer(4));
        about.add(Ui.hint("版本 " + DesktopRepo.version() + " · 完全离线运行 · 与 Android 版内容同步"));
        about.add(Ui.spacer(10));
        about.add(Ui.body("面向深圳小学一至六年级学生的英语自学工具，分为两部分："
                + "「知识引导」按话题单元讲解词汇、音标、核心句型和语法；"
                + "「练习验证」自动出题并即时判分，错题自动进入错题本和间隔复习队列。"));
        content.add(about);

        Card usage = Ui.card();
        usage.add(Ui.text("建议的使用顺序", 16, Ui.TEXT, true));
        usage.add(Ui.spacer(8));
        usage.add(Ui.body("1. 在「知识引导」里逐个学习词卡，听发音、看例句，学会的点「标记已掌握」。"));
        usage.add(Ui.spacer(4));
        usage.add(Ui.body("2. 读一遍「句型」和「语法」，理解这一单元要会说的句子。"));
        usage.add(Ui.spacer(4));
        usage.add(Ui.body("3. 做一次「练习验证」，目标 90 分以上（三颗星）。"));
        usage.add(Ui.spacer(4));
        usage.add(Ui.body("4. 第二天打开「今日复习」和「错题本」，把生词巩固掉。"));
        content.add(usage);

        Card keys = Ui.card();
        keys.add(Ui.text("键盘快捷键", 16, Ui.TEXT, true));
        keys.add(Ui.spacer(8));
        keys.add(Ui.body("词卡：← → 切换单词，空格显示释义，M 标记已掌握，S 朗读。"));
        keys.add(Ui.spacer(4));
        keys.add(Ui.body("练习：1–4 或 A–D 选择答案，Enter 提交 / 下一题，连词成句时 Backspace 撤销。"));
        keys.add(Ui.spacer(4));
        keys.add(Ui.body("任意页面：Esc 或 Alt + ← 返回上一页。"));
        content.add(keys);

        Card source = Ui.card();
        source.add(Ui.text("内容说明", 16, Ui.TEXT, true));
        source.add(Ui.spacer(8));
        source.add(Ui.body("三年级已按 2024 年审定的新版沪教版英语课本（上海教育出版社·义务教育教科书）编排："
                + "三年级上册（3A）全部 8 个单元、三年级下册（3B）已核实的 4 个单元、四年级上册（4A）已核实的 2 个单元，"
                + "单元标题与课本一致。新版课本从三年级开始，一、二年级为启蒙内容；其余单元暂作拓展话题"
                + "（对照旧版沪教牛津版·深圳用），新版目录核实后再更新。每个单元的「学习小贴士」里都标注了对应的课本单元。"));
        source.add(Ui.spacer(8));
        source.add(Ui.body("所有文字均为本项目自行撰写，并非任何出版社教材的原文摘录，"
                + "也不隶属于任何学校或机构；教材版次会调整，具体以学校当年所用课本和老师的要求为准。"));
        content.add(source);

        Card privacy = Ui.card();
        privacy.add(Ui.text("隐私与数据", 16, Ui.TEXT, true));
        privacy.add(Ui.spacer(8));
        privacy.add(Ui.body("本程序不联网，不收集任何个人信息。学习进度只保存在这台电脑上，"
                + "与手机上的 Android 版互不影响。"));
        if (repo.progressLocation() != null) {
            privacy.add(Ui.spacer(4));
            privacy.add(Ui.hint("进度文件：" + repo.progressLocation()));
        }
        content.add(privacy);

        Card tts = Ui.card();
        tts.add(Ui.text("朗读功能", 16, Ui.TEXT, true));
        tts.add(Ui.spacer(8));
        tts.add(Ui.body(repo.speaker().status()));
        tts.add(Ui.spacer(10));
        tts.add(Ui.align(Ui.soft("▶ 试听：Hello! Welcome to Shenzhen.", new Runnable() {
            @Override
            public void run() {
                speakOrWarn("Hello! Welcome to Shenzhen.");
                nav.refresh();
            }
        }), "left"));
        content.add(tts);

        content.add(Ui.button("清空全部学习进度", Ui.RED, Ui.WHITE, new Runnable() {
            @Override
            public void run() {
                int choice = JOptionPane.showConfirmDialog(nav,
                        "已学标记、练习成绩、错题本和复习记录都会被删除，且无法恢复。",
                        "确认清空？", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.OK_OPTION) {
                    repo.progress().reset();
                    nav.pop();
                    nav.toast("学习进度已清空");
                }
            }
        }));
    }
}
