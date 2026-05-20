package ui;

import javax.swing.*;
import java.awt.*;

/**
 * 游戏状态面板 — HUD 显示得分、倒计时、配对进度、已用时间
 *
 * 四栏 GridBagLayout：
 *   左（weightx=1.0, anchor=WEST）：当前分数 + COMBO 标签
 *   中（weightx=1.0, anchor=CENTER）：剩余时间
 *   中右（weightx=1.0, anchor=CENTER）：关卡进度（剩余配对数 + 完成百分比）
 *   右（weightx=1.0, anchor=CENTER）：已用时间
 *
 * 连击系统：
 *   - 3 秒内连续消除 → COMBO 计数递增
 *   - COMBO ≥ 3 时，单次得分 × (COMBO - 1)
 *   - 触发 COMBO 时启动彩虹闪烁动画（AnimationThread 内部类）
 */
public class StatusPanel extends JPanel {

    // ── 常量 ──
    private static final long COMBO_TIMEOUT = 3000;  // 连击超时 3 秒

    // ── UI 组件 ──
    JLabel scoreLabel;
    JLabel scoreValue;
    JLabel statusLabel;
    JLabel timeLabel;
    JLabel timeuseLabel;
    JLabel timecountLabel;
    JLabel comboLabel;
    JLabel remainingPairLabel;
    JLabel progressLabel;

    // ── 定时器 ──
    Timer timer;           // 每秒更新倒计时
    Timer comboTimer;
    Timer freezeTimer;
    boolean isTimeFrozen = false;
    boolean gameStarted = false;    // 标记游戏是否已经开始

    // ── 游戏数据 ──
    int totalSeconds;       // 剩余秒数
    int countSeconds;       // 已用秒数
    int secondsUsed;
    int minutesUsed;
    int seconds;
    int minutes;
    int score;
    int comboCount;
    long lastEliminationTime;   // 上次消除的时间戳（用于 COMBO 判断）

    // ════════════════════════════════════════════════════
    // 构造
    // ════════════════════════════════════════════════════

