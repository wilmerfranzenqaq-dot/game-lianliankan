package ui;

import model.LeaderBoard;
import model.LeaderRecord;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.util.List;

/**
 * 排行榜弹窗 — 分屏展示简单/困难两种模式的前 5 名
 *
 * 左表：简单模式 TOP 5
 * 右表：困难模式 TOP 5
 * 字段：排名 #、玩家（显示猫名字，tooltip 指定账号名）、分数、用时（MM:SS）
 */
public class LeaderBoardPanel extends JDialog {

    public LeaderBoard leaderBoard;

    public LeaderBoardPanel(JFrame parent, LeaderBoard leaderBoard) {
        super(parent, "排行榜", true);
        this.leaderBoard = leaderBoard;

        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ThemeColors.CANVAS);

        // ── 标题 ──
        JLabel titleLabel = new JLabel("排行榜 TOP 5", SwingConstants.CENTER);
        titleLabel.setFont(ThemeColors.FONT_TITLE);
        titleLabel.setForeground(ThemeColors.PRIMARY);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(ThemeColors.PAD_CARD, 0, 0, 0));
        add(titleLabel, BorderLayout.NORTH);

        // ── 双表并排 ──
        JPanel tablesPanel = new JPanel(new GridLayout(1, 2, ThemeColors.PAD_CARD, 0));
        tablesPanel.setBackground(ThemeColors.CANVAS);
        tablesPanel.setBorder(BorderFactory.createEmptyBorder(
            ThemeColors.PAD_CARD, ThemeColors.PAD_CARD,
            ThemeColors.PAD_SECTION, ThemeColors.PAD_CARD));
        tablesPanel.add(createTablePanel("简单模式", leaderBoard.getTopRecords("简单模式")));
        tablesPanel.add(createTablePanel("困难模式", leaderBoard.getTopRecords("困难模式")));
        add(tablesPanel, BorderLayout.CENTER);

        // ── 关闭按钮 ──
        JButton closeBtn = new JButton("关闭") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ThemeColors.RADIUS_BTN, ThemeColors.RADIUS_BTN);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        closeBtn.setFont(ThemeColors.FONT_BODY);
        closeBtn.setBackground(ThemeColors.PRIMARY);
        closeBtn.setForeground(ThemeColors.TEXT_ON_GOLD);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setOpaque(false);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.setPreferredSize(new Dimension(100, 36));
        closeBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(ThemeColors.CANVAS);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, ThemeColors.PAD_CARD, 0));
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    /**
     * 创建单张排行榜表格面板
     */
    private JPanel createTablePanel(String title, List<LeaderRecord> records) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(ThemeColors.CANVAS);

        // 自定义标题边框
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(ThemeColors.PRIMARY),
            title,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            ThemeColors.FONT_H2,
            ThemeColors.PRIMARY);
        panel.setBorder(BorderFactory.createCompoundBorder(tb,
            BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        String[] columns = {"#", "玩家", "分数", "用时"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        if (records.isEmpty()) {
            model.addRow(new Object[]{"", "暂无记录", "", ""});
        } else {
            for (int i = 0; i < records.size(); i++) {
                LeaderRecord r = records.get(i);
                model.addRow(new Object[]{i + 1, r.catName, r.score, r.getTimeFormatted()});
            }
        }

        JTable table = new JTable(model);
        table.setFont(ThemeColors.FONT_BODY);
        table.setRowHeight(40);
        table.setForeground(ThemeColors.TEXT);
        table.setBackground(ThemeColors.SURFACE);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ThemeColors.PRIMARY);
        table.setSelectionForeground(ThemeColors.TEXT_ON_GOLD);
        table.setFocusable(false);

        JTableHeader header = table.getTableHeader();
        header.setFont(ThemeColors.FONT_H2);
        header.setForeground(ThemeColors.PRIMARY);
        header.setBackground(ThemeColors.TEXT_ON_GOLD);
        header.setPreferredSize(new Dimension(0, 32));
        header.setBorder(BorderFactory.createEmptyBorder());

        // 自定义渲染器：玩家列加 tooltip 显示用户名
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value,
                        isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? ThemeColors.SURFACE : ThemeColors.SURFACE_2);
                    setForeground(ThemeColors.TEXT);
                } else {
                    setBackground(ThemeColors.PRIMARY);
                    setForeground(ThemeColors.TEXT_ON_GOLD);
                }
                // 从 records 取用户名作为 tooltip
                if (row < records.size()) {
                    setToolTipText(records.get(row).userName);
                }
                return c;
            }
        });

        // 其他列居中
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 1) continue; // 玩家列上面已经单独设了
            table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object value,
                        boolean isSelected, boolean hasFocus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(tbl, value,
                            isSelected, hasFocus, row, col);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                    if (!isSelected) {
                        setBackground(row % 2 == 0 ? ThemeColors.SURFACE : ThemeColors.SURFACE_2);
                        setForeground(ThemeColors.TEXT);
                    } else {
                        setBackground(ThemeColors.PRIMARY);
                        setForeground(ThemeColors.TEXT_ON_GOLD);
                    }
                    return c;
                }
            });
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(ThemeColors.SURFACE);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
}
