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
    private String currentPage = "";
    private LeaderBoard leaderBoard;
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
     * @param username   玩家账号名（null 表示游客）
     * @param catName    小猫名字
     * @param isHardMode 是否困难模式
     */
    public void startGame(String username, String catName, boolean isHardMode) {
        if (!gameAdded) {
            currentGamePanel = new GamePanel(isHardMode, leaderBoard, username, catName);
            add(currentGamePanel, "game");
            gameAdded = true;
        }
        showPage("game");
    }

    /** 切换到指定页面 */
    public void showPage(String name) {
        if ("game".equals(currentPage) && !"game".equals(name) && currentGamePanel != null) {
            currentGamePanel.stopEffects();
        }
        cardLayout.show(getContentPane(), name);
        currentPage = name;
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
}
