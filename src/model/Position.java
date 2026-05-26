package model;

/**
 * 棋盘上的行列坐标 — 不可变值对象
 * 用于标识棋盘格子的位置，以及路径查找中的拐点
 */
public class Position {

    private int row;
    private int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    // ── 访问器 ──

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    // ── 相等比较（基于行列值） ──

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Position other = (Position) obj;
        return this.row == other.row && this.col == other.col;
    }
}
