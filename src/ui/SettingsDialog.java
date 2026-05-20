package ui;

import utils.MusicManager;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/**
 * 游戏设置对话框 — 深木底金色主题，圆角面板，统一 token
 */
public class SettingsDialog extends JDialog {

    // ── Design Tokens ──
    private static final Color CANVAS     = new Color(0x2a221a);
    private static final Color SURFACE    = new Color(0x3a3023);
    private static final Color SURFACE_2  = new Color(0x4a3d2e);
    private static final Color PRIMARY    = new Color(0xe8c87a);
    private static final Color TEXT       = new Color(0xd4c5a9);
    private static final Color TEXT_MUTED = new Color(0x9a8b78);
    private static final Color BTN_BG     = new Color(0x4a3d2e);
    private static final Color BTN_ACTIVE = new Color(0xe8c87a);
    private static final Color BORDER     = new Color(0x5c4a3a);

    // ── Typography ──
    private static final Font FONT_BODY  = new Font("Microsoft YaHei", Font.PLAIN, 13);
    private static final Font FONT_BOLD  = new Font("Microsoft YaHei", Font.BOLD, 14);
    private static final Font FONT_SMALL = new Font("Microsoft YaHei", Font.PLAIN, 12);
    private static final Font FONT_BTN   = new Font("Microsoft YaHei", Font.BOLD, 14);

    // ── Rounded ──
    private static final int RADIUS_PANEL  = 8;
    private static final int RADIUS_BUTTON = 8;
    private static final int RADIUS_INPUT  = 6;

    // ── Spacing ──
    private static final int PAD_CARD   = 20;
    private static final int GAP_SECTION = 12;

    private static final int[] TIME_OPTIONS = {60, 90, 120, 180, -1};

    private final JComboBox<String> timeLimitCombo;
    private final JComboBox<String> modeCombo;
    private final JSlider bgmSlider;
    private final JSlider sfxSlider;

    private boolean restartRequested = false;
    private String skinDir = "resource";
    private Consumer<String> onSkinChange;

    // ── Helper: 圆角面板 ──

