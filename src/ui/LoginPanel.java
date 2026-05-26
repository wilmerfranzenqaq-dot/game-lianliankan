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
    private JPasswordField passwordField;
    private JTextField catNameField;
    private GameFrame parent;

    public LoginPanel(GameFrame parent) {
        this.parent = parent;
        setLayout(new GridBagLayout());
        setBackground(new Color(0xf4f0e8));

        Font labelFont = new Font("Microsoft YaHei", Font.PLAIN, 16);
        Color labelColor = new Color(0x5a4a3a);
        Color placeholderColor = new Color(0x9a9080);
        Color textColor = new Color(0x3a3530);
        Color fieldBg = new Color(0xfcf9f2);
        Color fieldBorder = new Color(0xe8ddd0);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ── 标题 ──
        JLabel titleLabel = new JLabel("哈基米连连看", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 32));
        titleLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        add(titleLabel, gbc);

        gbc.insets = new Insets(30, 10, 6, 10);
        gbc.gridwidth = 1;
        gbc.gridy = 1;

        // ── 账号 ──
        JLabel accountLabel = new JLabel("账号：");
        accountLabel.setFont(labelFont);
        accountLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(accountLabel, gbc);

        accountField = new JTextField();
        accountField.setText("请输入账号");
        accountField.setForeground(placeholderColor);
        accountField.setOpaque(true);
        accountField.setBackground(fieldBg);
        accountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fieldBorder),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        accountField.setPreferredSize(new Dimension(240, 32));
        accountField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (accountField.getText().equals("请输入账号")) {
                    accountField.setText("");
                    accountField.setForeground(textColor);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (accountField.getText().isEmpty()) {
                    accountField.setText("请输入账号");
                    accountField.setForeground(placeholderColor);
                }
            }
        });
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(accountField, gbc);

        // ── 密码 ──
        JLabel passwordLabel = new JLabel("密码：");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setText("请输入密码");
        passwordField.setEchoChar((char) 0);
        passwordField.setForeground(placeholderColor);
        passwordField.setOpaque(true);
        passwordField.setBackground(fieldBg);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fieldBorder),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        passwordField.setPreferredSize(new Dimension(240, 32));
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                String current = new String(passwordField.getPassword());
                if (current.equals("请输入密码")) {
                    passwordField.setText("");
                    passwordField.setEchoChar('●');
                    passwordField.setForeground(textColor);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                String current = new String(passwordField.getPassword());
                if (current.isEmpty()) {
                    passwordField.setEchoChar((char) 0);
                    passwordField.setText("请输入密码");
                    passwordField.setForeground(placeholderColor);
                }
            }
        });
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        // ── 小猫名字 ──
        JLabel catLabel = new JLabel("小猫名字：");
        catLabel.setFont(labelFont);
        catLabel.setForeground(labelColor);
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.EAST;
        add(catLabel, gbc);

        catNameField = new JTextField();
        catNameField.setText("给小猫取个名字");
        catNameField.setForeground(placeholderColor);
        catNameField.setOpaque(true);
        catNameField.setBackground(fieldBg);
        catNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(fieldBorder),
            BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        catNameField.setPreferredSize(new Dimension(240, 32));
        catNameField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (catNameField.getText().equals("给小猫取个名字")) {
                    catNameField.setText("");
                    catNameField.setForeground(textColor);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (catNameField.getText().isEmpty()) {
                    catNameField.setText("给小猫取个名字");
                    catNameField.setForeground(placeholderColor);
                }
            }
        });
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(catNameField, gbc);

        // ── 按钮 ──
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        RoundedButton loginBtn = new RoundedButton("登录", 0xd4a04a);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setPreferredSize(new Dimension(100, 36));
        buttonPanel.add(loginBtn);

        loginBtn.addActionListener(e -> {
            String username = accountField.getText();
            String password = new String(passwordField.getPassword());
            if (validateUser(username, password)) {
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

        RoundedButton registerBtn = new RoundedButton("注册", 0x8a7a65);
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setPreferredSize(new Dimension(100, 36));
        buttonPanel.add(registerBtn);

        registerBtn.addActionListener(e -> {
            String username = accountField.getText();
            String password = new String(passwordField.getPassword());
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
            if (isCatNameUsedByOthers(catName, "")) {
                JOptionPane.showMessageDialog(this, "已经有小哈基米叫这个名字了！");
                return;
            }
            if (writeUserToFile(username, password, catName)) {
                JOptionPane.showMessageDialog(this, "注册成功！");
                accountField.setText("请输入账号");
                accountField.setForeground(placeholderColor);
                passwordField.setEchoChar((char) 0);
                passwordField.setText("请输入密码");
                passwordField.setForeground(placeholderColor);
            } else {
                JOptionPane.showMessageDialog(this, "注册失败！");
            }
        });

        RoundedButton guestBtn = new RoundedButton("游客模式", 0x9a7a5a);
        guestBtn.setForeground(Color.WHITE);
        guestBtn.setPreferredSize(new Dimension(130, 36));
        buttonPanel.add(guestBtn);

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

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 10, 10);
        add(buttonPanel, gbc);
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
