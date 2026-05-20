package ui;

import utils.MusicManager;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * 游戏设置对话框（JScrollPane 增强版）
 */
public class SettingsDialog extends JDialog {

    private static final int[] TIME_OPTIONS = {60, 90, 120, 180, -1};

    private final JComboBox<String> timeLimitCombo;
    private final JComboBox<String> modeCombo;
    private final JSlider bgmSlider;
    private final JSlider sfxSlider;

    private boolean restartRequested = false;

    public SettingsDialog(JFrame parent, int currentTime, boolean currentHardMode, int currentVolume) {
        super(parent, "设置", true);
        setSize(460, 520);
        setLocationRelativeTo(parent);
        setResizable(false);
        setLayout(new BorderLayout());

        Font labelFont = new Font("Microsoft YaHei", Font.BOLD, 14);
        Font valueFont = new Font("Microsoft YaHei", Font.PLAIN, 14);
        Font descFont = new Font("Microsoft YaHei", Font.PLAIN, 12);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // 1. 时间限制
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        timePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "⏱ 时间限制", TitledBorder.LEFT, TitledBorder.TOP, labelFont));
        timeLimitCombo = new JComboBox<>(new String[]{"60 秒", "90 秒", "120 秒", "180 秒", "无限制"});
        timeLimitCombo.setFont(valueFont);
        int idx = 2;
        for (int i = 0; i < TIME_OPTIONS.length; i++) {
            if (TIME_OPTIONS[i] == currentTime) { idx = i; break; }
        }
        timeLimitCombo.setSelectedIndex(idx);
        timePanel.add(timeLimitCombo);
        JLabel timeHint = new JLabel("倒计时结束后游戏结束");
        timeHint.setFont(descFont);
        timeHint.setForeground(Color.GRAY);
        timePanel.add(timeHint);
        content.add(timePanel);

        // 2. 模式选择
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        modePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "🎯 模式", TitledBorder.LEFT, TitledBorder.TOP, labelFont));
        modeCombo = new JComboBox<>(new String[]{"简单模式（5种棋子）", "困难模式（12种棋子）"});
        modeCombo.setFont(valueFont);
        modeCombo.setSelectedIndex(currentHardMode ? 1 : 0);
        modePanel.add(modeCombo);
        content.add(modePanel);

        // 3. BGM 音量
        JPanel bgmPanel = new JPanel(new BorderLayout());
        bgmPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "🎵 背景音乐音量", TitledBorder.LEFT, TitledBorder.TOP, labelFont));
        bgmSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, currentVolume);
        bgmSlider.setMajorTickSpacing(25);
        bgmSlider.setPaintTicks(true);
        bgmSlider.setPaintLabels(true);
        bgmSlider.setFont(valueFont);
        bgmSlider.addChangeListener(e -> MusicManager.setVolume(bgmSlider.getValue() / 100f));
        bgmPanel.add(bgmSlider, BorderLayout.CENTER);
        content.add(bgmPanel);

        // 4. SFX 音量
        JPanel sfxPanel = new JPanel(new BorderLayout());
        sfxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "🔊 音效音量", TitledBorder.LEFT, TitledBorder.TOP, labelFont));
        sfxSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, (int)(MusicManager.getSfxVolume() * 100));
        sfxSlider.setMajorTickSpacing(25);
        sfxSlider.setPaintTicks(true);
        sfxSlider.setPaintLabels(true);
        sfxSlider.setFont(valueFont);
        sfxSlider.addChangeListener(e -> MusicManager.setSfxVolume(sfxSlider.getValue() / 100f));
        sfxPanel.add(sfxSlider, BorderLayout.CENTER);
        content.add(sfxPanel);

        // 5. 皮肤主题
        JPanel skinPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        skinPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "🎨 皮肤主题", TitledBorder.LEFT, TitledBorder.TOP, labelFont));
        JComboBox<String> skinCombo = new JComboBox<>(new String[]{"经典图标", "水果蔬菜"});
        skinCombo.setFont(valueFont);
        skinPanel.add(skinCombo);
        JLabel skinPreview = new JLabel("🍎🥕🍇🍄");
        skinPreview.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        skinPanel.add(skinPreview);
        content.add(skinPanel);

        // 6. 操作说明
        JPanel tipsPanel = new JPanel();
        tipsPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "📖 操作说明", TitledBorder.LEFT, TitledBorder.TOP, labelFont));
        tipsPanel.setLayout(new BoxLayout(tipsPanel, BoxLayout.Y_AXIS));
        String[] tips = {"• 点击 START 开始游戏", "• 选中两个相同图标即可消除", "• 路径最多允许 2 次转弯", "• HINT：提示一对可消除的棋子", "• SHUFFLE：打乱所有棋子位置", "• BOMB：点击一个棋子自动消除配对", "• FREEZE：冻结时间 10 秒"};
        for (String tip : tips) {
            JLabel tipLabel = new JLabel(tip);
            tipLabel.setFont(descFont);
            tipsPanel.add(tipLabel);
        }
        content.add(tipsPanel);

        // 7. 按钮区域
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
        JButton restartBtn = new JButton("🔄 应用并重新开始");
        restartBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        restartBtn.setBackground(new Color(70, 130, 180));
        restartBtn.setForeground(Color.WHITE);
        restartBtn.setFocusPainted(false);
        restartBtn.addActionListener(e -> { restartRequested = true; dispose(); });
        btnPanel.add(restartBtn);
        JButton closeBtn = new JButton("✕ 关闭");
        closeBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);
        content.add(btnPanel);

        // 8. 版本信息
        JLabel versionLabel = new JLabel("<html><center>🀄 连连看 v1.1<br>Built with Java Swing · 水果蔬菜皮肤</center></html>", SwingConstants.CENTER);
        versionLabel.setFont(descFont);
        versionLabel.setForeground(Color.DARK_GRAY);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        content.add(versionLabel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    public boolean isRestartRequested() { return restartRequested; }
    public int getSelectedTimeSeconds() {
        int idx = timeLimitCombo.getSelectedIndex();
        return idx >= 0 && idx < TIME_OPTIONS.length ? TIME_OPTIONS[idx] : 120;
    }
    public boolean isHardMode() { return modeCombo.getSelectedIndex() == 1; }
    public int getSelectedCoreSize() { return isHardMode() ? 8 : 4; }
    public int getMusicVolume() { return bgmSlider.getValue(); }
    public int getSfxVolume() { return sfxSlider.getValue(); }
}