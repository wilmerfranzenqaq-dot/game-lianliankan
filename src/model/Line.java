package model;

import java.util.List;

/**
 * 连接线 — 封装两个棋子之间的消除路径
 * 路径由一系列 Position 构成，对应 0 折、1 折或 2 折连接
 */
public class Line {

    List<Position> path;   // 路径上的节点序列

    public Line(List<Position> path) {
        this.path = path;
    }

    /** 获取路径节点列表（起点 → 拐点 → 终点） */
    public List<Position> getPath() {
        return path;
    }
}
