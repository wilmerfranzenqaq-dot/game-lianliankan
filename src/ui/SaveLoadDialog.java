package ui;

import utils.SaveManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 存档/读档对话框 — 使用 ThemeColors Design Token
 */
public class SaveLoadDialog extends JDialog {

    private static final String SAVE_DIR = "resource/savedata/";
    private static final int SLOT_COUNT = 9;

    private final boolean saveMode;
    private final GamePanel gamePanel;
    private final String username;
    private final String mode;
    private DefaultTableModel tableModel;
    private JTable slotTable;
    private JButton btnAction;
    private JButton btnDelete;

    /** 保持原构造函数签名不变 */
    public SaveLoadDialog(JFrame parent, GamePanel gamePanel, String username, String mode) {
        // 外部没传 saveMode → 默认读档模式（load）
        this(parent, gamePanel, username, mode, true);
    }

    /** 完整构造函数 */
    public SaveLoadDialog(JFrame parent, GamePanel gamePanel, String username, String mode, boolean saveMode) {
        super(parent, saveMode ? "保存进度" : "读取进度", ModalityType.APPLICATION_MODAL);
        this.saveMode = saveMode;
        this.gamePanel = gamePanel;
        this.username = username;
        this.mode = mode;

        setSize(560, 420);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ── Root panel ──
        JPanel root = new JPanel(new BorderLayout(0, ThemeColors.PAD_SECTION));
        root.setBackground(ThemeColors.CANVAS);
        root.setBorder(BorderFactory.createEmptyBorder(
            ThemeColors.PAD_CARD, ThemeColors.PAD_CARD,
            ThemeColors.PAD_SECTION, ThemeColors.PAD_CARD));
        setContentPane(root);

        // ── Title ──
        JLabel title = new JLabel(saveMode ? "保存进度" : "读取进度", SwingConstants.CENTER);
        title.setFont(ThemeColors.FONT_TITLE);
        title.setForeground(ThemeColors.PRIMARY);
        root.add(title, BorderLayout.NORTH);

        // ── Center: table ──
        String[] columns = {"存档位", "存档时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        slotTable = new JTable(tableModel);
        styleTable(slotTable);
        loadSlotData();

        JScrollPane scroll = new JScrollPane(slotTable);
        scroll.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ThemeColors.BORDER),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        scroll.getViewport().setBackground(ThemeColors.SURFACE);

        root.add(scroll, BorderLayout.CENTER);

        // ── Bottom: buttons ──
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, ThemeColors.PAD_BTN_GROUP, 0));
        bottom.setBackground(ThemeColors.CANVAS);

        if (saveMode) {
            btnAction = createRoundedButton("保存", ThemeColors.PRIMARY, ThemeColors.TEXT_ON_GOLD);
            btnAction.addActionListener(e -> doSave());
        } else {
            btnAction = createRoundedButton("读取", ThemeColors.ACCENT, ThemeColors.TEXT_ON_GOLD);
            btnAction.addActionListener(e -> doLoad());
        }

        btnDelete = createRoundedButton("删除", ThemeColors.DANGER, Color.WHITE);
        btnDelete.addActionListener(e -> doDelete());

        JButton btnCancel = createRoundedButton("取消", ThemeColors.SURFACE_2, ThemeColors.TEXT);
        btnCancel.addActionListener(e -> dispose());

        bottom.add(btnAction);
        bottom.add(btnDelete);
        bottom.add(btnCancel);
        root.add(bottom, BorderLayout.SOUTH);

        // 初始无选中 → 按钮禁用
        btnAction.setEnabled(false);
        btnDelete.setEnabled(false);
        slotTable.getSelectionModel().addListSelectionListener(e -> {
            boolean sel = slotTable.getSelectedRow() >= 0;
            btnAction.setEnabled(sel);
            btnDelete.setEnabled(sel);
        });
    }

    // ═══════════════════════════════════════
    //  Table style
    // ═══════════════════════════════════════
    private void styleTable(JTable table) {
        table.setBackground(ThemeColors.SURFACE);
        table.setForeground(ThemeColors.TEXT);
        table.setSelectionBackground(ThemeColors.PRIMARY);
        table.setSelectionForeground(ThemeColors.TEXT_ON_GOLD);
        table.setFont(ThemeColors.FONT_BODY);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFocusable(false);
        table.setRowSelectionAllowed(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(ThemeColors.TEXT_ON_GOLD);
        header.setForeground(ThemeColors.PRIMARY);
        header.setFont(ThemeColors.FONT_H2);
        header.setPreferredSize(new Dimension(0, 32));
        header.setBorder(BorderFactory.createEmptyBorder());

        // 单元格样式：居中 + 交替行 + 空位"— 空 —"
        for (int i = 0; i < table.getColumnCount(); i++) {
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
                    // 空存档
                    if (value == null || value.toString().trim().isEmpty()) {
                        setText("— 空 —");
                        setForeground(isSelected ? ThemeColors.TEXT_ON_GOLD : ThemeColors.TEXT_MUTED);
                    }
                    return c;
                }
            });
        }
    }

    // ═══════════════════════════════════════
    //  Button factory
    // ═══════════════════════════════════════
    private JButton createRoundedButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
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
        btn.setFont(ThemeColors.FONT_BODY);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(100, 36));
        return btn;
    }

    // ═══════════════════════════════════════
    //  Data
    // ═══════════════════════════════════════
    private void loadSlotData() {
        tableModel.setRowCount(0);
        boolean[] filled = new boolean[SLOT_COUNT];

        // 尝试从 SaveManager 读取
        for (int slot = 1; slot <= SLOT_COUNT; slot++) {
            if (SaveManager.hasSave(username, mode, slot)) {
                filled[slot - 1] = true;
                File f = new File(SaveManager.getSaveFilePath(username, mode, slot));
                String time = LocalDateTime.ofEpochSecond(f.lastModified() / 1000, 0, ZoneOffset.ofHours(8))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd  HH:mm"));
                tableModel.addRow(new Object[]{"存档 " + slot, time});
            }
        }
        // 填充空位保持顺序
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!filled[i]) {
                tableModel.addRow(new Object[]{"存档 " + (i + 1), ""});
            }
        }
    }

    // ═══════════════════════════════════════
    //  Operations
    // ═══════════════════════════════════════
    private void doSave() {
        int row = slotTable.getSelectedRow();
        if (row < 0) return;
        int slot = row + 1;
        String time = (String) tableModel.getValueAt(row, 1);
        if (time != null && !time.isEmpty()) {
            int r = JOptionPane.showConfirmDialog(this,
                    "存档 " + slot + " 已有记录，确定覆盖？", "确认覆盖",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }
        boolean success = gamePanel.saveGame(slot);
        if (success) {
            JOptionPane.showMessageDialog(this, "保存成功！");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "保存失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doLoad() {
        int row = slotTable.getSelectedRow();
        if (row < 0) return;
        int slot = row + 1;
        String time = (String) tableModel.getValueAt(row, 1);
        if (time == null || time.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该存档位没有数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "读取存档 " + slot + " ？\n当前进度将会丢失。",
                "确认读取", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        String error = gamePanel.loadGame(slot);
        if (error == null) {
            JOptionPane.showMessageDialog(this, "读取成功！");
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, error, "读取失败", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void doDelete() {
        int row = slotTable.getSelectedRow();
        if (row < 0) return;
        int slot = row + 1;
        String time = (String) tableModel.getValueAt(row, 1);
        if (time == null || time.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该存档位没有数据", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this,
                "确定删除存档 " + slot + " ？\n此操作不可恢复。",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        boolean success = SaveManager.deleteSave(username, mode, slot);
        if (success) {
            loadSlotData();
            JOptionPane.showMessageDialog(this, "已删除存档 " + slot);
        } else {
            JOptionPane.showMessageDialog(this, "删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
