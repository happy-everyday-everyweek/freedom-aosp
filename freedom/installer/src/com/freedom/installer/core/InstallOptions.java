package com.freedom.installer.core;

/** 安装选项（高级功能菜单勾选状态）。 */
public class InstallOptions {
    public boolean silent;           // 静默安装
    public boolean allowDowngrade;   // 允许降级
    public boolean keepData;         // 保留数据重装（覆盖更新）
    public boolean allUsers;         // 安装到全部用户
    public boolean allowTest;        // 允许测试包
    public boolean grantAllRuntime;  // 授予全部运行时权限
    public boolean installAsSystem;  // 作为系统应用（特权模式；真系统模式后续对接系统分区）
}