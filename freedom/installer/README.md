# FreedomInstaller（com.freedom.installer）

系统底层应用安装子系统（自研）。platform 签名 + privileged，直接对接 framework 安装链路，无任何桥接（不依赖 Root/Shizuku/Dhizuku）。

## UI 原则（参考 InstallerX Revived，仅 UI 与功能清单）

- 只保留安装器本体：安装确认、高级功能菜单、进度与结果。
- 高级功能菜单默认展开（功能核心入口）。
- 不引入配置中心/网络开关/授权配置等非安装器页面。
- 风格基线：Material 3 Expressive（AOSP 内建组件优先，保持 Pixel 风格观感）。

## 安装状态集合

静默安装 | 允许降级 | 保留数据重装 | 全部用户 | 测试包 | 授予全部运行时权限 | 作为系统应用（真系统/特权两档）。

## 代码布局（待实现逐步填充）

- InstallerActivity：安装入口（接收 APK/AXPK/批量文件）。
- InstallConfirmActivity：确认页 + 高级功能菜单（默认展开）。
- core/InstallRequest：安装参数模型（状态集合 -> PackageManager installFlags 映射）。
- core/FreedomInstallService：平台安装服务对接层（或调用 system_server 特权服务）。

## 依赖上游

- 不携带 installerX 代码；Android 17 默认树已无 com.android.packageinstaller（见 docs/INSTALLER.md），本应用为全新建树。