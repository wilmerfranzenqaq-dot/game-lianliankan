# 🀄 Hajimi Match 连连看

> 猫猫主题连连看 — 消除棋子喂猫，小奶猫长成大胖橘

---

## 快速开始

```bash
# Windows：双击 run.bat 即可运行
# 或命令行：
java -cp "out;resource" app.Main
```

依赖：JDK 17+

### 重新编译（修改源码后）

```bash
javac -encoding UTF-8 -d out -sourcepath src src/app/Main.java
```

---

## 操作说明

| 操作 | 说明 |
|------|------|
| `> START` | 开始游戏计时 |
| 点击棋盘 | 选第一个棋子 → 选第二个，相同且可连通则消除 |
| `RESTART` | 重置棋盘 |
| `SETTINGS` | 时间限制、难度（3~6核心大小）、音效开关 |
| `RANK` | 排行榜 |

**难度模式**：登录后选择
- 简单模式：两个独立 4×4 区域，5 种棋子
- 困难模式：连续 10×9 区域，12 种棋子

**侧栏猫猫**：消除棋子 → 喂鱼 → 猫猫从小奶猫 → 大橘 → 翻肚皮胖橘 🐱

---

## 项目结构

```
game-lianliankan/
├── src/
│   ├── app/
│   │   └── Main.java          # 启动器
│   ├── model/
│   │   ├── Cell.java          # 棋子单元
│   │   ├── ChessGenerator.java  # 简单/困难棋盘生成
│   │   ├── GameBoard.java     # 棋盘逻辑
│   │   ├── LeaderBoard.java   # 排行榜
│   │   ├── LeaderRecord.java  # 记录条目
│   │   ├── Line.java          # 路径线
│   │   ├── Position.java      # 坐标
│   │   └── Rectangle.java     # 矩形区
│   ├── ui/
│   │   ├── GameFrame.java     # 主窗口 (CardLayout)
│   │   ├── SplashPanel.java   # 钓鱼动画封面
│   │   ├── LoginPanel.java    # 登录/注册
│   │   ├── GamePanel.java     # 游戏容器 (棋盘+状态+控制+猫)
│   │   ├── BoardPanel.java    # 棋盘绘制 + 点击处理
│   │   ├── StatusPanel.java   # 状态栏 (分数/时间/用时)
│   │   ├── ControlPanel.java   # 控制栏 (START/RESTART/SETTINGS/RANK)
│   │   ├── CatPanel.java      # 像素猫侧栏
│   │   ├── SettingsDialog.java # 设置弹窗
│   │   ├── RoundedButton.java  # 圆角按钮组件
│   │   └── LeaderBoardPanel.java # 排行榜弹窗
│   └── utils/
│       ├── Utils.java         # 路径检测算法
│       └── MusicManager.java  # 背景音乐管理
├── resource/
│   ├── background.png         # 登录页背景
│   ├── board_bg.png           # 棋盘背景
│   ├── 0.png ~ 12.png         # 棋子素材
│   └── music/                 # WAV 背景音乐
│       ├── splash.wav
│       ├── login.wav
│       └── game.wav
├── user.txt                   # 用户数据
└── README.md
```

---

## 开发日志

### 2026-05-16 —— UI 大修 + 性能优化

#### 配色重做 (LoginPanel)
- 深棕底 → 暖白 `#f4f0e8`（与游戏面板统一）
- 输入框透明 → 暖白底 `#fcf9f2` + 灰边 `#e8ddd0`，placeholder 灰棕 `#9a9080`
- 按钮蓝/绿 → 金色 `#d4a04a`（登录）+ 暖灰 `#8a7a65`（注册）
- **教训**：不要猜配色，从游戏自身色系出发

#### 圆角按钮 (RoundedButton)
- 新增 `RoundedButton` 组件：纯 Graphics2D 绘制，12px 弧角
- 带 hover 高亮（`bg.brighter()`）
- 替换 LoginPanel 按钮 + ControlPanel 全部 4 个按钮

#### Splash 交互修复
- 先切页再切歌：`parent.showPage("login")` → `MusicManager.play("login")`
- 鼠标移上去变手形

#### 状态栏对齐 (StatusPanel)
- 左边分数区从 `BorderLayout(NORTH+CENTER)` → `GridBagLayout + GridLayout(2,1)`
- 四列布局策略统一，分数/"剩余时间"/"已经用时"纵坐标严格对齐

