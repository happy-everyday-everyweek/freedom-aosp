package com.freedom.installer.core;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;

import com.freedom.installer.InstallerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * 安装执行：直接使用框架 PackageInstaller session 链路（非桥）。
 * platform 签名 + INSTALL_PACKAGES 权限下可用完整 installFlags。
 */
public final class InstallRequest {

    // 与 AOSP PackageManager INSTALL_*（@hide）数值一致的私有定义
    public static final int INSTALL_REPLACE_EXISTING = 0x00000002;
    public static final int INSTALL_ALLOW_TEST = 0x00000004;
    public static final int INSTALL_ALL_USERS = 0x00000040;
    public static final int INSTALL_ALLOW_DOWNGRADE = 0x00000080;

    private InstallRequest() {
    }

    public static String execute(Context ctx, String apkPath, InstallOptions opts) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInstaller pi = pm.getPackageInstaller();

            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);

            int flags = 0;
            if (opts.keepData) flags |= INSTALL_REPLACE_EXISTING;
            if (opts.allUsers) flags |= INSTALL_ALL_USERS;
            if (opts.allowDowngrade) flags |= INSTALL_ALLOW_DOWNGRADE;
            if (opts.allowTest) flags |= INSTALL_ALLOW_TEST;
            if (flags != 0) params.setInstallFlags(flags);

            if (opts.grantAllRuntime) {
                Set<String> perms = collectRequestedPermissions(pm, apkPath);
                if (!perms.isEmpty()) {
                    params.setGrantedRuntimePermissions(perms);
                }
            }

            int sessionId = pi.createSession(params);
            PackageInstaller.Session session = pi.openSession(sessionId);
            try {
                File apk = new File(apkPath);
                try (OutputStream out = session.openWrite("base.apk", 0, apk.length());
                     InputStream in = new FileInputStream(apk)) {
                    byte[] buf = new byte[65536];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                }
                session.fsync(null);
            } finally {
                session.close();
            }

            Intent done = new Intent(ctx, InstallerActivity.class);
            PendingIntent sender = PendingIntent.getActivity(ctx, sessionId, done,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            pi.commit(sessionId, sender.getIntentSender());
            return "已提交安装";
        } catch (Exception e) {
            return "失败：" + e.getMessage();
        }
    }

    private static Set<String> collectRequestedPermissions(PackageManager pm, String apkPath) {
        Set<String> out = new HashSet<>();
        try {
            ApplicationInfo ai = pm.getApplicationArchiveInfo(apkPath, PackageManager.GET_PERMISSIONS);
            if (ai != null && ai.requestedPermissions != null) {
                for (String p : ai.requestedPermissions) {
                    out.add(p);
                }
            }
        } catch (Exception ignored) {
        }
        return out;
    }
}