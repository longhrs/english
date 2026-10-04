package com.szprimary.english.desktop.ui;

import com.szprimary.english.desktop.DesktopRepo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

/**
 * 顶部标题栏 + 页面区，用页面栈实现前进 / 返回。
 * 它只是一个面板，不依赖窗口，单元测试可以在无显示器（headless）环境里驱动全部页面。
 */
public final class Navigator extends JPanel {

    private static final class Entry {
        final Page page;
        int scroll;

        Entry(Page page) {
            this.page = page;
        }
    }

    private final DesktopRepo repo;
    private final Deque<Entry> stack = new ArrayDeque<Entry>();
    private final JPanel body = new JPanel(new BorderLayout());
    private final JLabel titleLabel = new JLabel();
    private final JLabel subtitleLabel = new JLabel();
    private final RoundButton back;
    private final JLabel toast = new JLabel("", SwingConstants.CENTER);
    private Timer toastTimer;
    private JScrollPane scroll;
    private String lastToast;
    private Component focusTarget;

    public Navigator(DesktopRepo repo) {
        super(new BorderLayout());
        this.repo = repo;
        setBackground(Ui.BG);
        // 默认由面板自己持有焦点，避免页面上第一个按钮一出现就带着焦点框
        setFocusable(true);

        back = new RoundButton("‹ 返回", Ui.PRIMARY_DARK, Ui.WHITE, null);
        back.setFocusable(false);
        back.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pop();
            }
        });

        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setBackground(Ui.PRIMARY);
        header.setBorder(new EmptyBorder(12, 18, 12, 22));
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titleLabel.setFont(Ui.font(21, true));
        titleLabel.setForeground(Ui.WHITE);
        subtitleLabel.setFont(Ui.font(13, false));
        subtitleLabel.setForeground(new Color(255, 255, 255, 215));
        titles.add(titleLabel, BorderLayout.CENTER);
        titles.add(subtitleLabel, BorderLayout.SOUTH);
        header.add(back, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        body.setBackground(Ui.BG);
        add(body, BorderLayout.CENTER);

        toast.setFont(Ui.font(14, false));
        toast.setForeground(Ui.WHITE);
        toast.setOpaque(true);
        toast.setBackground(new Color(0x2B303B));
        toast.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        toast.setVisible(false);
    }

    public DesktopRepo repo() {
        return repo;
    }

    /** 打开新页面。 */
    public void push(Page page) {
        if (!stack.isEmpty()) {
            stack.peek().scroll = currentScroll();
        }
        stack.push(new Entry(page));
        render(0);
    }

    /** 关闭当前页面并打开另一个（对应 Android 的 startActivity + finish）。 */
    public void replace(Page page) {
        if (!stack.isEmpty()) {
            stack.pop();
        }
        stack.push(new Entry(page));
        render(0);
    }

    /** 返回上一页，上一页重新 build 以反映最新进度（对应 onResume）。 */
    public void pop() {
        if (stack.size() <= 1) {
            return;
        }
        stack.pop();
        render(stack.peek().scroll);
    }

    /** 重新 build 当前页面，保留滚动位置。 */
    public void refresh() {
        render(currentScroll());
    }

    /** 重新 build 当前页面并回到顶部。 */
    public void refreshToTop() {
        render(0);
    }

    public Page current() {
        return stack.isEmpty() ? null : stack.peek().page;
    }

    public int depth() {
        return stack.size();
    }

    public String titleText() {
        return titleLabel.getText();
    }

    public String subtitleText() {
        return subtitleLabel.getText();
    }

    /** 当前页面内容的根组件（测试用）。 */
    public Component pageContent() {
        if (scroll == null) {
            return null;
        }
        Component view = scroll.getViewport().getView();
        return view instanceof ScrollColumn ? ((ScrollColumn) view).getComponent(0) : view;
    }

    /** 页面 build 时调用：渲染完成后把键盘焦点交给这个组件（如拼写输入框）。 */
    public void focusLater(Component component) {
        focusTarget = component;
    }

    public String lastToast() {
        return lastToast;
    }

    private int currentScroll() {
        return scroll == null ? 0 : scroll.getVerticalScrollBar().getValue();
    }

    private void render(final int scrollTo) {
        Entry entry = stack.peek();
        if (entry == null) {
            return;
        }
        VBox content = Ui.column(14);
        focusTarget = null;
        entry.page.build(content);
        final Component focus = focusTarget != null ? focusTarget : this;
        titleLabel.setText(entry.page.title());
        String sub = entry.page.subtitle();
        subtitleLabel.setText(sub == null ? "" : sub);
        subtitleLabel.setVisible(sub != null && sub.length() > 0);
        back.setVisible(stack.size() > 1);

        scroll = new JScrollPane(new ScrollColumn(content));
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Ui.BG);
        scroll.getVerticalScrollBar().setUnitIncrement(28);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        body.removeAll();
        body.add(scroll, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
        final JScrollPane target = scroll;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                target.getViewport().doLayout();
                target.getVerticalScrollBar().setValue(scrollTo);
                focus.requestFocusInWindow();
            }
        });
    }

    /** 底部短暂提示，对应 Android 的 Toast。 */
    public void toast(String message) {
        lastToast = message;
        JRootPane root = SwingUtilities.getRootPane(this);
        if (root == null) {
            return;
        }
        JLayeredPane layers = root.getLayeredPane();
        if (toast.getParent() != layers) {
            layers.add(toast, JLayeredPane.POPUP_LAYER);
        }
        toast.setText(message);
        Dimension d = toast.getPreferredSize();
        int w = Math.min(d.width, layers.getWidth() - 40);
        toast.setBounds((layers.getWidth() - w) / 2, layers.getHeight() - d.height - 36, w, d.height);
        toast.setVisible(true);
        if (toastTimer != null) {
            toastTimer.stop();
        }
        toastTimer = new Timer(2600, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toast.setVisible(false);
            }
        });
        toastTimer.setRepeats(false);
        toastTimer.start();
    }

    /** 处理按键：Esc / Alt+← 返回，其余交给当前页面。正在输入文字时不拦截。 */
    public boolean onKey(KeyEvent e) {
        Component focus = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        boolean typing = focus instanceof JTextComponent && ((JTextComponent) focus).isEditable();
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE || (e.getKeyCode() == KeyEvent.VK_LEFT && e.isAltDown())) {
            if (stack.size() > 1) {
                pop();
                return true;
            }
            return false;
        }
        if (typing) {
            return false;
        }
        Page page = current();
        return page != null && page.handleKey(e);
    }
}
