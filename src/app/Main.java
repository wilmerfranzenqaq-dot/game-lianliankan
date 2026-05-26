package app;

import ui.GameFrame;

import javax.swing.*;

/**
 * 连连看游戏入口 — 启动 Swing 主窗口
 *
 * 在事件分发线程 (EDT) 上创建 GameFrame，确保线程安全
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameFrame("连连看", 1000, 1000);
        });
    }
}
