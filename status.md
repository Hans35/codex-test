# status.md

本文件记录仓库每次重要更新的状态。

## 2026-07-07

### README 查漏补缺

- 补充 README 中缺少的环境要求：Node.js、npm、JDK。
- 补充 Vue 项目的 `npm run build` 和 `npm run preview` 命令。
- 补充独立 HTML 小游戏可以直接用浏览器打开的说明。
- 补充项目文件说明，包括 `src/main.js`、`src/App.vue`、`src/styles.css`、`AGENTS.md`、`status.md`。
- 补充 Java 拼图在未选择照片时会使用内置默认图片。
- 补充维护约定：每次更新同步 `status.md`，功能/运行方式变化同步 `README.md`。
- 验证：对照仓库根目录、`src` 文件列表、`package.json` 脚本和 `.gitignore` 完成文档检查。

### 新增 Java 照片滑动拼图

- 新增 `SlidingPuzzleGame.java`。
- 支持九宫格、十六宫格、二十五宫格。
- 支持选择本地照片、自动裁剪拆分、去除右下角空位、合法滑动打乱。
- 支持鼠标点击、方向键移动、步数、计时、查看原图、重新打乱。
- 内置默认图片，未选择本地照片时也可以直接开始游戏。
- 更新 `.gitignore`，忽略 Java 编译生成的 `*.class` 文件。
- 验证：已运行 `javac SlidingPuzzleGame.java`，编译通过。

### 新增仓库维护文档

- 新增 `AGENTS.md`，写入后续自动化助手需要遵守的维护规则。
- 新增 `status.md`，用于记录每次重要更新。
- 重写 `README.md`，修复原有乱码，并补充 Java 拼图和 Vue 项目运行方式。
- 验证：文档更新，无需编译验证。
