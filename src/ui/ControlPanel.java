package ui;
import utils.MusicManager;

import javax.swing.*;
import java.awt.*;

/**
 * 底部控制栏 — 六个操作按钮 + 四个道具按钮（带剩余次数标签）
 *
 *   第一行："> START" / "RESTART" / "SAVE" / "LOAD" / "SETTINGS" / "RANK"
 *   第二行：道具按钮（HINT/SHUFFLE/BOMB/FREEZE）+ 下方剩余次数标签
 */
public class ControlPanel extends JPanel {

    // ── 布局参数 ──
    int offSetX;
    int offSetY;
    int width;
    int height;

    // ── UI 组件 ──
    StatusPanel statusPanel;
    BoardPanel boardPanel;
    JButton startButton;
    JButton restartButton;
    JButton saveButton;
    JButton loadButton;
    JButton settingsButton;
    JButton leaderBoardBtn;
    JButton hintButton;
    JButton shuffleButton;
    JButton bombButton;
    JButton freezeTimeButton;

    // ── 道具次数标签 ──
    JLabel hintCountLabel;
    JLabel shuffleCountLabel;
    JLabel bombCountLabel;
    JLabel freezeTimeCountLabel;

    Runnable onRestart;
    Runnable onLeaderBoard;
    Runnable onSave;
    Runnable onLoad;
    Runnable onUseHint;
    Runnable onUseShuffle;
    Runnable onUseBomb;
    Runnable onUseFreezeTime;

    // ── 设置参数（从 SettingsDialog 读回） ──
    int currentTimeLimit = 120;
    int currentCoreSize = 4;
    String currentSkinDir = "resource";

    public String getSkinDir() { return currentSkinDir; }

    public int getCurrentCoreSize() { return currentCoreSize; }
    public int getCurrentTimeLimit() { return currentTimeLimit; }

    // ════════════════════════════════════════════════════
    // 回调注册
    // ════════════════════════════════════════════════════

    public void setOnRestart(Runnable callback) {
        this.onRestart = callback;
    }

    public void setOnLeaderBoard(Runnable callback) {
        this.onLeaderBoard = callback;
    }

    public void setOnSave(Runnable callback) {
        this.onSave = callback;
    }

    public void setOnLoad(Runnable callback) {
        this.onLoad = callback;
    }

    public void setOnUseHint(Runnable callback) {
        this.onUseHint = callback;
    }

    public void setOnUseShuffle(Runnable callback) {
        this.onUseShuffle = callback;
    }

    public void setOnUseBomb(Runnable callback) {
        this.onUseBomb = callback;
    }

    public void setOnUseFreezeTime(Runnable callback) {
        this.onUseFreezeTime = callback;
    }

    // ════════════════════════════════════════════════════
    // 道具计数更新（替代旧的 statusPanel.updateItemDisplay）
    // ════════════════════════════════════════════════════

    public void setItemCounts(int hints, int shuffles, int bombs, int freezes) {
        if (hintCountLabel != null) hintCountLabel.setText("剩余: " + hints);
        if (shuffleCountLabel != null) shuffleCountLabel.setText("剩余: " + shuffles);
        if (bombCountLabel != null) bombCountLabel.setText("剩余: " + bombs);
        if (freezeTimeCountLabel != null) freezeTimeCountLabel.setText("剩余: " + freezes);
    }

    // ════════════════════════════════════════════════════
    // 构造
    // ════════════════════════════════════════════════════

