# 探路结果：免费 runner 可行边界（2026-09-01，run 33498171234）

## 实测资源（ubuntu-24.04，免费 hosted runner）

- CPU：4 核（非文档所述 2 核）
- RAM：15Gi（非文档所述 7GB）
- 磁盘：145GB 总量，初始可用 86GB

结论：GitHub 免费 runner 的实际规格明显高于旧文档，是可行的构建环境。

## 同步实测（浅克隆 android-latest-release，--depth=1）

- repo init：成功，.repo 仅 19M（空 manifest）。
- build/make + build/soong + prebuilts/build-tools + prebuilts/clang/host/linux-x86：约 11 分钟，占 25GB。
- 追加 frameworks/base + frameworks/av：约 1 分钟，总占用 28GB，此时磁盘仍余 59GB。
- 注意：manifest 中不存在项目 build/bazel（报 project not found），Android 17 构建系统项目为 build/make、build/soong。

## 编译实测

- Soong bootstrap（framework-minus-apex，-j2，timeout 300s）步骤完成，但详细编译输出被日志截断，是否真正编译通过未确认，需在下一步探路中复测并抓全日志。

## 对切分方案的影响

- 免费 runner 足以承载"浅克隆局部源码 + 单模块编译"的构建单元；28GB 源码 + 产物仍贴近 59GB 余量，单 job 内的源码集不能无限扩大，模块切分按此边界设计。
- 单模块构建时只需同步该模块依赖的源码集（按 repo 项目列表局部同步），其余由其他子流水线负责，天然互不重叠。
- 4 核并行度：构建 job 内 -j 建议 4 左右，避免内存峰值超限。