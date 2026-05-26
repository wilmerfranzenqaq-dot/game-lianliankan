package model;

/**
 * 棋盘上的一个格子 — 存储位置、图标、是否为空、是否被选中
 * 负责维护单个格子的渲染状态和交互状态
 */
public class Cell {

    // ── 状态字段 ──

    Position pos;          // 格子在棋盘中的坐标
    boolean isEmpty;       // 是否为空（已被消除或边框占位）
    int iconIndex;         // 图标编号，对应 resource/{iconIndex}.png；0 = 空/边框
    boolean isChosen;      // 是否被玩家选中（高亮显示）

    public Cell(Position pos, boolean isEmpty, int iconIndex) {
        this.pos = pos;
        this.isEmpty = isEmpty;
        this.iconIndex = iconIndex;
    }

    // ── 访问器 ──

    public Position getPos() {
        return pos;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public int getIconIndex() {
        return iconIndex;
    }

    public boolean getIsChosen() {
        return isChosen;
    }

    // ── 修改器 ──

    /** 设置选中高亮状态 */
    public void setChosen(boolean chosen) {
        isChosen = chosen;
    }

    /** 置为空格子（消除时调用），同时将图标设为 0 */
    public void setEmpty(boolean empty) {
        isEmpty = empty;
        iconIndex = 0;
    }

    public void setIconIndex(int iconIndex) {
        this.iconIndex = iconIndex;
    }
}