    public ControlPanel(StatusPanel statusPanel, BoardPanel boardPanel,
                        int offSetX, int offSetY, int width, int height) {
        setLayout(null);
        setBounds(offSetX, offSetY, width, height);
        setBackground(new Color(0x5c4a3a));
        setOpaque(true);

        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.width = width;
        this.height = height;
        this.statusPanel = statusPanel;
        this.boardPanel = boardPanel;

        int btnWidth = 110;
        int btnHeight = 40;
        int gap = 10;
        
        int totalW = btnWidth * 6 + gap * 5;
        int x = (width - totalW) / 2;
        int y = (height - btnHeight * 2 - gap - 20) / 2;  // 往上稍微调一下让两行都看得到

        // ── 第一行：功能按钮 ──
        startButton = createStyledButton("> START", new Color(0xe8c87a), new Color(0x4a3d2e));
        startButton.setBounds(x, y, btnWidth, btnHeight);
        add(startButton);
        startButton.addActionListener(e -> {
            statusPanel.startGame();
            boardPanel.startGame();
            boardPanel.refreshPairInfo();
        });

        restartButton = createStyledButton("RESTART", new Color(0x6b5b45), new Color(0xc4b091));
        restartButton.setBounds(x + btnWidth + gap, y, btnWidth, btnHeight);
        add(restartButton);
        restartButton.addActionListener(e -> {
            if (onRestart != null) onRestart.run();
        });

        saveButton = createStyledButton("SAVE", new Color(0x4CAF50), new Color(0xffffff));
        saveButton.setBounds(x + 2 * (btnWidth + gap), y, btnWidth, btnHeight);
        add(saveButton);
        saveButton.addActionListener(e -> {
            if (onSave != null) {
                onSave.run();
            }
        });

        loadButton = createStyledButton("LOAD", new Color(0x2196F3), new Color(0xffffff));
        loadButton.setBounds(x + 3 * (btnWidth + gap), y, btnWidth, btnHeight);
        add(loadButton);
        loadButton.addActionListener(e -> {
            if (onLoad != null) {
                onLoad.run();
            }
        });

        settingsButton = createStyledButton("SETTINGS", new Color(0x5c4a3a), new Color(0xa09070));
        settingsButton.setBounds(x + 4 * (btnWidth + gap), y, btnWidth, btnHeight);
        add(settingsButton);
        settingsButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            SettingsDialog dlg = new SettingsDialog(frame, currentTimeLimit, currentCoreSize > 4, 50);
            dlg.setOnSkinChange(dir -> currentSkinDir = dir);
            dlg.setVisible(true);
            if (dlg.isRestartRequested()) {
                currentTimeLimit = dlg.getSelectedTimeSeconds();
                currentCoreSize = dlg.getSelectedCoreSize();
                MusicManager.setSfxVolume(dlg.getSfxVolume() / 100f);
                currentSkinDir = dlg.getSelectedSkinDir();
                if (onRestart != null) onRestart.run();
            }
        });

        leaderBoardBtn = createStyledButton("RANK", new Color(0x4a3d2e), new Color(0xe8c87a));
        leaderBoardBtn.setBounds(x + 5 * (btnWidth + gap), y, btnWidth, btnHeight);
        add(leaderBoardBtn);
        leaderBoardBtn.addActionListener(e -> {
            if (onLeaderBoard != null) onLeaderBoard.run();
        });

        // ── 第二行：道具按钮 + 下方计数标签 ──
        int itemBtnWidth = 80;
        int itemBtnHeight = 32;
        int itemGap = 10;
        int itemTotalW = (itemBtnWidth + 20) * 4 + itemGap * 3;  // 每个算上 label 宽度
        int itemX = (width - itemTotalW) / 2;
        int itemY = y + btnHeight + gap + 2;

        // HINT
        JPanel hintPanel = createItemButtonWithLabel(
            hintButton = new RoundedButton("HINT", 0x4a3d2e),
            hintCountLabel = new JLabel("剩余: 3", SwingConstants.CENTER),
            new Color(0xd4a017)
        );
        hintPanel.setBounds(itemX, itemY, itemBtnWidth + 20, itemBtnHeight + 22);
        add(hintPanel);
        hintButton.addActionListener(e -> { if (onUseHint != null) onUseHint.run(); });

        // SHUFFLE
        JPanel shufflePanel = createItemButtonWithLabel(
            shuffleButton = new RoundedButton("SHUFFLE", 0x4a3d2e),
            shuffleCountLabel = new JLabel("剩余: 3", SwingConstants.CENTER),
            new Color(0x6b8e23)
        );
        shufflePanel.setBounds(itemX + (itemBtnWidth + 20) + itemGap, itemY, itemBtnWidth + 20, itemBtnHeight + 22);
        add(shufflePanel);
        shuffleButton.addActionListener(e -> { if (onUseShuffle != null) onUseShuffle.run(); });

        // BOMB
        JPanel bombPanel = createItemButtonWithLabel(
            bombButton = new RoundedButton("BOMB", 0x4a3d2e),
            bombCountLabel = new JLabel("剩余: 2", SwingConstants.CENTER),
            new Color(0xb22222)
        );
        bombPanel.setBounds(itemX + 2 * ((itemBtnWidth + 20) + itemGap), itemY, itemBtnWidth + 20, itemBtnHeight + 22);
        add(bombPanel);
        bombButton.addActionListener(e -> { if (onUseBomb != null) onUseBomb.run(); });

        // FREEZE
        JPanel freezePanel = createItemButtonWithLabel(
            freezeTimeButton = new RoundedButton("FREEZE", 0x4a3d2e),
            freezeTimeCountLabel = new JLabel("剩余: 2", SwingConstants.CENTER),
            new Color(0x4682b4)
        );
        freezePanel.setBounds(itemX + 3 * ((itemBtnWidth + 20) + itemGap), itemY, itemBtnWidth + 20, itemBtnHeight + 22);
        add(freezePanel);
        freezeTimeButton.addActionListener(e -> { if (onUseFreezeTime != null) onUseFreezeTime.run(); });
    }

    // ════════════════════════════════════════════════════
    // 辅助：创建道具按钮+标签组合
    // ════════════════════════════════════════════════════

    /**
     * 创建一个垂直布局的 JPanel，上方是按钮，下方是剩余次数标签
     */
    private JPanel createItemButtonWithLabel(JButton btn, JLabel label, Color labelColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setForeground(labelColor);

        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        label.setForeground(labelColor);
        label.setOpaque(false);

        panel.add(btn, BorderLayout.NORTH);
        panel.add(label, BorderLayout.SOUTH);
        return panel;
    }

    // ════════════════════════════════════════════════════
    // 工具
    // ════════════════════════════════════════════════════

    /** 创建统一样式的按钮（圆角、手型光标） */
    private JButton createStyledButton(String text, Color bg, Color fg) {
        RoundedButton btn = new RoundedButton(text, bg.getRGB() & 0xFFFFFF);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setForeground(fg);
        return btn;
    }
}
