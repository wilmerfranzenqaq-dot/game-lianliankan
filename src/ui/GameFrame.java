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

    public GameFrame(String title, int width, int height) {
        super(title);
        setSize(width, height);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 关闭窗口时停止音乐
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                MusicManager.stop();
            }
        });

        setLocationRelativeTo(null);

        // 初始化排行榜数据层（全局共享）
        leaderBoard = new LeaderBoard();
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        // 预创建 splash 和 login 两个页面
        add(new SplashPanel(this), "splash");
        add(new LoginPanel(this), "login");

        showPage("splash");
        setVisible(true);
    }

    /**
     * 登录成功后调用 — 创建游戏页面并切换过去
     * @param username   玩家账号名（null 表示游客）
     * @param isHardMode 是否困难模式
     */
    public void startGame(String username, boolean isHardMode) {
        if (!gameAdded) {
            add(new GamePanel(isHardMode, leaderBoard, username), "game");
            gameAdded = true;
        }
        showPage("game");
    }

    /** 切换到指定页面 */
    public void showPage(String name) {
        cardLayout.show(getContentPane(), name);
    }
}
