# AGENTS.md

本文件给后续自动化助手或协作者使用。修改本仓库时，请遵守下面规则。

## 自动维护规则

1. 每次更新代码、文档、配置或资源后，都要同步更新 `status.md`。
   - 记录日期、改动摘要、涉及文件、验证方式。
   - 如果没有运行验证，也要写明原因。

2. 当功能、运行方式、依赖、入口文件或项目结构发生变化时，要自动更新 `README.md`。
   - README 应保持面向使用者，说明项目包含什么、如何运行、关键文件在哪里。
   - 不要把过长的实现细节放进 README；实现细节可以写在代码注释或其他文档里。

## 项目说明

- 当前前端部分是 Vite/Vue 项目，同时保留了几个独立 HTML 小游戏页面。
- Java 拼图是独立 Swing 程序，源码为 `SlidingPuzzleGame.java`。
- 编译 Java 会生成 `.class` 文件，已经通过 `.gitignore` 忽略，不要提交编译产物。

## 常用验证

- Java 拼图：

```bash
javac SlidingPuzzleGame.java
java SlidingPuzzleGame
```

- Vue 项目：

```bash
npm run build
```
