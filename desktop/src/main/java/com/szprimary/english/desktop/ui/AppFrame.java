package com.szprimary.english.desktop.ui;

import com.szprimary.english.desktop.DesktopRepo;
import com.szprimary.english.desktop.IconFactory;

import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

/** 主窗口：装一个 Navigator，负责窗口级的快捷键和退出时的收尾。 */
public final class AppFrame extends JFrame {

    private final Navigator nav;

    public AppFrame(final DesktopRepo repo) {
        super("深圳小学英语");
        nav = new Navigator(repo);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setIconImages(IconFactory.windowIcons());
        setMinimumSize(new Dimension(720, 560));
        setSize(1040, 780);
        setLocationRelativeTo(null);
        setContentPane(nav);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                return isActive() && e.getID() == KeyEvent.KEY_PRESSED && nav.onKey(e);
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                repo.progress().save();
                repo.speaker().shutdown();
            }
        });
    }

    public Navigator nav() {
        return nav;
    }
}
