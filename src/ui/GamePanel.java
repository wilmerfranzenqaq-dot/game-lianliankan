package ui;

import model.*;
import utils.SaveManager;

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
    private String currentMode;

    public GamePanel(boolean isHardMode, LeaderBoard leaderBoard, String username) {
        this.isHardMode = isHardMode;
        this.username = username;
        this.currentMode = isHardMode ? "困难模式" : "简单模式";

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
            Cell[][] newBoard = isHardMode ? gen2.generateHardBoard() : gen2.generateEasyBoard();
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

        // 胜利回调 → 记录成绩到排行榜
        boardPanel.setOnWinCallback(() -> {
            String mode = isHardMode ? "困难模式" : "简单模式";
            LeaderRecord record = new LeaderRecord(username, mode,
                    statusPanel.getScore(), statusPanel.getTimeUsed());
            leaderBoard.addRecord(record);
        });

        // 保存按钮回调
        controlPanel.setOnSave(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(controlPanel);
            SaveLoadDialog dialog = new SaveLoadDialog(frame, GamePanel.this, username, currentMode);
            dialog.setVisible(true);
        });

        // 加载按钮回调
        controlPanel.setOnLoad(() -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(controlPanel);
            SaveLoadDialog dialog = new SaveLoadDialog(frame, GamePanel.this, username, currentMode);
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
    }

    /**
     * 保存当前游戏状态到指定槽位
     */
    public boolean saveGame(int slot) {
        String filePath = SaveManager.getSaveFilePath(username, currentMode, slot);
        
        return SaveManager.saveGame(
            filePath,
            username,
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
    public boolean loadGame(int slot) {
        String filePath = SaveManager.getSaveFilePath(username, currentMode, slot);
        SaveManager.SaveData data = SaveManager.loadGame(filePath);
        
        if (data == null) {
            return false;
        }
        
        boardPanel.restoreFromSave(data.gameBoard);
        
        statusPanel.setScore(data.score);
        statusPanel.setRemainingSeconds(data.remainingSeconds);
        statusPanel.setElapsedSeconds(data.elapsedSeconds);
        statusPanel.setComboState(data.comboCount, data.lastEliminationTime);
        
        boardPanel.refreshPairInfo();
        
        boardPanel.setStarted(true);
        statusPanel.startTimer();
        
        return true;
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

    /**
     * 获取用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 获取当前模式
     */
    public String getCurrentMode() {
        return currentMode;
    }
}
