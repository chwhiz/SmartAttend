package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class UIBuilder {

    // Centralized Colors
    public static final Color MAROON    = new Color(138, 26, 19);
    public static final Color GOLD      = new Color(248, 205, 0);
    public static final Color BG        = new Color(245, 243, 240);
    public static final Color CARD_BG   = Color.WHITE;
    public static final Color TEXT_MAIN = new Color(40, 40, 40);
    public static final Color TEXT_DIM  = new Color(130, 130, 130);
    public static final Color BORDER    = new Color(220, 215, 210);
    public static final Color DANGER    = new Color(192, 57, 43);
    public static final Color SUCCESS   = new Color(39, 174, 96);
    
    // Centralized Fonts
    public static final Font FONT_H1    = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_H2    = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_H3    = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY  = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SUB   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_NORM  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BTN   = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_MONO  = new Font("Consolas", Font.BOLD, 16);

    /**
     * Creates a standardized Close ('X') button for title bars.
     * Automatically handles the hover (danger red) and disposes the passed window on click.
     */
    public static JButton createCloseButton(Window window) {
        JButton btnClose = new JButton("✕");
        btnClose.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        btnClose.setForeground(Color.WHITE);
        btnClose.setBackground(MAROON);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setOpaque(true);
        btnClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btnClose.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btnClose.setBackground(DANGER); }
            public void mouseExited(MouseEvent e)  { btnClose.setBackground(MAROON); }
        });
        
        if (window != null) {
            btnClose.addActionListener(e -> window.dispose());
        }
        
        return btnClose;
    }

    /**
     * Creates the standard Gold Bar separator used across title bars.
     */
    public static JPanel createGoldBar() {
        JPanel goldBar = new JPanel();
        goldBar.setBackground(GOLD);
        goldBar.setPreferredSize(new Dimension(0, 3));
        return goldBar;
    }

    /**
     * Creates a standardized Toolbar Button with hover effects.
     */
    public static JButton createToolbarButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.darker()); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });
        
        return btn;
    }
}
