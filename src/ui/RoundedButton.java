package ui;

import javax.swing.*;
import java.awt.*;

/**
 * 圆角矩形按钮 — 纯绘制，无锯齿边缘
 *
 * 用法：
 *   RoundedButton btn = new RoundedButton("登录", 0xd4a04a);
 *   btn.setForeground(Color.WHITE);
 */
public class RoundedButton extends JButton {

    private final int arc;
    private final Color bgColor;
    private boolean hovered;

    public RoundedButton(String text, int bgHex) {
        this(text, bgHex, 12);
    }

    public RoundedButton(String text, int bgHex, int arc) {
        super(text);
        this.arc = arc;
        this.bgColor = new Color(bgHex);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(6, 18, 6, 18));
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                hovered = true;
                repaint();
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // 背景
        g2.setColor(hovered ? bgColor.brighter() : bgColor);
        g2.fillRoundRect(0, 0, w, h, arc, arc);

        // 文字居中
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (w - fm.stringWidth(getText())) / 2;
        int textY = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width + 20, 80);
        d.height = Math.max(d.height + 8, 32);
        return d;
    }
}
