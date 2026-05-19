package ui;

import utils.MusicManager;

import javax.swing.*;
import java.awt.*;

/**
 * 自定义胜负弹窗 — 替代 JOptionPane
 */
public class GameResultDialog extends JDialog {

    public GameResultDialog(JFrame parent, boolean won, int score) {
        super(parent, won ? " 胜利" : " 失败", true);
        setUndecorated(true);
        setSize(320, 220);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(won ? new Color(0x2d4a3e) : new Color(0x4a2d2d));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(won ? new Color(0x4ecdc4) : new Color(0xff6b6b), 3),
                BorderFactory.createEmptyBorder(30, 40, 24, 40)
        ));

        JLabel iconLabel = new JLabel(won ? "  " : "  ", SwingConstants.CENTER);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        panel.add(iconLabel, BorderLayout.NORTH);

        JLabel titleLabel = new JLabel(won ? "你赢了！" : "你输了…", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 22));
        titleLabel.setForeground(new Color(0xf5e6c8));
        panel.add(titleLabel, BorderLayout.CENTER);

        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        bottomRow.setOpaque(false);

        if (won) {
            JLabel scoreLabel = new JLabel("得分: " + score, SwingConstants.CENTER);
            scoreLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
            scoreLabel.setForeground(new Color(0xe8c87a));
            bottomRow.add(scoreLabel);
        }

        JButton closeBtn = new JButton("确定");
        closeBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        closeBtn.setBackground(won ? new Color(0x4ecdc4) : new Color(0xff6b6b));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.addActionListener(e -> dispose());
        bottomRow.add(closeBtn);

        panel.add(bottomRow, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    public static void showWin(JFrame parent, int score) {
        MusicManager.playSfx("win");
        GameResultDialog dlg = new GameResultDialog(parent, true, score);
        dlg.setVisible(true);
    }

    public static void showLose(JFrame parent) {
        MusicManager.playSfx("lose");
        GameResultDialog dlg = new GameResultDialog(parent, false, 0);
        dlg.setVisible(true);
    }
}
