package ui;

import model.LeaderBoard;
import utils.MusicManager;

import javax.swing.*;
import java.awt.*;

/**
 * 主窗口 — CardLayout 多页面容器
 *
 * 管理三个页面的切换：
 *   "splash" → 动画启动页
 *   "login"  → 登录/注册页
 *   "game"   → 游戏主界面（登录成功后才创建）
 */
public class GameFrame extends JFrame {

    private CardLayout cardLayout;
    private boolean gameAdded = false;
    private LeaderBoard leaderBoard;
    private LoadingOverlay loadingOverlay;
    JMenuBar menuBar;
    JMenu gameMenu;
    JMenu helpMenu;
    private GamePanel currentGamePanel;

    public GameFrame(String title, int contentW, int contentH) {
        super(title);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 关闭窗口时停止音乐
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                MusicManager.stop();
            }
        });

        // 初始化排行榜数据层（全局共享）
        leaderBoard = new LeaderBoard();
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // 预创建 splash 和 login 两个页面
        add(new SplashPanel(this), "splash");
        add(new LoginPanel(this), "login");

        // ── 菜单栏 ──
        createMenuBar();
        setJMenuBar(menuBar);

        showPage("splash");
        setVisible(true);

        // 窗口显示后，补偿标题栏+边框，确保内容区恰为 contentW × contentH
        SwingUtilities.invokeLater(() -> {
            Insets insets = getInsets();
            setSize(contentW + insets.left + insets.right,
                    contentH + insets.top + insets.bottom);
            setLocationRelativeTo(null);
        });
    }

    /**
     * 登录成功后调用 — 创建游戏页面并切换过去
     * @param username   玩家账号名
     * @param catName    小猫名字
     * @param isHardMode 是否困难模式
     */
    /**
     * 登录成功后调用 — 先显示加载动画，再创建游戏页面并切换过去
     */
    public void startGame(String username, String catName, boolean isHardMode) {
        // 显示加载动画
        if (loadingOverlay == null) {
            loadingOverlay = new LoadingOverlay();
            add(loadingOverlay, "loading");
        }
        showPage("loading");

        // 延迟一帧再创建游戏界面（确保加载条动画能跑）
        Timer timer = new Timer(400, e -> {
            if (!gameAdded) {
                currentGamePanel = new GamePanel(isHardMode, leaderBoard, username, catName);
                add(currentGamePanel, "game");
                gameAdded = true;
            }
            showPage("game");
        });
        timer.setRepeats(false);
        timer.start();
    }

    /** 切换到指定页面 */
    public void showPage(String name) {
        cardLayout.show(getContentPane(), name);
    }

    /** 创建菜单栏 */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(new Color(0x3a3023));

        // ── 游戏菜单 ──
        gameMenu = new JMenu("游戏");
        gameMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        gameMenu.setForeground(new Color(0xe8c87a));
        gameMenu.setMnemonic('G');

        JMenuItem startItem = new JMenuItem("开始  Space");
        startItem.setMnemonic('S');
        startItem.addActionListener(e -> {
            if (currentGamePanel != null) currentGamePanel.getStartButton().doClick();
        });

        JMenuItem restartItem = new JMenuItem("重新开始  R");
        restartItem.setMnemonic('R');
        restartItem.addActionListener(e -> {
            if (currentGamePanel != null) currentGamePanel.getRestartButton().doClick();
        });

        JMenuItem saveItem = new JMenuItem("保存  S");
        saveItem.setMnemonic('S');
        saveItem.setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
            java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() == 0
                ? java.awt.event.InputEvent.CTRL_DOWN_MASK
                : java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> {
            if (currentGamePanel != null) currentGamePanel.getSaveButton().doClick();
        });

        JMenuItem loadItem = new JMenuItem("加载  L");
        loadItem.setMnemonic('L');
        loadItem.addActionListener(e -> {
            if (currentGamePanel != null) currentGamePanel.getLoadButton().doClick();
        });

        gameMenu.add(startItem);
        gameMenu.add(restartItem);
        gameMenu.addSeparator();
        gameMenu.add(saveItem);
        gameMenu.add(loadItem);

        // ── 帮助菜单 ──
        helpMenu = new JMenu("帮助");
        helpMenu.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        helpMenu.setForeground(new Color(0xe8c87a));
        helpMenu.setMnemonic('H');

        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.setMnemonic('A');
        aboutItem.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "连连看 v1.1\nJava Swing 实现\nGitHub: yubailing666/game-lianliankan",
                "关于", JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);

        menuBar.add(gameMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
    }

    // ── 加载动画面板 ──

    /**
     * 细条加载动画：深木底色 + 一条金色小条来回滚动
     * 模仿成熟游戏的 loading bar 风格
     */
    class LoadingOverlay extends JPanel {

        private float progress = 0f;
        private int direction = 1;

        LoadingOverlay() {
            setBackground(new Color(0x2c2822)); // 深木色

            Timer animTimer = new Timer(16, e -> {
                progress += direction * 0.025f;
                if (progress > 0.85f || progress < 0.05f) {
                    direction = -direction;
                }
                repaint();
            });
            animTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // 中间细条轨道
            int barW = w / 3;
            int barH = 4;
            int barX = w / 2 - barW / 2;
            int barY = h / 2 - barH / 2;

            // 轨道底色
            g2.setColor(new Color(0x4a443a));
            g2.fillRoundRect(barX, barY, barW, barH, 4, 4);

            // 金色进度条
            int fillW = (int) (barW * progress);
            if (fillW > 0) {
                g2.setColor(new Color(0xd4a04a));
                g2.fillRoundRect(barX, barY, fillW, barH, 4, 4);
            }

            g2.dispose();
        }
    }
}
