package com.szprimary.english.desktop;

import com.szprimary.english.desktop.pages.HomePage;
import com.szprimary.english.desktop.ui.AppFrame;
import com.szprimary.english.desktop.ui.Ui;

import java.awt.Font;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** PC 版入口。 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                start(DesktopRepo.createDefault());
            }
        });
    }

    public static AppFrame start(DesktopRepo repo) {
        lookAndFeel();
        AppFrame frame = new AppFrame(repo);
        frame.nav().push(new HomePage(frame.nav()));
        frame.setVisible(true);
        return frame;
    }

    /** 系统外观（对话框、滚动条与系统一致），并把对话框字体换成中文字体。 */
    public static void lookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // 用 Swing 默认外观
        }
        Font font = Ui.font(14, false);
        String[] keys = {"OptionPane.messageFont", "OptionPane.buttonFont", "Button.font", "Label.font",
                "TextField.font", "ToolTip.font"};
        for (int i = 0; i < keys.length; i++) {
            UIManager.put(keys[i], font);
        }
        UIManager.put("OptionPane.okButtonText", "确定");
        UIManager.put("OptionPane.cancelButtonText", "取消");
        UIManager.put("OptionPane.yesButtonText", "是");
        UIManager.put("OptionPane.noButtonText", "否");
    }
}
