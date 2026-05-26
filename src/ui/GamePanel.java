package ui;

import model.*;
import utils.SaveManager;

import utils.MusicManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 游戏主面板 — 组合所有游戏子面板并连接回调
 *
 * 布局（1000×1000）：
 *   ├── StatusPanel   (0, 0,    800, 100)   — 顶部状态栏
 *   ├── BoardPanel    (0, 100,  800, 800)   — 棋盘区域
 *   ├── ControlPanel  (0, 900,  800, 100)   — 底部控制按钮
 *   └── CatPanel      (800, 0,  200, 1000)  — 右侧猫面板
 *
 * 回调链：
 *   ControlPanel.onRestart  → 重新生成棋盘 + 重置状态
 *   BoardPanel.onFishFeed   → CatPanel.feedFish()
 *   BoardPanel.onWinCallback → 写入 LeaderBoard
 *   ControlPanel.onLeaderBoard → 弹出排行榜窗口
 */
public class GamePanel extends JPanel {

    private BoardPanel boardPanel;
    private StatusPanel statusPanel;
    private ControlPanel controlPanel;
    private CatPanel catPanel;
    private boolean isHardMode;
    private String username;
    private String catName;
    private String currentMode;
    private boolean isGuestMode;

    public GamePanel(boolean isHardMode, LeaderBoard leaderBoard, String username, String catName) {
        this.isHardMode = isHardMode;
        this.username = username;
        this.catName = catName;
        this.currentMode = isHardMode ? "困难模式" : "简单模式";
        this.isGuestMode = (username == null);

        setLayout(null);
        setBackground(new Color(0x6b5b45));
        setPreferredSize(new Dimension(1000, 1000));
        setBounds(0, 0, 1000, 1000);

        ChessGenerator gen = new ChessGenerator();
        Cell[][] board = isHardMode ? gen.generateHardBoard() : gen.generateEasyBoard();
        int totalRow = board.length;
        int totalCol = board[0].length;

        statusPanel = new StatusPanel(0, 0, 800, 100);
        boardPanel = new BoardPanel(new GameBoard(totalRow, totalCol, board), statusPanel,
                0, 100, 800, 750);
        controlPanel = new ControlPanel(statusPanel, boardPanel, 0, 850, 800, 150);

        // ── 将 ControlPanel 引用注入 BoardPanel（供 setItemCounts 调用） ──
        boardPanel.setControlPanel(controlPanel);

        catPanel = new CatPanel();
        catPanel.setBounds(800, 0, 200, 1000);

        add(statusPanel);
        add(boardPanel);
        add(controlPanel);
        add(catPanel);

        // ── 回调连接 ──

        // 重新开始：重新生成棋盘 + 重置状态
        controlPanel.setOnRestart(() -> {
            ChessGenerator gen2 = new ChessGenerator();
            Cell[][] newBoard = GamePanel.this.isHardMode ? gen2.generateHardBoard() : gen2.generateEasyBoard();
            int newRow = newBoard.length;
            int newCol = newBoard[0].length;
            boardPanel.setGameBoard(new GameBoard(newRow, newCol, newBoard));
            boardPanel.setSkinDir(controlPanel.getSkinDir());
            statusPanel.resetGame();
            boardPanel.refreshPairInfo();
        });

        // 消除棋子 → 喂猫
        boardPanel.setOnFishFeed(() -> catPanel.feedFish());

        // 排行榜按钮
        controlPanel.setOnLeaderBoard(() -> {
            LeaderBoardPanel panel = new LeaderBoardPanel(null, leaderBoard);
            panel.setVisible(true);
        });

        // 胜利回调 → 记录成绩到排行榜（游客模式不记录）
        boardPanel.setOnWinCallback(() -> {
            if (!isGuestMode) {
                String mode = GamePanel.this.isHardMode ? "困难模式" : "简单模式";
                LeaderRecord record = new LeaderRecord(catName, username, mode,
                        statusPanel.getScore(), statusPanel.getTimeUsed());
                leaderBoard.addRecord(record);
            }
        });

        // 保存按钮回调
        controlPanel.setOnSave(() -> {
            if (isGuestMode) {
                JOptionPane.showMessageDialog(this, "游客模式不支持存档功能！");
                return;
            }
            if (statusPanel.isGameOver()) {                    
            JOptionPane.showMessageDialog(this, "游戏已结束，无法保存！");
                return;
            }
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(controlPanel);
            SaveLoadDialog dialog = new SaveLoadDialog(frame, GamePanel.this, username, currentMode);
            dialog.setVisible(true);
        });

        // 加载按钮回调
        controlPanel.setOnLoad(() -> {
            if (isGuestMode) {
                JOptionPane.showMessageDialog(this, "游客模式不支持读档功能！");
                return;
            }
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(controlPanel);
            SaveLoadDialog dialog = new SaveLoadDialog(frame, GamePanel.this, username, currentMode, false);
            dialog.setVisible(true);
        });

        controlPanel.setOnUseHint(() -> {
            boardPanel.useHint();
        });

        controlPanel.setOnUseShuffle(() -> {
            boardPanel.useShuffle();
        });

        controlPanel.setOnUseBomb(() -> {
            boardPanel.useBomb();
        });

        controlPanel.setOnUseFreezeTime(() -> {
            boardPanel.useFreezeTime();
        });

        // ── 监听 SettingsDialog：打开时暂停计时器，关闭时恢复 ──
        controlPanel.settingsButton.addActionListener(e -> {
            // 暂停计时器（只暂停 StatusPanel 的倒计时，不改变 gameStarted 状态）
            if (boardPanel.isStarted()) {
                statusPanel.pauseTimer();
            }

            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            SettingsDialog dlg = new SettingsDialog(frame, controlPanel.getCurrentCoreSize() > 4, 50);
            dlg.setOnSkinChange(dir -> controlPanel.currentSkinDir = dir);
            dlg.setVisible(true);
            if (dlg.isRestartRequested()) {
                GamePanel.this.isHardMode = dlg.isHardMode();  // ← 关键：更新模式
                controlPanel.currentCoreSize = dlg.getSelectedCoreSize();
                MusicManager.setSfxVolume(dlg.getSfxVolume() / 100f);
                controlPanel.currentSkinDir = dlg.getSelectedSkinDir();
                // 重置后会恢复计时器（statusPanel.resetGame 会停止它）
                if (controlPanel.onRestart != null) controlPanel.onRestart.run();
            } else {
                // 没有重置，恢复计时器
                if (boardPanel.isStarted()) {
                    statusPanel.resumeTimer();
                }
            }
        });

        // 初始刷新配对信息
        boardPanel.refreshPairInfo();

        // ── 键盘快捷键 ──
        setupKeyboardShortcuts();
    }