#### 性能优化 (BoardPanel)
- 删掉空的 `GridLayout`（没有子组件，纯迷惑）
- 图片预缩放到格子大小（`Image.SCALE_FAST`），消除每帧 drawImage 的缩放开销
- 困难模式 90 个棋子每帧省掉 90 次实时缩放

#### 消除进度移入猫侧栏 (CatPanel)
- 从 StatusPanel 第四列移出「消除进度 + 剩余对数」
- 在猫下方增加：分隔线 → 消除进度百分比（大字金色）→ 剩余对数
- 使用 consumer callback 连接数据流：BoardPanel → GamePanel → CatPanel
- StatusPanel 精简为 3 列

---

### 2026-05-11 —— 架构重构：Hajimi Match

#### 从单页面到 CardLayout 多页面

**之前**:
```
Main.java(登录) → LoginFrame → JOptionPane选难度 → GameFrame(游戏)
```

**之后**:
```
Main.java(启动器)
  ↓
GameFrame (CardLayout)
  ├── SplashPanel    ← 钓鱼动画封面
  ├── LoginPanel     ← 登录/注册/猫名
  └── GamePanel      ← 游戏（棋盘+状态+控制）
```

**关键设计决策**：
- `add(panel, "name")` 注册，`showPage("name")` 切换
- Constructor 传 `this`（父引用模式），子面板回调父窗口
- `startGame(isHardMode)` 懒创建 GamePanel（登录后才确定难度）

#### SplashPanel 钓鱼动画
- Graphics2D 手绘：暖棕渐变、水面、鱼线浮标、绕圈游鱼
- 60fps Timer 驱动，鱼跳跃抛物线 + 水花粒子
- 标题淡入 + 呼吸闪烁

#### 棋子素材踩坑 3 次
1. ❌ Java 2D 像素画 → 丑
2. ❌ Emoji + 暖棕配色 → 区分度低
3. ❌ 手绘 12 种农场 SVG → 还是丑
4. ✅ Playwright 截图 HTML 页面棋子 → 效果一致，零成本

**教训：素材类工作不要自己画，从设计稿截图最保险**

#### 其他
- Settings 弹窗：时间 / 难度滑条 / 音效开关
- ControlPanel 当前时间/核心大小字段
- 修复登录页 placeholder focus listener 挂错组件

#### 技术点积累

| 技术 | 应用场景 |
|------|---------|
| CardLayout | 多页面切换，类似 Android Fragment |
| Playwright screenshot | 网页截取素材 |
| 父引用模式 | 子组件回调父窗口 |
| Timer 驱动动画 | 60fps 自定义绘制 |
| Graphics2D | 渐变、透明度、仿射变换、抗锯齿 |

---

### 2026-04-29 —— 项目初始化

- 项目位置：`D:\game-lianliankan`
- 技术栈：Java Swing
- GitHub：`yubailing666/game-lianliankan`
- 已做功能：
  - 登录/注册（文件存储）
  - 图片素材加载（PNG 棋子）
  - 倒计时（120s）
  - Start 按钮 + Start 前防误触
  - 加分系统
  - StatusPanel 布局
- 路径检测：0/1/2 折连通算法
- 已知 Bug：`Utils.java#findOneTurn` 中 `posB.getCol()` 应为 `posB.getRow()`

---

## 关键算法

### 路径检测

```java
canLinkAB(gameBoard, posA, posB)  // 入口
├── findZeroTurn()                // 同行或同列无障碍
├── findOneTurn()                 // 一个拐点
└── findTwoTurn()                 // 两个拐点，BFS 向四个方向扩展
```

### 棋盘生成

`ChessGenerator` 算法：
1. 确定有效位置（简单=两个 4×4 区块，困难=完整 10×9 区域）
2. 生成成对图标列表，shuffle
3. 填充到棋盘
4. 验证至少存在一个可消除对（有重试机制，最多 100 次）

---

## 已知问题

- [ ] `Utils.findOneTurn` 中 `posB.getCol()` → 应为 `posB.getRow()`（不影响功能？）
- [ ] 困难模式下路径检测 findTwoTurn 可能较慢（10×9 棋盘 BFS 需优化）
- [ ] 音效开关尚未真正实现（SoundCheckbox 一直 disabled）
- [ ] Settings 修改时间和难度后，新值未传入 GamePanel（仅存于 ControlPanel 字段）
- [ ] 猫猫鱼数不持久化（重启归零）
