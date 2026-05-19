package ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import utils.MusicManager;

/**
 * 游戏状态面板 — HUD 显示得分、倒计时、连击、配对进度
 *
 * 四栏布局：
 *   左：当前分数 + COMBO 标签
 *   中：剩余时间（倒计时 120s → 0 判负）
 *   中右：关卡进度（剩余配对数 + 完成百分比）
 *   右：已用时间
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
    JLabel hintCountLabel;
    JLabel shuffleCountLabel;
    JLabel bombCountLabel;
    JLabel freezeTimeCountLabel;

    // ── 定时器 ──
    Timer timer;           // 每秒更新倒计时
    Timer comboTimer;
    Timer freezeTimer;
    boolean isTimeFrozen = false;
    Runnable onUseHint;
    Runnable onUseShuffle;
    Runnable onUseBomb;
    Runnable onUseFreezeTime;// COMBO 超时检测

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
        setBackground(new Color(0x5c4a3a));
        setOpaque(true);
        // 底部 hairline border 与 BoardPanel 分隔
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x7a6a52)));

        // ── 字体 ──
        Font cjkFont = new Font("Microsoft YaHei", Font.PLAIN, 13);
        Font numFont = new Font("Arial", Font.BOLD, 28);
        Font comboFont = new Font("Microsoft YaHei", Font.BOLD, 14);
        Font itemFont = new Font("Microsoft YaHei", Font.PLAIN, 12);

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
        timeLabel.setForeground(new Color(0xf5e6c8));

        // ── 已用时间 ──
        timeuseLabel = new JLabel("已经用时", SwingConstants.CENTER);
        timeuseLabel.setFont(cjkFont);
        timeuseLabel.setForeground(new Color(0xa09070));

        timecountLabel = new JLabel("00:00", SwingConstants.CENTER);
        timecountLabel.setFont(numFont);
        timecountLabel.setForeground(new Color(0xc4b091));

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

        hintCountLabel = new JLabel("提示: 3", SwingConstants.CENTER);
        hintCountLabel.setFont(itemFont);
        hintCountLabel.setForeground(new Color(0xffeb3b));

        shuffleCountLabel = new JLabel("重排: 3", SwingConstants.CENTER);
        shuffleCountLabel.setFont(itemFont);
        shuffleCountLabel.setForeground(new Color(0x4caf50));

        bombCountLabel = new JLabel("炸弹: 2", SwingConstants.CENTER);
        bombCountLabel.setFont(itemFont);
        bombCountLabel.setForeground(new Color(0xf44336));

        freezeTimeCountLabel = new JLabel("冻结: 2", SwingConstants.CENTER);
        freezeTimeCountLabel.setFont(itemFont);
        freezeTimeCountLabel.setForeground(new Color(0x2196f3));



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
            secondsUsed = countSeconds % 60;
            minutesUsed = countSeconds / 60;

            if (totalSeconds == 0) {
                timer.stop();
                JFrame p = (JFrame) SwingUtilities.getWindowAncestor(StatusPanel.this);
                GameResultDialog.showLose(p);
            }
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

        // ── 五栏布局 ──
        setLayout(new GridLayout(1, 5));

        // 第 1 栏：分数 + COMBO
        JPanel left = new JPanel(new BorderLayout());
        left.setBackground(new Color(0x5c4a3a));
        left.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        JPanel scoreBox = new JPanel(new BorderLayout());
        scoreBox.setOpaque(false);
        scoreBox.add(scoreLabel, BorderLayout.NORTH);
        scoreBox.add(scoreValue, BorderLayout.CENTER);
        left.add(scoreBox, BorderLayout.CENTER);
        left.add(comboLabel, BorderLayout.SOUTH);

        // 第 2 栏：剩余时间
        JPanel middle = new JPanel(new GridBagLayout());
        middle.setBackground(new Color(0x5c4a3a));
        JPanel middleInner = new JPanel(new GridLayout(2, 1));
        middleInner.setBackground(new Color(0x5c4a3a));
        middleInner.add(statusLabel);
        middleInner.add(timeLabel);
        middle.add(middleInner);

        // 第 3 栏：配对进度
        Font pairFont = new Font("Microsoft YaHei", Font.PLAIN, 12);
        remainingPairLabel = new JLabel("剩余可消除：0对", SwingConstants.CENTER);
        remainingPairLabel.setFont(pairFont);
        remainingPairLabel.setForeground(new Color(0xf5e6c8));

        progressLabel = new JLabel("关卡进度：0%", SwingConstants.CENTER);
        progressLabel.setFont(pairFont);
        progressLabel.setForeground(new Color(0xf5e6c8));

        JPanel pairPanel = new JPanel(new GridBagLayout());
        pairPanel.setBackground(new Color(0x5c4a3a));
        JPanel pairInner = new JPanel(new GridLayout(2, 1));
        pairInner.setBackground(new Color(0x5c4a3a));
        pairInner.add(remainingPairLabel);
        pairInner.add(progressLabel);
        pairPanel.add(pairInner);

        JPanel itemPanel = new JPanel(new GridLayout(4, 1));
        itemPanel.setBackground(new Color(0x5c4a3a));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        itemPanel.add(hintCountLabel);
        itemPanel.add(shuffleCountLabel);
        itemPanel.add(bombCountLabel);
        itemPanel.add(freezeTimeCountLabel);

        // 第 4 栏：已用时间
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(new Color(0x5c4a3a));
        JPanel rightInner = new JPanel(new GridLayout(2, 1));
        rightInner.setBackground(new Color(0x5c4a3a));
        rightInner.add(timeuseLabel);
        rightInner.add(timecountLabel);
        right.add(rightInner);

        add(left);
        add(middle);
        add(pairPanel);
        add(itemPanel);
        add(right);
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
        statusLabel.setText("你赢了！");
    }

    public void updateItemDisplay(int hints, int shuffles, int bombs, int freezes) {
        hintCountLabel.setText("提示: " + hints);
        shuffleCountLabel.setText("重排: " + shuffles);
        bombCountLabel.setText("炸弹: " + bombs);
        freezeTimeCountLabel.setText("冻结: " + freezes);
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
        updateItemDisplay(3, 3, 2, 2);
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
