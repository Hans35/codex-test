# 小游戏页面与 Java 拼图

这个仓库包含一个 Vite/Vue 小项目、几个独立 HTML 小游戏页面，以及一个 Java Swing 照片滑动拼图小游戏。

## 环境要求

- Node.js：用于运行 Vite/Vue 项目
- npm：用于安装依赖和执行前端脚本
- JDK：用于编译和运行 `SlidingPuzzleGame.java`

## 项目内容

- `index.html`：Vite/Vue 入口页面
- `src/main.js`：Vue 应用入口
- `src/App.vue`：Vue 今日工作台页面
- `src/styles.css`：Vue 页面样式
- `gomoku.html`：经典五子棋
- `minesweeper.html`：经典扫雷
- `calculator.html`：经典计算器
- `SlidingPuzzleGame.java`：Java Swing 照片滑动拼图
- `AGENTS.md`：自动化助手和协作者维护规则
- `status.md`：仓库更新状态记录

## Vue 项目

安装依赖：

```bash
npm install
```

启动开发服务器：

```bash
npm run dev
```

构建生产版本：

```bash
npm run build
```

预览构建结果：

```bash
npm run preview
```

## 独立 HTML 小游戏

下面这些文件可以直接用浏览器打开：

```text
gomoku.html
minesweeper.html
calculator.html
```

## Java 拼图

`SlidingPuzzleGame.java` 是一个照片滑动拼图小游戏，支持：

- 九宫格、十六宫格、二十五宫格
- 选择本地照片
- 自动裁剪照片为正方形并拆分
- 去除右下角作为空位
- 通过合法滑动打乱，保证可以复原
- 鼠标点击和方向键移动
- 步数、计时、查看原图、重新打乱

编译并运行：

```bash
javac SlidingPuzzleGame.java
java SlidingPuzzleGame
```

如果没有选择照片，游戏会使用内置默认图片。

## 维护约定

- 每次更新代码、文档、配置或资源后，同步更新 `status.md`。
- 当功能、运行方式、依赖、入口文件或项目结构变化时，同步更新 `README.md`。
- Java 编译生成的 `*.class` 文件不提交，已在 `.gitignore` 中忽略。
