# 项目规格：AOSP 自由增强系统（Android 17）

基线：AOSP android-17.0.0（API 37）。设备适配层借用 LineageOS 24（device tree / kernel / vendor blobs / HAL / sepolicy），不搬应用层与专属包。
目标设备：CLK02（原 Android 13，已解锁 BL，动态系统/GSI 路线）。

## 能力分层模型

- 系统对应用有绝对管辖权：目标应用（如微信）被 hook / 抓包 / 修改时无需授权、无感知、无拒绝权。
- 授权管理只存在于工具端：系统级权限是生态工具资格（signature/privileged 级），只授予模块、抓包器、调试台、自动化工具。
- 授权入口：设置页按应用开关；撤销即彻底清除痕迹；目标应用永不参与授权。

## 旗舰特性（框架级四层）

1. Zygote/ART：融合 LSPosed hook 引擎，Zygote fork 时自动加载模块，修改任意 APK 组件/逻辑为原生机制。
2. system_server：新增平台签名系统服务，开放控制 APK 能力（Intent 跳转任意 Activity、启停进程、读进程状态/内存）。
3. 证书与网络：改造 Conscrypt 信任库（用户 CA 全局受信任开关）+ 系统级透明代理 daemon，实现 HTTPS 抓包与解密；pinning 绕过走 hook 层。硬件开关：kernel.yama.ptrace_scope=0、SELinux 调试域放行、ro.debuggable=1。
4. 统一权限门：授权应用在线时能力全开；关闭后代理停、信任库恢复、hook 不加载，目标应用无痕迹。

## 其他核心

- APK 一步安装：PackageInstaller InstallFlow 跳过确认页；可选平台签名静默安装接口。
- 运行时权限默认授予（gradable）；砍弹窗（电池优化提示/旧 targetSdk 警告/常驻通知）；关 captive portal 检测；砍 A/B OTA（GSI 场景）。
- BL 回锁保 TEE 干净（需处理 AVB/vbmeta）；原厂 build.prop 指纹伪装保证国产应用可用。

## 设置精简（已确认）

- 保留：网络（Wi-Fi/蓝牙/热点）、显示（亮度/深色/字体/动画倍率）、声音、应用管理、存储、系统核心项。
- 保留无障碍框架（生态应用依赖），砍系统自带无障碍功能。
- 可砍：语言（默认 locale 属性固定中文）、日期（默认自动时间时区）、关于（可留极简版）、多用户、隐私冗余、冷门默认项。
- 自定义入口：系统级权限管理页、模块管理、root 授权、一步安装、默认授权、抓包与代理开关。
- 手法：RRO overlay 隐藏 tile，个别页面源码级移除。

## 构建与 CI

- 源码：浅克隆（repo init --depth=1），官方全量要求 400GB（250 检出 + 150 构建）与 64GB RAM，浅克隆 + 单设备单 ABI GSI 预计 100-150GB（待实测）。
- CI：GitHub Actions；轻量校验 job 用 ubuntu-latest，AOSP 全量构建 job 用 larger runner（或自托管），构建产物走 artifacts。
- 兼容红线（不砍）：WebView、PermissionController、DocumentsUI、StorageManager、framework-res/SystemUI/Launcher3。
- 精简手法：产品 makefile allowlist，不物理删源码。