package ui;

import effects.EffectManager;
import model.*;
import model.Rectangle;
import utils.MusicManager;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 棋盘渲染与交互面板 — 游戏的核心显示区域
 *
 * 职责：
 *   - 从 resource/ 目录加载棋子图片
 *   - 绘制棋盘网格（含选中高亮、格子边框）
 *   - 处理鼠标点击选择与消除判定
 *   - 消除成功后绘制连接线动画（200ms）
 *   - 胜利检测与回调触发
 */
public class BoardPanel extends JPanel {

    // ── 光标模式枚举 ──
    public enum CursorMode {
        NORMAL,
        HINT,
        BOMB,
        SHUFFLE,
        FREEZE
    }

    // ── 布局参数 ──
    int offSetX;
    int offSetY;
    int width;
    int height;
    int cellWidth;
    int cellHeight;

    // ── 棋盘数据 ──
    GameBoard gameBoard;
    int totalRow;
    int totalCol;

    // ── 图片资源 ──
    List<Image> imageList = new ArrayList<>();
    private String skinDir = "resource";
    Image[] scaledImages;

    // ── 游戏状态 ──
    StatusPanel statusPanel;
    ControlPanel controlPanel;          // 引用 ControlPanel 以便更新道具计数
    boolean started;                    // 是否已开始（START 按钮控制）
    boolean animating;                  // 是否正在播放消除动画
    Position firstSelected = null;      // 第一次点击选中的位置
    Position secondSelected = null;     // 第二次点击选中的位置
    ItemManager itemManager;
    Position[] hintPositions = null;
    long hintShowTime = 0;
    CursorMode currentCursorMode = CursorMode.NORMAL;

    // ── 连接线绘制 ──
    List<Line> lineList = new ArrayList<>();
    boolean lineVisible;

    // ── 回调 ──
    Runnable onWinCallback;
    Runnable onFishFeed;

    // ── 特效系统 ──
    private EffectManager effectManager;

    // ── COMBO 浮动文字 ──
    private List<ComboText> comboTexts = new ArrayList<>();

    // ════════════════════════════════════════════════════
    // 构造与初始化
    // ════════════════════════════════════════════════════

