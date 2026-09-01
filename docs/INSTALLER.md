# 安装子系统规格：Freedom Installer

状态：设计定稿，待源码落地。参考项目：InstallerX Revived（wxxsfxyzm/InstallerX-Revived，功能与 UI 参照）；底层完全自研，不做桥。

## 定位

系统底层的应用安装子系统。UI 只保留安装器本体与"高级功能菜单"（默认展开，这是功能核心入口）；不引入 InstallerX 的配置中心、网络开关、授权配置等多余页面。所有安装能力由系统底层原生提供，不依赖 Root / Shizuku / Dhizuku / app_process / shell 中转的桥接实现。

## 功能集合（安装状态）

- 静默安装：无 UI 直接安装，完成后通知或直接打开。
- 允许降级（INSTALL_ALLOW_DOWNGRADE）。
- 保留数据重装（REPLACE_EXISTING + 不杀进程保留数据）。
- 全部用户安装（INSTALL_ALL_USERS）。
- 允许测试包（INSTALL_ALLOW_TEST）。
- 授予全部运行时权限（安装时直接 grant 目标 runtime 权限）。
- 作为系统应用安装（两档）：
  - 真系统应用：APK 写入 /system/priv-app，挂载更新，重启生效（配合已解锁 BL / 可写 system）。
  - 特权应用：普通安装 + 由系统级权限门授予 privapp 特权，立即生效、可撤销。

## UI 设计

完全参考 installerX 的 Material 3 Expressive 风格（以及其表单/对话框布局），只保留：

- 安装确认界面：应用图标、名称、版本、权限摘要、包信息。
- 高级功能菜单：默认展开，展示全部安装选项（上述功能集合），用户按需勾选，确认后一次安装完成。
- 安装进度与结果反馈（成功/失败原因）。
- 不包含：设置页、网络开关、授权配置入口、主题切换中心等非安装器页面。

## 底层实现（非桥）

1. system_server 新增平台签名安装服务（FreedomInstallService）：
   - 直接调用 PackageManagerService 内部安装链路（installStage / installPackageAsUser + 完整 installFlags），不做 pm shell 中转。
   - 暴露能力：上述所有安装状态、批量安装、全部用户、系统应用两档。
2. 权限门：服务接口要求调用方持有"系统级权限"（signature/privileged 级，见 SPEC 能力分层），未授权应用无法调用；目标应用全程无感知。
3. 无额外 daemon、无 shizuku server、无 su 进程。

## 上游现状与自研决策（2026-09-01 探明）

- AOSP master 与 android-latest-release 的 manifest 中均无 packageinstaller 项目；packages/apps/PackageInstaller 仓库当前承载的是 PermissionController 应用。
- 结论：Android 16/17 已把安装确认 UI 移出独立安装器 app，默认树无 com.android.packageinstaller。
- 因此 Freedom Installer 完全自研，新建应用（com.freedom.installer，platform 签名 + privileged），不依赖上游安装器源码；安装流程直接对接 framework 内部安装 API（Android 17 的 PackageManager install 链路）。
- 参考物：InstallerX Revived 仅作 UI（Material 3 Expressive）与功能清单参考；AOSP PermissionController / Settings 中残留的安装 UI 组件可在适配期参考。

## 落地文件（AOSP 侧待改清单，随源码到位逐步确认）

- freedom/installer（新建系统应用）：Android.bp、AndroidManifest.xml、UI（高级菜单默认展开）、安装选项透传、系统服务对接。
- frameworks/base（services/core/java/com/android/server/pm）：新增安装特权服务注册与 installFlags 支持、系统应用两档实现。
- frameworks/base（core/res）：新增"系统级权限"定义与安装子系统的权限声明。
- 产品配置：freedom/installer 作为核心组件保留（不在精简剔除清单）。

## 与 installerX 差异

- installerX：桥（Root/Shizuku/Dhizuku）+ 配置页 + shell pm 中转；依赖系统已有授权服务。
- Freedom Installer：平台原生服务 + 无配置页 + 高级菜单默认开 + 直接 API；系统对应用绝对管辖，目标 app 无需授权。