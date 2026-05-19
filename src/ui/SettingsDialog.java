package ui;

import utils.MusicManager;

import javax.swing.*;
import java.awt.*;

/**
 * 游戏设置对话框 — 模态窗口
 *
 * 选项：
 *   - 时间限制：60s / 90s / 120s / 180s / 无限制
 *   - 模式：简单模式 / 困难模式
 *   - 音乐音量：0~100 滑条
 */
public class SettingsDialog extends JDialog {

    /** 时间选项与 ComboBox 索引的映射 */
    private static final int[] TIME_OPTIONS = {60, 90, 120, 180, -1};

    // ── UI 组件 ──
    private final JComboBox<String> timeLimitCombo;
    private final JComboBox<String> modeCombo;
    private final JSlider volumeSlider;

    /** 用户是否点击了「重新开始」按钮 */
    private boolean restartRequested = false;

    public SettingsDialog(JFrame parent, int currentTime, boolean currentHardMode, int currentVolume) {
        super(parent, "设置", true);
        setSize(420, 380);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.weightx = 1;

        Font labelFont = new Font("Microsoft YaHei", Font.BOLD, 15);
        Font valueFont = new Font("Microsoft YaHei", Font.PLAIN, 14);

        // ── 1. 时间限制 ──
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        JLabel timeLabel = new JLabel("⏱ 时间限制");
        timeLabel.setFont(labelFont);
        mainPanel.add(timeLabel, gbc);

        timeLimitCombo = new JComboBox<>(new String[]{
                "60 秒", "90 秒", "120 秒", "180 秒", "无限制"
        });
        timeLimitCombo.setFont(valueFont);

        // 匹配当前时间设置到下拉框索引
        int idx = 2; // 默认 120 秒
        for (int i = 0; i < TIME_OPTIONS.length; i++) {
            if (TIME_OPTIONS[i] == currentTime) {
                idx = i;
                break;
            }
        }
        timeLimitCombo.setSelectedIndex(idx);
        gbc.gridx = 1;
        mainPanel.add(timeLimitCombo, gbc);

        // ── 2. 模式选择（简单 / 困难） ──
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel modeLabel = new JLabel("🎯 模式");
        modeLabel.setFont(labelFont);
        mainPanel.add(modeLabel, gbc);

        modeCombo = new JComboBox<>(new String[]{"简单模式", "困难模式"});
        modeCombo.setFont(valueFont);
        modeCombo.setSelectedIndex(currentHardMode ? 1 : 0);
        gbc.gridx = 1;
        mainPanel.add(modeCombo, gbc);

        // ── 3. 音乐音量 ──
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel volLabel = new JLabel("🔊 音乐音量");
        volLabel.setFont(labelFont);
        mainPanel.add(volLabel, gbc);

        JPanel volPanel = new JPanel(new BorderLayout(8, 0));
        volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, currentVolume);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setFont(valueFont);
        volumeSlider.addChangeListener(e -> {
            MusicManager.setVolume(volumeSlider.getValue() / 100f);
        });
        volPanel.add(volumeSlider, BorderLayout.CENTER);
        gbc.gridx = 1;
        mainPanel.add(volPanel, gbc);

        // ── 4. 重新开始按钮 ──
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 8, 4, 8);
        JButton restartBtn = new JButton("🔄 应用并重新开始");
        restartBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        restartBtn.setBackground(new Color(70, 130, 180));
        restartBtn.setForeground(Color.WHITE);
        restartBtn.setFocusPainted(false);
        restartBtn.addActionListener(e -> {
            restartRequested = true;
            dispose();
        });
        mainPanel.add(restartBtn, gbc);

        // ── 5. 版本信息 ──
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(8, 8, 4, 8);
        JLabel versionLabel = new JLabel(
                "<html><center>🀄 连连看 v1.0<br>Built with Java Swing</center></html>",
                SwingConstants.CENTER
        );
        versionLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        versionLabel.setForeground(Color.DARK_GRAY);
        mainPanel.add(versionLabel, gbc);

        // ── 6. 关闭按钮 ──
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(4, 8, 0, 8);
        JButton closeBtn = new JButton("关闭");
        closeBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        closeBtn.addActionListener(e -> dispose());
        mainPanel.add(closeBtn, gbc);

        add(mainPanel, BorderLayout.CENTER);
    }

    // ── 查询接口 ──

    /** 用户是否点击了「重新开始」按钮 */
    public boolean isRestartRequested() {
        return restartRequested;
    }

    /** 读取时间限制秒数（-1 = 无限制） */
    public int getSelectedTimeSeconds() {
        int idx = timeLimitCombo.getSelectedIndex();
        if (idx < 0 || idx >= TIME_OPTIONS.length) idx = 2;
        return TIME_OPTIONS[idx];
    }

    /** 读取模式：true = 困难模式 */
    public boolean isHardMode() {
        return modeCombo.getSelectedIndex() == 1;
    }

    /** 读取音量（0~100） */
    public int getMusicVolume() {
        return volumeSlider.getValue();
    }
}