    public BoardPanel(GameBoard gameBoard, StatusPanel statusPanel,
                      int offSetX, int offSetY, int width, int height) {
        this.statusPanel = statusPanel;
        this.offSetX = offSetX;
        this.offSetY = offSetY;

        setBounds(offSetX, offSetY, width, height);
        setBackground(new Color(0x6b5b45));
        setOpaque(true);

        this.totalRow = gameBoard.getRowCnt();
        this.totalCol = gameBoard.getColCnt();
        this.width = width;
        this.height = height;
        this.gameBoard = gameBoard;
        this.itemManager = new ItemManager(gameBoard);

        setPreferredSize(new Dimension(this.width, this.height));
        this.cellWidth = this.width / totalCol;
        this.cellHeight = this.height / totalRow;

        // ── 加载棋子图片资源 ──
        loadImages();

        // 预缩放到格子大小        // 预缩放到格子大小（同步缩放，消除每帧缩放开销 + 懒加载空白 bug）
        scaledImages = new Image[imageList.size()];
        for (int i = 0; i < imageList.size(); i++) {
            BufferedImage bi = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.drawImage(imageList.get(i), 0, 0, cellWidth, cellHeight, null);
            g2d.dispose();
            scaledImages[i] = bi;
        }

        // ── 鼠标点击监听 ──
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentCursorMode == CursorMode.BOMB) {
                    handleBombClick(e.getX(), e.getY());
                } else {
                    handleClick(e.getX(), e.getY());
                }
            }
        });

        // ── 初始化特效管理器并启动动画定时器 ──
        effectManager = new EffectManager();
        Timer effectTimer = new Timer(16, e -> {
            effectManager.update();

            // 更新 COMBO 浮动文字
            Iterator<ComboText> iter = comboTexts.iterator();
            while (iter.hasNext()) {
                ComboText ct = iter.next();
                ct.update();
                if (ct.life <= 0) iter.remove();
            }

            if (effectManager.hasActiveEffects() || !comboTexts.isEmpty()) {
                repaint();
            }
        });
        effectTimer.start();
    }

    // ════════════════════════════════════════════════════
    // ControlPanel 引用设置
    // ════════════════════════════════════════════════════

    public void setControlPanel(ControlPanel controlPanel) {
        this.controlPanel = controlPanel;
    }

    // ════════════════════════════════════════════════════
    // 回调注册
    // ════════════════════════════════════════════════════

    public void setOnWinCallback(Runnable callback) {
        this.onWinCallback = callback;
    }

    public void setOnFishFeed(Runnable callback) {
        this.onFishFeed = callback;
    }

    // ════════════════════════════════════════════════════
    // 游戏控制
    // ════════════════════════════════════════════════════

    /**
     * 激活棋盘交互（START 按钮调用）
     */
    public void startGame() {
        started = true;
    }

    /** 替换棋盘（RESTART / 设置变更时调用） */
    public void setGameBoard(GameBoard newBoard) {
        this.gameBoard = newBoard;
        this.totalRow = newBoard.getRowCnt();
        this.totalCol = newBoard.getColCnt();
        this.started = false;
        this.firstSelected = null;
        this.secondSelected = null;
        this.lineList.clear();
        effectManager.clearAll();
        currentCursorMode = CursorMode.NORMAL;
        setCursor(Cursor.getDefaultCursor());
        
        // 重置道具管理器（恢复初始数量）
        this.itemManager = new ItemManager(gameBoard);
        updateItemDisplay();
        
        repaint();
    }


    /**
     * 刷新 StatusPanel 上的配对进度信息，同时更新道具计数
     */
    public void refreshPairInfo() {
        int totalPairs = gameBoard.getTotalPairs();
        int clearedPairs = gameBoard.getClearedPairs();
        int remainingPairs = gameBoard.getRemainingPairs();
        statusPanel.updatePairInfo(remainingPairs, clearedPairs, totalPairs);
        updateItemDisplay();
    }

    /**
     * 通过 ControlPanel 的 setItemCounts 更新道具计数显示
     */
    // ── COMBO 浮动文字内部类 ──
    static class ComboText {
        int x, y;
        String text;
        float life = 1.0f; // 1.0 → 0.0
        int vy = -2;       // 上浮

        ComboText(int x, int y, String text) {
            this.x = x;
            this.y = y;
            this.text = text;
        }

        void update() {
            y += vy;
            life -= 0.02f;
        }

        void draw(Graphics2D g) {
            if (life <= 0) return;
            g.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
            g.setColor(new Color(
                    ThemeColors.FX_COMBO.getRed(),
                    ThemeColors.FX_COMBO.getGreen(),
                    ThemeColors.FX_COMBO.getBlue(),
                    (int)(life * 255)
            ));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(text);
            g.drawString(text, x - tw / 2, y);
        }
    }

    private void updateItemDisplay() {
        if (controlPanel != null) {
            controlPanel.setItemCounts(
                    itemManager.getHintCount(),
                    itemManager.getShuffleCount(),
                    itemManager.getBombCount(),
                    itemManager.getFreezeTimeCount()
            );
        }
    }

    public void useHint() {
        if (!started || animating) return;

        currentCursorMode = CursorMode.HINT;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Position[] result = itemManager.useHint();
        if (result != null) {
            hintPositions = result;
            hintShowTime = System.currentTimeMillis();
            repaint();

            Timer timer = new Timer(1500, e -> {
                hintPositions = null;
                currentCursorMode = CursorMode.NORMAL;
                setCursor(Cursor.getDefaultCursor());
                repaint();
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            JOptionPane.showMessageDialog(this, "没有可消除的配对！");
            currentCursorMode = CursorMode.NORMAL;
            setCursor(Cursor.getDefaultCursor());
        }
        updateItemDisplay();
    }

    public void useShuffle() {
        if (!started || animating) return;

        currentCursorMode = CursorMode.SHUFFLE;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (itemManager.useShuffle()) {
            gameBoard.clearAllChosen();
            firstSelected = null;
            secondSelected = null;
            repaint();
        } else {
            JOptionPane.showMessageDialog(this, "没有重排道具了！");
        }

        currentCursorMode = CursorMode.NORMAL;
        setCursor(Cursor.getDefaultCursor());
        updateItemDisplay();
    }

    public void useBomb() {
        if (!started || animating) return;

        if (itemManager.getBombCount() <= 0) {
            JOptionPane.showMessageDialog(this, "没有炸弹道具了！");
            return;
        }

        currentCursorMode = CursorMode.BOMB;
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        repaint();
    }

    private void handleBombClick(int x, int y) {
        Position pos = getPositionByPoint(x, y);
        if (pos == null) {
            // 点击空白区域退出 bombMode
            currentCursorMode = CursorMode.NORMAL;
            setCursor(Cursor.getDefaultCursor());
            repaint();
            return;
        }

        Cell cell = gameBoard.getCell(pos.getRow(), pos.getCol());
        if (cell.isEmpty()) {
            currentCursorMode = CursorMode.NORMAL;
            setCursor(Cursor.getDefaultCursor());
            repaint();
            return;
        }

        Position[] result = itemManager.useBomb(pos);
        if (result != null) {
            Position pos1 = result[0];
            Position pos2 = result[1];

            effectManager.createShatterEffect(pos1, pos2, cellWidth, cellHeight,
                    gameBoard.getCell(pos1.getRow(), pos1.getCol()).getIconIndex());

            statusPanel.addScore(5);
            if (onFishFeed != null) onFishFeed.run();
            refreshPairInfo();

            if (gameBoard.isAllCleared()) {
                statusPanel.winGame();
                if (onWinCallback != null) {
                    onWinCallback.run();
                }
                JOptionPane.showMessageDialog(BoardPanel.this, "你赢了！");
            }
        } else {
            JOptionPane.showMessageDialog(this, "无法消除该棋子！");
        }

        currentCursorMode = CursorMode.NORMAL;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }

    public void useFreezeTime() {
        if (!started || animating) return;

        currentCursorMode = CursorMode.FREEZE;
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        int freezeSeconds = itemManager.useFreezeTime();
        if (freezeSeconds > 0) {
            statusPanel.addFreezeTime(freezeSeconds);
        } else {
            JOptionPane.showMessageDialog(this, "没有冻结道具了！");
        }

        currentCursorMode = CursorMode.NORMAL;
        setCursor(Cursor.getDefaultCursor());
        updateItemDisplay();
    }

    // ════════════════════════════════════════════════════
    // 坐标映射
    // ════════════════════════════════════════════════════

    /**
     * 像素坐标 → 棋盘行列坐标（超出边界返回 null）
     */
    public Position getPositionByPoint(int x, int y) {
        int col = x / cellWidth;
        int row = y / cellHeight;
        if (row < 0 || row >= totalRow || col < 0 || col >= totalCol) {
            return null;
        }
        return new Position(row, col);
    }

    /**
     * 获取某个棋盘格子在屏幕上的像素矩形
     */
    public Rectangle getRectangle(Position position) {
        int x = position.getCol() * cellWidth;
        int y = position.getRow() * cellHeight;
        return new Rectangle(x, y, cellWidth, cellHeight);
    }

    // ════════════════════════════════════════════════════
    // 连接线绘制
    // ════════════════════════════════════════════════════

    /**
     * 显示消除连接线
     */
    public void showLine(List<Position> path) {
        lineList.clear();
        lineList.add(new Line(path));
        lineVisible = true;
        repaint();
    }

    /**
     * 清除连接线
     */
    public void clearLine() {
        lineVisible = false;
        lineList.clear();
        repaint();
    }

    // ════════════════════════════════════════════════════
    private void loadImages() {
        File dir = new File(skinDir);
        if (!dir.exists()) {
            dir = new File("D:" + File.separator + "game-lianliankan" + File.separator + skinDir);
        }
        imageList.clear();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String fname = file.getName();
                if (fname.endsWith(".png")) {
                    ImageIcon icon = new ImageIcon(file.getPath());
                    imageList.add(icon.getImage());
                }
            }
        }
        // 预缩放到格子大小
        scaledImages = new Image[imageList.size()];
        for (int i = 0; i < imageList.size(); i++) {
            BufferedImage bi = new BufferedImage(cellWidth, cellHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = bi.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(imageList.get(i), 0, 0, cellWidth, cellHeight, null);
            g2d.dispose();
            scaledImages[i] = bi;
        }
        repaint();
    }

    public void setSkinDir(String dir) {
        this.skinDir = dir;
        loadImages();
    }

    // 点击处理（核心交互逻辑）
    // ════════════════════════════════════════════════════

    /**
     * 处理鼠标点击棋盘
     * <p>
     * 流程：
     * 1. 如果未开始或正在动画 → 忽略
     * 2. 第一次选中 → 高亮该格子
     * 3. 第二次选中不同格子 → 判断是否可以消除
     * - 图标不同 → 取消选中
     * - 图标相同且可连接 → 显示连线动画 → 200ms 后消除
     * - 图标相同但不可连接 → 取消选中
     */
    public void handleClick(int x, int y) {
        if (!started) return;
        if (animating) return;

        Position pos = getPositionByPoint(x, y);
        if (pos == null) return;

        Cell clickedCell = gameBoard.getCell(pos.getRow(), pos.getCol());
        if (clickedCell == null || clickedCell.isEmpty()) return;

        // ── 第一次选中 ──
        if (firstSelected == null) {
            gameBoard.clearAllChosen();
            clickedCell.setChosen(true);
            firstSelected = pos;
            repaint();
            return;
        }

        // ── 点击同一位置 → 取消选中 ──
        if (firstSelected.equals(pos)) {
            clickedCell.setChosen(false);
            firstSelected = null;
            secondSelected = null;
            repaint();
            return;
        }

        // ── 第二次选中 ──
        secondSelected = pos;
        Cell firstCell = gameBoard.getCell(firstSelected.getRow(), firstSelected.getCol());
        Cell secondCell = gameBoard.getCell(secondSelected.getRow(), secondSelected.getCol());

        // 图标不同 → 取消选中
        if (firstCell.getIconIndex() != secondCell.getIconIndex()) {
            gameBoard.clearAllChosen();
            firstCell.setChosen(false);
            secondCell.setChosen(false);
            firstSelected = null;
            secondSelected = null;
            repaint();
            return;
        }

        // 图标相同且可连接 → 立即消除
        if (Utils.canLinkAB(gameBoard, firstSelected, secondSelected)) {
            secondCell.setChosen(true);

            MusicManager.playSfx("click");

            List<Position> path = Utils.findPath(gameBoard, firstSelected, secondSelected);
            showLine(path);

            // 瞬间消除（不阻塞输入）
            animating = true;
            effectManager.createShatterEffect(firstSelected, secondSelected, cellWidth, cellHeight, firstCell.getIconIndex());

            firstCell.setEmpty(true);
            secondCell.setEmpty(true);
            statusPanel.addScore(10);

            // COMBO 浮动文字：在消除位置中点显示
            int cx = (firstSelected.getCol() + secondSelected.getCol()) * cellWidth / 2 + cellWidth / 2;
            int cy = (firstSelected.getRow() + secondSelected.getRow()) * cellHeight / 2 + cellHeight / 2;
            if (statusPanel.getComboCount() >= 3) {
                comboTexts.add(new ComboText(cx, cy, "COMBO x" + statusPanel.getComboCount()));
            }

            if (onFishFeed != null) onFishFeed.run();
            refreshPairInfo();

            // 胜利检测
            if (gameBoard.isAllCleared()) {
                statusPanel.winGame();
                if (onWinCallback != null) {
                    onWinCallback.run();
                }
                JOptionPane.showMessageDialog(BoardPanel.this, "你赢了！");
            }

            // 恢复选中状态
            firstCell.setChosen(false);
            secondCell.setChosen(false);
            firstSelected = null;
            secondSelected = null;
            animating = false;

            // 连线保留 200ms 后自动消失（不影响操作）
            Timer lineTimer = new Timer(200, ev -> {
                lineVisible = false;
                lineList.clear();
                repaint();
            });
            lineTimer.setRepeats(false);
            lineTimer.start();

            repaint();
        } else {
            // 不可连接 → 取消选中
            gameBoard.clearAllChosen();
            firstCell.setChosen(false);
            secondCell.setChosen(false);
            firstSelected = null;
            secondSelected = null;
            repaint();
        }
    }

    // ════════════════════════════════════════════════════
    // 自定义绘制
    // ════════════════════════════════════════════════════


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // ── 绘制棋盘格子 ──
        for (int i = 0; i < gameBoard.getRowCnt(); i++) {
            for (int j = 0; j < gameBoard.getColCnt(); j++) {
                Rectangle rec = getRectangle(new Position(i, j));
                int iconIdx = gameBoard.getCell(i, j).getIconIndex();
                if (iconIdx >= 0 && iconIdx < scaledImages.length) {
                    g2.drawImage(scaledImages[iconIdx],
                            rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight(),
                            this
                    );

                    if (gameBoard.getCell(i, j).getIsChosen()) {
                        g2.setColor(new Color(0xe8c87a));
                        g2.setStroke(new BasicStroke(3));
                        g2.drawRect(rec.getX() + 1, rec.getY() + 1,
                                rec.getWidth() - 3, rec.getHeight() - 3);
                    } else if (hintPositions != null) {
                        boolean isHint = false;
                        for (Position hintPos : hintPositions) {
                            if (hintPos.getRow() == i && hintPos.getCol() == j) {
                                isHint = true;
                                break;
                            }
                        }
                        if (isHint) {
                            g2.setColor(new Color(0xffeb3b));
                            g2.setStroke(new BasicStroke(4));
                            g2.drawRect(rec.getX() + 2, rec.getY() + 2,
                                    rec.getWidth() - 5, rec.getHeight() - 5);
                        } else {
                            g2.setColor(new Color(122, 106, 85));
                            g2.setStroke(new BasicStroke(1));
                            g2.drawRect(rec.getX(), rec.getY(),
                                    rec.getWidth() - 1, rec.getHeight() - 1);
                        }
                    } else {
                        g2.setColor(new Color(122, 106, 85));
                        g2.setStroke(new BasicStroke(1));
                        g2.drawRect(rec.getX(), rec.getY(),
                                rec.getWidth() - 1, rec.getHeight() - 1);
                    }
                }
            }

            // ── 绘制连接线 ──
            g2.setColor(new Color(0xe8c87a));
            g2.setStroke(new BasicStroke(3));
            if (lineVisible) {
                for (Line line : lineList) {
                    List<Position> path = line.getPath();
                    java.util.List<Point> pixelPoints = new ArrayList<>();
                    for (Position pos : path) {
                        Point center = getRectangle(pos).getCenterPosition();
                        pixelPoints.add(new Point(center.x, center.y));
                    }
                    effects.RainbowLineEffect.draw(g2, pixelPoints);
                }
            }

            // ── 绘制 bombMode 提示文字 ──
            if (currentCursorMode == CursorMode.BOMB) {
                g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
                g2.setColor(new Color(0xe8c87a));
                String msg = "请点击一个棋子自动消除配对";
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(msg);
                int textX = (getWidth() - textWidth) / 2;
                int textY = fm.getAscent() + 6;
                g2.drawString(msg, textX, textY);
            }

            // ── 绘制破碎特效（在最上层） ──
            effectManager.draw(g2);

            // ── 绘制 COMBO 浮动文字（最上层） ──
            for (ComboText ct : comboTexts) {
                ct.draw(g2);
            }
        }
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public void restoreFromSave(GameBoard saveboard) {
        this.gameBoard = saveboard;
        this.totalRow = saveboard.getRowCnt();
        this.totalCol = saveboard.getColCnt();
        this.firstSelected = null;
        this.secondSelected = null;
        this.lineList.clear();
        this.itemManager = new ItemManager(saveboard);
        this.currentCursorMode = CursorMode.NORMAL;
        setCursor(Cursor.getDefaultCursor());

        gameBoard.clearAllChosen();
        effectManager.clearAll();
        updateItemDisplay();

        repaint();
    }
}
