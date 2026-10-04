package com.szprimary.english.desktop.pages;

import com.szprimary.english.desktop.DesktopRepo;
import com.szprimary.english.desktop.ui.Navigator;
import com.szprimary.english.desktop.ui.HBox;
import com.szprimary.english.desktop.ui.Page;
import com.szprimary.english.desktop.ui.Ui;
import com.szprimary.english.desktop.ui.VBox;

import javax.swing.JComponent;

/** 页面公共部分：窗口、数据和几个常用的小组件。 */
abstract class BasePage implements Page {

    protected final Navigator nav;
    protected final DesktopRepo repo;

    BasePage(Navigator nav) {
        this.nav = nav;
        this.repo = nav.repo();
    }

    @Override
    public String subtitle() {
        return null;
    }

    /** 数字 + 说明的统计块。 */
    static JComponent statBlock(String value, String label, float size) {
        VBox block = Ui.column(2);
        block.add(Ui.centered(value, size, Ui.PRIMARY, true));
        block.add(Ui.centered(label, 12, Ui.SUB, false));
        return Ui.weight(block, 1f);
    }

    /** 带标题、说明和右箭头的可点击行。 */
    static JComponent entry(String title, String desc, Runnable action) {
        com.szprimary.english.desktop.ui.Card row = new com.szprimary.english.desktop.ui.Card(Ui.CARD, null, 10, 10, 0);
        HBox line = Ui.row(10);
        VBox texts = Ui.column(2);
        texts.add(Ui.text(title, 16, Ui.TEXT, true));
        texts.add(Ui.text(desc, 12, Ui.SUB, false));
        line.add(Ui.weight(texts, 1f));
        line.add(Ui.label("›", 22, Ui.SUB, false));
        row.add(line);
        Ui.clickable(row, action);
        return row;
    }

    /** 词条行：英文 + 音标，下面一行词性和中文，右侧状态。 */
    static JComponent wordRow(String top, String bottom, String right, java.awt.Color rightColor, boolean highlight) {
        HBox line = Ui.row(10);
        line.setBorder(new javax.swing.border.EmptyBorder(7, 0, 7, 0));
        VBox texts = Ui.column(2);
        texts.add(Ui.text(top, 15, highlight ? Ui.PRIMARY : Ui.TEXT, true));
        texts.add(Ui.text(bottom, 13, Ui.SUB, false));
        line.add(Ui.weight(texts, 1f));
        line.add(Ui.label(right, right.length() <= 1 ? 16 : 12, rightColor, right.length() <= 1));
        return line;
    }

    void speakOrWarn(String text) {
        if (repo.speaker().isAvailable()) {
            repo.speaker().speak(text);
        } else {
            nav.toast("本机暂无可用的英语语音引擎，详见「关于与设置」");
        }
    }
}