    public StatusPanel(int offSetX, int offSetY, int width, int height) {
        setBounds(offSetX, offSetY, width, height);
        setBackground(new Color(0x4a3d2e)); // SURFACE_2
        setOpaque(true);

        // ── 字体 ──
        Font cjkFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        Font numFont = new Font("Arial", Font.BOLD, 28);
        Font comboFont = new Font("Microsoft YaHei", Font.BOLD, 14);

        // ── 分数 ──
        scoreLabel = new JLabel("分数", SwingConstants.CENTER);
        scoreLabel.setFont(cjkFont);
        scoreLabel.setForeground(new Color(0xa09070));

        scoreValue = new JLabel("0", SwingConstants.CENTER);
        scoreValue.setFont(numFont);
        scoreValue.setForeground(new Color(0xe8c87a));

        // ── 剩余时间 ──
        statusLabel = new JLabel("剩余时间", SwingConstants.CENTER);
        statusLabel.setFont(cjkFont);
        statusLabel.setForeground(new Color(0xa09070));

        timeLabel = new JLabel("00:00", SwingConstants.CENTER);
        timeLabel.setFont(numFont);
        timeLabel.setForeground(new Color(0xe8c87a));

        // ── 已用时间 ──
        timeuseLabel = new JLabel("已经用时", SwingConstants.CENTER);
        timeuseLabel.setFont(cjkFont);
        timeuseLabel.setForeground(new Color(0xa09070));

        timecountLabel = new JLabel("00:00", SwingConstants.CENTER);
        timecountLabel.setFont(numFont);
        timecountLabel.setForeground(new Color(0xe8c87a));

        // ── COMBO 标签 ──
        comboLabel = new JLabel("", SwingConstants.CENTER);
        comboLabel.setFont(comboFont);
        comboLabel.setForeground(new Color(255, 69, 0));
        comboLabel.setOpaque(true);
        comboLabel.setBackground(new Color(255, 255, 220));
        comboLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 165, 0), 2),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));
        comboLabel.setVisible(false);

        // ── 游戏参数初始化 ──
        totalSeconds = 120;
        countSeconds = 0;
        comboCount = 0;
        lastEliminationTime = 0;

        // ── COMBO 超时定时器 ──
        comboTimer = new Timer((int) COMBO_TIMEOUT, e -> {
            comboLabel.setVisible(false);
            comboCount = 0;
            comboTimer.stop();
        });
        comboTimer.setRepeats(false);

        freezeTimer = new Timer(1000, e -> {
            totalSeconds++;
            if (totalSeconds > 120) {
                totalSeconds = 120;
                freezeTimer.stop();
                isTimeFrozen = false;
            }
            seconds = totalSeconds % 60;
            minutes = totalSeconds / 60;
            timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
        });

        timer = new Timer(1000, e -> {
            if (!isTimeFrozen) {
                totalSeconds--;
                countSeconds++;
                seconds = totalSeconds % 60;
                minutes = totalSeconds / 60;
                secondsUsed = countSeconds % 60;
                minutesUsed = countSeconds / 60;

                if (totalSeconds == 0) {
                    timer.stop();
                    JOptionPane.showMessageDialog(null, "你输了！");
                }
                timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
                timecountLabel.setText(String.format("%02d:%02d", minutesUsed, secondsUsed));
            }
        });

        // ── 四栏 GridBagLayout ──
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // 第 1 栏：分数（weightx=1.0, anchor=WEST — 左对齐）
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(new Color(0x4a3d2e));
        left.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 8));
        JPanel scoreBox = new JPanel(new BorderLayout());
        scoreBox.setOpaque(false);
        scoreBox.add(scoreLabel, BorderLayout.NORTH);
        scoreBox.add(scoreValue, BorderLayout.CENTER);
        left.add(scoreBox, BorderLayout.CENTER);
        left.add(comboLabel, BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(left, gbc);

        // 第 2 栏：剩余时间（weightx=1.0, anchor=CENTER — 居中对齐）
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setBackground(new Color(0x4a3d2e));
        JPanel middleInner = new JPanel(new GridLayout(2, 1));
        middleInner.setBackground(new Color(0x4a3d2e));
        middleInner.add(statusLabel);
        middleInner.add(timeLabel);
        middle.add(middleInner);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(middle, gbc);

        // 第 3 栏：配对进度（weightx=1.0, anchor=CENTER — 居中）
        Font pairFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        remainingPairLabel = new JLabel("剩余可消除：0对", SwingConstants.CENTER);
        remainingPairLabel.setFont(pairFont);
        remainingPairLabel.setForeground(new Color(0xf5e6c8));

        progressLabel = new JLabel("关卡进度：0%", SwingConstants.CENTER);
        progressLabel.setFont(pairFont);
        progressLabel.setForeground(new Color(0xf5e6c8));

        JPanel pairPanel = new JPanel(new GridBagLayout());
        pairPanel.setBackground(new Color(0x4a3d2e));
        JPanel pairInner = new JPanel(new GridLayout(2, 1));
        pairInner.setBackground(new Color(0x4a3d2e));
        pairInner.add(remainingPairLabel);
        pairInner.add(progressLabel);
        pairPanel.add(pairInner);

        gbc.gridx = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(pairPanel, gbc);

        // 第 4 栏：已用时间（weightx=1.0, anchor=CENTER — 居中对齐）
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(new Color(0x4a3d2e));
        JPanel rightInner = new JPanel(new GridLayout(2, 1));
        rightInner.setBackground(new Color(0x4a3d2e));
        rightInner.add(timeuseLabel);
        rightInner.add(timecountLabel);
        right.add(rightInner);

        gbc.gridx = 3;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        add(right, gbc);
    }

    // ════════════════════════════════════════════════════
    // 计时器控制（供 SettingsDialog 使用）
    // ════════════════════════════════════════════════════

    /**
     * 暂停倒计时（打开设置对话框时调用）
     */
    public void pauseTimer() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
    }

    /**
     * 恢复倒计时（关闭设置对话框时调用）
     */
    public void resumeTimer() {
        if (gameStarted && timer != null && !timer.isRunning()) {
            timer.start();
        }
    }

    // ════════════════════════════════════════════════════
    // 查询
    // ════════════════════════════════════════════════════

    public int getScore() {
        return score;
    }

    /** 获取已用秒数（用于排行榜记录） */
    public int getTimeUsed() {
        return countSeconds;
    }

    // ════════════════════════════════════════════════════
    // 更新
    // ════════════════════════════════════════════════════

    /** 更新配对进度信息 */
    public void updatePairInfo(int remainingPairs, int clearedPairs, int totalPairs) {
        remainingPairLabel.setText("剩余可消除：" + remainingPairs + "对");
        int progress = clearedPairs * 100 / totalPairs;
        progressLabel.setText("关卡进度: " + progress + "%");
    }

    /** 启动游戏倒计时 */
    public void startGame() {
        statusLabel.setText("进行中");
        gameStarted = true;
        timer.start();
    }

    /**
     * 增加分数，并处理 COMBO 逻辑
     * 3 秒内的连续消除算 COMBO，COMBO ≥ 3 时得分倍增
     */
    public void addScore(int points) {
        long currTime = System.currentTimeMillis();

        if (lastEliminationTime == 0 || (currTime - lastEliminationTime) > COMBO_TIMEOUT) {
            comboCount = 1;   // 断连 → 重置
        } else {
            comboCount++;     // 连续 → 递增
        }

        lastEliminationTime = currTime;

        int bonusPoints = points;
        if (comboCount >= 3) {
            bonusPoints = points * (comboCount - 1);
            showComboMessage(comboCount, bonusPoints);
        }

        score += bonusPoints;
        scoreValue.setText(String.valueOf(score));

        if (comboCount >= 3) {
            comboTimer.restart();
        }
    }

    /** 显示 COMBO 提示并启动彩虹闪烁动画 */
    private void showComboMessage(int comboCount, int bonusPoints) {
        String message = String.format(">>>COMBO x%d! +%d分<<<", comboCount, bonusPoints);

        comboLabel.setText(message);
        comboLabel.setVisible(true);

        revalidate();
        repaint();

        AnimationThread anim = new AnimationThread(comboLabel);
        anim.start();
    }

    /** 胜利 — 停止倒计时 */
    public void winGame() {
        timer.stop();
        gameStarted = false;
        statusLabel.setText("你赢了！");
    }

    public void addFreezeTime(int seconds) {
        isTimeFrozen = true;
        freezeTimer.start();
    }

    /** 重置所有状态（重新开始游戏时调用） */
    public void resetGame() {
        comboCount = 0;
        lastEliminationTime = 0;
        comboLabel.setVisible(false);
        comboTimer.stop();
        freezeTimer.stop();
        isTimeFrozen = false;
        gameStarted = false;

        totalSeconds = 120;
        countSeconds = 0;
        score = 0;
        scoreValue.setText("0");
        timeLabel.setText("02:00");
        timecountLabel.setText("00:00");
        statusLabel.setText("按 Start 开始");
        timer.stop();

        remainingPairLabel.setText("剩余可消除: 0对");
        progressLabel.setText("关卡进度: 0%");
    }

    // ════════════════════════════════════════════════════
    // 存档相关方法
    // ════════════════════════════════════════════════════

    public int getRemainingSeconds() {
        return totalSeconds;
    }
    public int getElapsedSeconds() {
        return countSeconds;
    }
    public int getComboCount(){
        return comboCount;
    }
    public long getLastEliminationTime() {
        return lastEliminationTime;
    }
    public void setRemainingSeconds(int seconds) {
        this.totalSeconds = seconds;
        this.seconds = seconds % 60;
        this.minutes = seconds / 60;
        timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    /**
     * 设置已用秒数（加载存档时调用）
     */
    public void setElapsedSeconds(int seconds) {
        this.countSeconds = seconds;
        this.secondsUsed = seconds % 60;
        this.minutesUsed = seconds / 60;
        timecountLabel.setText(String.format("%02d:%02d", minutesUsed, secondsUsed));
    }

    /**
     * 设置分数（加载存档时调用）
     */
    public void setScore(int score) {
        this.score = score;
        scoreValue.setText(String.valueOf(score));
    }

    /**
     * 设置连击状态（加载存档时调用）
     */
    public void setComboState(int comboCount, long lastTime) {
        this.comboCount = comboCount;
        this.lastEliminationTime = lastTime;
        if (comboCount >= 3) {
            String message = String.format(">>>COMBO x%d! <<<", comboCount);
            comboLabel.setText(message);
            comboLabel.setVisible(true);
            comboTimer.restart();
        }
    }

    /**
     * 启动计时器（加载存档后调用）
     */
    public void startTimer() {
        statusLabel.setText("进行中");
        gameStarted = true;
        timer.start();
    }

    // ════════════════════════════════════════════════════
    // 内部类：COMBO 彩虹闪烁动画
    // ════════════════════════════════════════════════════

    /**
     * COMBO 标签彩虹色闪烁动画线程
     * 在 7 种颜色间循环 3 轮，每帧 100ms，结束后恢复橙色
     */
    static class AnimationThread extends Thread {
        JLabel label;

        public AnimationThread(JLabel label) {
            this.label = label;
        }

        @Override
        public void run() {
            try {
                Color[] rainbowColors = {
                        Color.RED,
                        new Color(255, 127, 0),
                        Color.YELLOW,
                        Color.GREEN,
                        Color.CYAN,
                        Color.BLUE,
                        new Color(148, 0, 211)
                };

                for (int cycle = 0; cycle < 3; cycle++) {
                    for (int i = 0; i < rainbowColors.length; i++) {
                        final Color c = rainbowColors[i];
                        SwingUtilities.invokeLater(() -> label.setForeground(c));
                        Thread.sleep(100);
                    }
                }
                SwingUtilities.invokeLater(() -> label.setForeground(new Color(255, 165, 0)));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
