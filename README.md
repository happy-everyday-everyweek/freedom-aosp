# AOSP 自由增强系统（Android 17）

基于 AOSP android-17.0.0，融合 LineageOS 设备适配，构建一套"自由第一"的定制系统。
开发模式：本地只编辑少量文件 → push 到 GitHub → CI（GitHub Actions）完成源码同步与全部构建，绕开本地硬件限制。

## 核心原则

- 自由第一：能力开放 + 选择权。
- 能力分层：系统对应用有绝对管辖权；目标应用被操作时无需授权、无感知。
- 系统级权限：生态工具资格，只授予工具/模块，目标应用永不参与。

## 目录结构

- docs/SPEC.md 项目规格
- scripts/ 构建与校验脚本（单元测试驱动，见 tests/）
- tests/ 脚本单元测试（TDD 红绿循环）
- .github/workflows/ CI 工作流

## 开发流程（TDD）

1. 在 tests/ 写失败测试（红色）。
2. 在 scripts/ 写最小实现令测试通过（绿色）。
3. push 到 GitHub，CI 同步 AOSP 源码并构建，构建结束跑同一套校验。