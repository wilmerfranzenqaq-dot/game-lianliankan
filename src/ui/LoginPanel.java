package ui;

import utils.MusicManager;
import utils.PathManager;

import javax.swing.*;
import java.awt.*;
import java.io.*;

/**
 * 登录/注册页面 — 用户凭据管理页
 *
 * 功能：
 *   - 账号 + 密码输入框（带 placeholder 提示文字）
 *   - 小猫名字输入框
 *   - 登录按钮 → 验证凭据，成功后弹出难度选择对话框
 *   - 注册按钮 → 写入 user.txt（明文 CSV 格式）
 *   - 背景图片（resource/background.png）
 *
 * 用户数据存储：项目根目录 user.txt，格式为 username,password 每行一条
 */
public class LoginPanel extends JPanel {

    private static final File USER_FILE = PathManager.getUserFile();

    // ── UI 组件 ──
    private JTextField accountField;
    private JTextField passwordField;
    private JTextField catNameField;
    private GameFrame parent;

    public LoginPanel(GameFrame parent) {
        this.parent = parent;
        setLayout(null);
        setBackground(new Color(0xf4f0e8));
        setSize(parent.getWidth(), parent.getHeight());

        // ── 账号输入框 ──
        accountField = new JTextField();
        accountField.setText("请输入账号");
        accountField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (accountField.getText().equals("请输入账号")) {
                    accountField.setText("");
                    accountField.setForeground(new Color(0x3a3530));
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (accountField.getText().isEmpty()) {
                    accountField.setText("请输入账号");
                    accountField.setForeground(new Color(0x9a9080));
                }
            }
        });
        accountField.setSize(170, 32);
        accountField.setLocation(110, 100);
        accountField.setForeground(new Color(0x9a9080));
        accountField.setOpaque(true);
        accountField.setBackground(new Color(0xfcf9f2));
        accountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xe8ddd0)),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        add(accountField);

        // ── 密码输入框 ──
        passwordField = new JTextField();
        passwordField.setText("请输入密码");
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (passwordField.getText().equals("请输入密码")) {
                    passwordField.setText("");
                    passwordField.setForeground(new Color(0x3a3530));
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (passwordField.getText().isEmpty()) {
                    passwordField.setText("请输入密码");
                    passwordField.setForeground(new Color(0x9a9080));
                }
            }
        });
        passwordField.setSize(170, 32);
        passwordField.setLocation(110, 140);
        passwordField.setForeground(new Color(0x9a9080));
        passwordField.setOpaque(true);
        passwordField.setBackground(new Color(0xfcf9f2));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xe8ddd0)),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        add(passwordField);

        // ── 小猫名字输入框 ──
        catNameField = new JTextField();
        catNameField.setText("给小猫取个名字");
        catNameField.setForeground(new Color(0x9a9080));
        catNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (catNameField.getText().equals("给小猫取个名字")) {
                    catNameField.setText("");
                    catNameField.setForeground(new Color(0x3a3530));
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (catNameField.getText().isEmpty()) {
                    catNameField.setText("给小猫取个名字");
                    catNameField.setForeground(new Color(0x9a9080));
                }
            }
        });
        catNameField.setSize(170, 32);
        catNameField.setLocation(110, 180);
        catNameField.setOpaque(true);
        catNameField.setBackground(new Color(0xfcf9f2));
        catNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xe8ddd0)),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        add(catNameField);

        // ── 登录按钮 ──
        RoundedButton loginBtn = new RoundedButton("登录", 0xd4a04a);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setBounds(60, 225, 80, 32);
        add(loginBtn);

        loginBtn.addActionListener(e -> {
            String username = accountField.getText();
            String password = passwordField.getText();
            if (validateUser(username, password)) {
                // 登录成功 → 弹出难度选择对话框 → 进入游戏
                String[] options = {"简单模式", "困难模式"};
                int choice = JOptionPane.showOptionDialog(this,
                        "选择游戏难度", "连连看",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);
                boolean isHardMode = (choice == 1);
                String catName = catNameField.getText();
                if (catName.equals("给小猫取个名字") || catName.trim().isEmpty()) {
                    catName = "Mimi";
                }
                MusicManager.play("game");
                parent.startGame(username, catName, isHardMode);
            } else {
                JOptionPane.showMessageDialog(this, "账号或密码错误！");
            }
        });

        // ── 注册按钮 ──
        RoundedButton registerBtn = new RoundedButton("注册", 0x8a7a65);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBounds(140, 225, 80, 32);
        add(registerBtn);

        registerBtn.addActionListener(e -> {
            String username = accountField.getText();
            String password = passwordField.getText();
            if (username.equals("请输入账号") || username.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入账号！");
                return;
            }
            if (password.equals("请输入密码") || password.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请输入密码！");
                return;
            }
            if (checkUserExists(username)) {
                JOptionPane.showMessageDialog(this, "用户已存在！");
                return;
            }
            String catName = catNameField.getText();
            if (catName.equals("给小猫取个名字") || catName.trim().isEmpty()) {
                catName = "Mimi";
            }
            // 检测猫名是否已被其他用户使用（排除自己——注册时自己是新用户，不存在）
            if (isCatNameUsedByOthers(catName, "")) {
                JOptionPane.showMessageDialog(this, "已经有小哈基米叫这个名字了！");
                return;
            }
            if (writeUserToFile(username, password, catName)) {
                JOptionPane.showMessageDialog(this, "注册成功！");
                accountField.setText("请输入账号");
                passwordField.setText("请输入密码");
            } else {
                JOptionPane.showMessageDialog(this, "注册失败！");
            }
        });

        RoundedButton guestBtn = new RoundedButton("游客模式", 0x9a7a5a);
        guestBtn.setForeground(Color.WHITE);
        guestBtn.setBounds(220, 225, 120, 32);
        add(guestBtn);

        guestBtn.addActionListener(e -> {
            String[] options = {"简单模式", "困难模式"};
            int choice = JOptionPane.showOptionDialog(this,
                    "选择游戏难度", "连连看",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            boolean isHardMode = (choice == 1);
            MusicManager.play("game");
            parent.startGame(null, "Mimi", isHardMode);
        });

        // ── 背景图片 ──
        ImageIcon bgIcon = PathManager.getResourceImageIcon("background.png", "resource/background.png");
        if (bgIcon != null) {
            JLabel bgLabel = new JLabel(bgIcon);
            bgLabel.setSize(400, 300);
            bgLabel.setLocation(-10, -6);
            add(bgLabel);
        }
    }

    // ── 用户数据管理（静态方法，读写 user.txt） ──

    /** 检查用户名是否已存在 */
    private static boolean checkUserExists(String username) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(username)) return true;
            }
        } catch (IOException e) {
            // 文件不存在或无法读取 → 视为不存在
        }
        return false;
    }

    /** 将新用户追加写入 user.txt（含小猫名字） */
    private static boolean writeUserToFile(String name, String password, String catName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            // 格式：username,password,nickname,avatarFilename,catsName
            writer.write(name + "," + password + ",,," + catName);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** 验证用户名和密码是否匹配（兼容 2 列和 5 列格式） */
    private static boolean validateUser(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && parts[0].equals(username) && parts[1].equals(password))
                    return true;
            }
        } catch (IOException e) {
            // 文件不存在或无法读取 → 验证失败
        }
        return false;
    }

    /**
     * 检测猫名是否已被其他用户使用
     * @param catName 要检测的猫名
     * @param excludeUser 排除的用户名（自己的旧名），空串代表不排除
     * @return true 已被占用
     */
    private static boolean isCatNameUsedByOthers(String catName, String excludeUser) {
        if (catName == null || catName.trim().isEmpty()) return false;
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String user = parts[0].trim();
                    String existingCat = parts[4].trim();
                    if (existingCat.equals(catName.trim()) && !user.equals(excludeUser)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            // 文件不存在 → 无占用
        }
        return false;
    }
}
