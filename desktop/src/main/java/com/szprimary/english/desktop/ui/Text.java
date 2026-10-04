package com.szprimary.english.desktop.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.swing.JTextArea;

/** 只读、自动换行的文字。中英文混排时按词或按字折行。 */
public class Text extends JTextArea {

    public Text(String text, Font font, Color color) {
        super(text == null ? "" : text);
        setFont(font);
        setForeground(color);
        setEditable(false);
        setFocusable(false);
        setOpaque(false);
        setLineWrap(true);
        setWrapStyleWord(true);
        setBorder(null);
        setHighlighter(null);
        setCursor(Cursor.getDefaultCursor());
    }
}
