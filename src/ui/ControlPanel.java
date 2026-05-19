package ui;

import javax.swing.*;
import java.awt.*;

/**
 * 底部控制栏 — 六个操作按钮
 *
 *   "> START"  — 启动游戏（开启计时器 + 激活棋盘交互）
 *   "RESTART"  — 重新生成棋盘并重置状态
 *   "SAVE"     — 保存当前游戏
 *   "LOAD"     — 加载存档
 *   "SETTINGS" — 打开设置对话框（可改时间和难度后重启）
 *   "RANK"     — 打开排行榜弹窗
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
        int y = (height - btnHeight * 2 - gap) / 2;

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
            SettingsDialog dlg = new SettingsDialog(frame, currentTimeLimit, currentCoreSize);
            dlg.setVisible(true);
            if (dlg.isRestartRequested()) {
                currentTimeLimit = dlg.getSelectedTimeSeconds();
                currentCoreSize = dlg.getSelectedCoreSize();
                if (onRestart != null) onRestart.run();
            }
        });

        leaderBoardBtn = createStyledButton("RANK", new Color(0x4a3d2e), new Color(0xe8c87a));
        leaderBoardBtn.setBounds(x + 5 * (btnWidth + gap), y, btnWidth, btnHeight);
        add(leaderBoardBtn);
        leaderBoardBtn.addActionListener(e -> {
            if (onLeaderBoard != null) onLeaderBoard.run();
        });

        int itemBtnWidth = 80;
        int itemBtnHeight = 35;
        int itemGap = 8;
        int itemTotalW = itemBtnWidth * 4 + itemGap * 3;
        int itemX = (width - itemTotalW) / 2;
        int itemY = y + btnHeight + gap;

        hintButton = createStyledButton("HINT", new Color(0xffeb3b), new Color(0x000000));
        hintButton.setBounds(itemX, itemY, itemBtnWidth, itemBtnHeight);
        add(hintButton);
        hintButton.addActionListener(e -> {
            if (onUseHint != null) onUseHint.run();
        });

        shuffleButton = createStyledButton("SHUFFLE", new Color(0x4caf50), new Color(0xffffff));
        shuffleButton.setBounds(itemX + itemBtnWidth + itemGap, itemY, itemBtnWidth, itemBtnHeight);
        add(shuffleButton);
        shuffleButton.addActionListener(e -> {
            if (onUseShuffle != null) onUseShuffle.run();
        });

        bombButton = createStyledButton("BOMB", new Color(0xf44336), new Color(0xffffff));
        bombButton.setBounds(itemX + 2 * (itemBtnWidth + itemGap), itemY, itemBtnWidth, itemBtnHeight);
        add(bombButton);
        bombButton.addActionListener(e -> {
            if (onUseBomb != null) onUseBomb.run();
        });

        freezeTimeButton = createStyledButton("FREEZE", new Color(0x2196f3), new Color(0xffffff));
        freezeTimeButton.setBounds(itemX + 3 * (itemBtnWidth + itemGap), itemY, itemBtnWidth, itemBtnHeight);
        add(freezeTimeButton);
        freezeTimeButton.addActionListener(e -> {
            if (onUseFreezeTime != null) onUseFreezeTime.run();
        });
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