    /**
     * 注册全局键盘快捷键
     * Space=START, R=restart, S=save, L=load
     */
    private void setupKeyboardShortcuts() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke("SPACE"), "gameStart");
        im.put(KeyStroke.getKeyStroke("typed r"), "gameRestart");
        im.put(KeyStroke.getKeyStroke("typed R"), "gameRestart");
        im.put(KeyStroke.getKeyStroke("typed s"), "gameSave");
        im.put(KeyStroke.getKeyStroke("typed S"), "gameSave");
        im.put(KeyStroke.getKeyStroke("typed l"), "gameLoad");
        im.put(KeyStroke.getKeyStroke("typed L"), "gameLoad");

        am.put("gameStart", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (controlPanel.startButton != null && controlPanel.startButton.isEnabled()) {
                    controlPanel.startButton.doClick();
                }
            }
        });

        am.put("gameRestart", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (controlPanel.restartButton != null && controlPanel.restartButton.isEnabled()) {
                    controlPanel.restartButton.doClick();
                }
            }
        });

        am.put("gameSave", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (controlPanel.saveButton != null && controlPanel.saveButton.isEnabled()) {
                    controlPanel.saveButton.doClick();
                }
            }
        });

        am.put("gameLoad", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (controlPanel.loadButton != null && controlPanel.loadButton.isEnabled()) {
                    controlPanel.loadButton.doClick();
                }
            }
        });
    }

    // ── 供 GameFrame 菜单栏调用的公开暴露按钮 ──
    public JButton getStartButton() { return controlPanel.startButton; }
    public JButton getRestartButton() { return controlPanel.restartButton; }
    public JButton getSaveButton() { return controlPanel.saveButton; }
    public JButton getLoadButton() { return controlPanel.loadButton; }

    /**
     * 保存当前游戏状态到指定槽位
     */
    public boolean saveGame(int slot) {
        String filePath = SaveManager.getSaveFilePath(username, currentMode, slot);

        return SaveManager.saveGame(
            filePath,
            username,
            catName,
            currentMode,
            slot,
            statusPanel.getScore(),
            statusPanel.getRemainingSeconds(),
            statusPanel.getElapsedSeconds(),
            statusPanel.getComboCount(),
            statusPanel.getLastEliminationTime(),
            boardPanel.getGameBoard()
        );
    }

    /**
     * 检查指定槽位是否有存档
     */
    public boolean hasSave(int slot) {
        return SaveManager.hasSave(username, currentMode, slot);
    }

    /**
     * 加载指定槽位的存档并恢复游戏状态
     */
    /**
     * 加载存档，返回错误信息（null 表示成功）
     */
    public String loadGame(int slot) {
        String filePath = SaveManager.getSaveFilePath(username, currentMode, slot);
        SaveManager.SaveData data = SaveManager.loadGame(filePath);

        if (data == null) {
            return "存档文件不存在或已损坏";
        }

        if (!username.equals(data.username)) {
            return "该存档不属于当前用户，无法读取！";
        }
        // 读取存档中的猫名字
        if (data.catName != null && !data.catName.trim().isEmpty()) {
            this.catName = data.catName;
        }

        boardPanel.restoreFromSave(data.gameBoard);

        statusPanel.setScore(data.score);
        statusPanel.setRemainingSeconds(data.remainingSeconds);
        statusPanel.setElapsedSeconds(data.elapsedSeconds);
        statusPanel.setComboState(data.comboCount, data.lastEliminationTime);

        boardPanel.refreshPairInfo();

        boardPanel.setStarted(true);
        statusPanel.startTimer();

        return null;
    }

    /**
     * 获取所有可用的存档槽位列表
     */
    public List<Integer> getAvailableSaves() {
        return SaveManager.getAvailableSlots(username, currentMode);
    }

    /**
     * 检查棋盘是否已开始（供对话框调用）
     */
    public boolean boardPanelIsStarted() {
        return boardPanel.isStarted();
    }

    public void stopEffects() {
        boardPanel.stopEffects();
    }

    /**
     * 获取用户名
     */
    public String getUsername() {
        return username;
    }

    public boolean isGuestMode() {
        return isGuestMode;
    }

    /**
     * 获取小猫名字（排行榜/存档显示用）
     */
    public String getCatName() {
        return catName;
    }

    /**
     * 获取当前模式
     */
    public String getCurrentMode() {
        return currentMode;
    }
}
