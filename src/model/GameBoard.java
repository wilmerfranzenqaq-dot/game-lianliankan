package model;

/**
 * 游戏棋盘 — 持有二维 Cell 数组并提供状态查询
 * 棋盘四周包含一圈空边框（索引 0 和 rowCnt-1/colCnt-1），
 * 用于路径查找时允许走"绕外圈"的间接连接
 */
public class GameBoard {

    // ── 棋盘元数据 ──

    int rowCnt;                // 总行数（含边框）
    int colCnt;                // 总列数（含边框）
    Cell[][] board;            // 二维格子数组
    public int totalPairs;            // 初始总配对数量

    public GameBoard(int rowCnt, int colCnt, Cell[][] board) {
        this.rowCnt = rowCnt;
        this.colCnt = colCnt;
        this.board = board;
        this.totalPairs = countNonEmptyInnerCells() / 2;
    }

    // ── 维度访问 ──

    public int getRowCnt() {
        return rowCnt;
    }

    public int getColCnt() {
        return colCnt;
    }

    // ── 格子访问 ──

    /** 获取指定位置的格子（含边框） */
    public Cell getCell(int row, int col) {
        return board[row][col];
    }

    /** 清除棋盘上所有格子的选中状态 */
    public void clearAllChosen() {
        for (int i = 0; i < rowCnt; i++) {
            for (int j = 0; j < colCnt; j++) {
                board[i][j].setChosen(false);
            }
        }
    }

    // ── 进度查询 ──

    /** 检查内部所有格子是否均已消除（游戏胜利条件） */
    public boolean isAllCleared() {
        for (int i = 1; i < rowCnt - 1; i++) {
            for (int j = 1; j < colCnt - 1; j++) {
                if (!board[i][j].isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /** 初始总配对数 */
    public int getTotalPairs() {
        return totalPairs;
    }

    /** 已消除的配对数 */
    public int getClearedPairs() {
        return totalPairs - countNonEmptyInnerCells() / 2;
    }

    /** 剩余未消除的配对数 */
    public int getRemainingPairs() {
        return countNonEmptyInnerCells() / 2;
    }

    // ── 内部工具 ──

    /** 统计内部（排除边框）非空格子数量 */
    private int countNonEmptyInnerCells() {
        int count = 0;
        for (int i = 1; i < rowCnt - 1; i++) {
            for (int j = 1; j < colCnt - 1; j++) {
                if (!board[i][j].isEmpty) {
                    count++;
                }
            }
        }
        return count;
    }
}