    private static JPanel roundedPanel(String title, Component inner) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SURFACE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), RADIUS_PANEL, RADIUS_PANEL));
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, PAD_CARD, 14, PAD_CARD));

        // 标题
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_BOLD);
        titleLabel.setForeground(PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(inner, BorderLayout.CENTER);
        return panel;
    }

    // ── Helper: 圆角按钮 ──

    private static JButton styledButton(String text, Color bg, Color fg) {
        RoundedButton btn = new RoundedButton(text, bg.getRGB() & 0xFFFFFF);
        btn.setFont(FONT_BTN);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        Dimension d = btn.getPreferredSize();
        btn.setPreferredSize(new Dimension(Math.max(d.width, 140), 36));
        return btn;
    }

    // ── Helper: 圆角组合框 ──

    private static JComboBox<String> styledCombo(String[] items, int selected) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(SURFACE_2);
        cb.setForeground(TEXT);
        cb.setSelectedIndex(selected);
        // 自定义下拉项颜色
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSel, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSel, cellHasFocus);
                setBackground(isSel ? SURFACE_2 : SURFACE);
                setForeground(TEXT);
                setFont(FONT_BODY);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        return cb;
    }

    // ── 滑条 UI ──

    private static JSlider styledSlider(int value) {
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 100, value) {
            @Override public void updateUI() {
                setUI(new BasicSliderUI(this) {
                    @Override public void paintTrack(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        int w = trackRect.width;
                        int h = 6;
                        int y = trackRect.y + (trackRect.height - h) / 2;
                        g2.setColor(new Color(0x5c4a3a));
                        g2.fill(new RoundRectangle2D.Float(trackRect.x, y, w, h, 3, 3));
                        // 已走过部分
                        float pct = slider.getValue() / 100f;
                        int fillW = (int) (w * pct);
                        g2.setColor(PRIMARY);
                        g2.fill(new RoundRectangle2D.Float(trackRect.x, y, fillW, h, 3, 3));
                        g2.dispose();
                    }
                    @Override public void paintThumb(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(PRIMARY);
                        int r = 10;
                        int x = thumbRect.x + (thumbRect.width - r * 2) / 2;
                        int y = thumbRect.y + (thumbRect.height - r * 2) / 2;
                        g2.fillOval(x, y, r * 2, r * 2);
                        g2.dispose();
                    }
                });
            }
        };
        slider.setOpaque(false);
        slider.setMajorTickSpacing(50);
        slider.setPaintLabels(true);
        slider.setPaintTicks(false);
        slider.setFont(FONT_SMALL);
        return slider;
    }

    // ══════════════════════ 构造 ══════════════════════

    public SettingsDialog(JFrame parent, int currentTime, boolean currentHardMode, int currentVolume) {
        super(parent, "设置", true);
        setSize(480, 580);
        setLocationRelativeTo(parent);
        setResizable(false);
        getContentPane().setBackground(CANVAS);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CANVAS);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // 1. 时间限制
        timeLimitCombo = styledCombo(
            new String[]{"60 秒", "90 秒", "120 秒", "180 秒", "无限制"},
            2
        );
        for (int i = 0; i < TIME_OPTIONS.length; i++) {
            if (TIME_OPTIONS[i] == currentTime) { timeLimitCombo.setSelectedIndex(i); break; }
        }
        content.add(roundedPanel("时间限制", timeLimitCombo));
        content.add(Box.createVerticalStrut(GAP_SECTION));

        // 2. 模式选择
        modeCombo = styledCombo(new String[]{"简单模式（4 种棋子）", "困难模式（8 种棋子）"}, currentHardMode ? 1 : 0);
        content.add(roundedPanel("模式选择", modeCombo));
        content.add(Box.createVerticalStrut(GAP_SECTION));

        // 3. BGM 音量
        bgmSlider = styledSlider(currentVolume);
        bgmSlider.addChangeListener(e -> MusicManager.setVolume(bgmSlider.getValue() / 100f));
        content.add(roundedPanel("背景音乐音量", bgmSlider));
        content.add(Box.createVerticalStrut(GAP_SECTION));

        // 4. SFX 音量
        sfxSlider = styledSlider((int)(MusicManager.getSfxVolume() * 100));
        sfxSlider.addChangeListener(e -> MusicManager.setSfxVolume(sfxSlider.getValue() / 100f));
        content.add(roundedPanel("音效音量", sfxSlider));
        content.add(Box.createVerticalStrut(GAP_SECTION));

        // 5. 皮肤主题
        JComboBox<String> skinCombo = styledCombo(new String[]{"经典图标", "水果蔬菜"}, 0);
        JLabel skinPreview = new JLabel("  🍇🍊🍋🍉");
        skinPreview.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        skinPreview.setForeground(TEXT_MUTED);
        JPanel skinInner = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        skinInner.setOpaque(false);
        skinInner.add(skinCombo);
        skinInner.add(skinPreview);
        content.add(roundedPanel("皮肤主题", skinInner));
        content.add(Box.createVerticalStrut(GAP_SECTION));
        skinCombo.addActionListener(e -> {
            skinDir = skinCombo.getSelectedIndex() == 0 ? "resource" : "resource/fruit";
            if (onSkinChange != null) onSkinChange.accept(skinDir);
        });

        // 6. 操作说明
        String[] tips = {
            "点击 START 开始游戏",
            "选中两个相同图标即可消除",
            "路径最多允许 2 次转弯",
            "HINT：提示一对可消除的棋子",
            "SHUFFLE：打乱所有棋子位置",
            "BOMB：点击一个棋子自动消除配对",
            "FREEZE：冻结倒计时 10 秒",
        };
        JPanel tipsInner = new JPanel();
        tipsInner.setLayout(new BoxLayout(tipsInner, BoxLayout.Y_AXIS));
        tipsInner.setOpaque(false);
        for (String tip : tips) {
            JLabel lbl = new JLabel("• " + tip);
            lbl.setFont(FONT_SMALL);
            lbl.setForeground(TEXT_MUTED);
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
            tipsInner.add(lbl);
        }
        content.add(roundedPanel("操作说明", tipsInner));
        content.add(Box.createVerticalStrut(GAP_SECTION));

        // 7. 按钮区域
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnPanel.setOpaque(false);

        JButton restartBtn = styledButton("应用并重新开始", new Color(0x7a6a50), PRIMARY);
        restartBtn.setBackground(new Color(0x3a3023));
        restartBtn.addActionListener(e -> { restartRequested = true; dispose(); });
        btnPanel.add(restartBtn);

        JButton closeBtn = styledButton("  关闭  ", BTN_BG, TEXT_MUTED);
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.add(btnPanel, BorderLayout.CENTER);
        btnWrapper.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        content.add(btnWrapper);

        // 8. 版本信息
        JLabel versionLabel = new JLabel("连连看 v1.1 · Built with Java Swing", SwingConstants.CENTER);
        versionLabel.setFont(FONT_SMALL);
        versionLabel.setForeground(TEXT_MUTED);
        versionLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        content.add(versionLabel);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(CANVAS);
        add(scrollPane, BorderLayout.CENTER);
    }

    // ── 公共方法 ──

    public void setOnSkinChange(Consumer<String> callback) { this.onSkinChange = callback; }
    public boolean isRestartRequested() { return restartRequested; }
    public int getSelectedTimeSeconds() {
        int idx = timeLimitCombo.getSelectedIndex();
        return idx >= 0 && idx < TIME_OPTIONS.length ? TIME_OPTIONS[idx] : 120;
    }
    public boolean isHardMode() { return modeCombo.getSelectedIndex() == 1; }
    public int getSelectedCoreSize() { return isHardMode() ? 8 : 4; }
    public int getMusicVolume() { return bgmSlider.getValue(); }
    public int getSfxVolume() { return sfxSlider.getValue(); }
    public String getSelectedSkinDir() { return skinDir; }
}
