package com.freedom.installer;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 安装入口：接收文件管理器/浏览器发来的 APK（ACTION_VIEW / INSTALL_PACKAGE）。
 * 解析包信息后进入确认页（高级功能菜单默认展开）。
 */
public class InstallerActivity extends Activity {

    static final String EXTRA_APK_PATH = "com.freedom.installer.extra.APK_PATH";
    static final String EXTRA_SILENT = "com.freedom.installer.extra.SILENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        List<String> apkPaths = collectApks(intent);
        if (apkPaths.isEmpty()) {
            finish();
            return;
        }

        // 单包：解析信息 -> 确认页（高级菜单默认展开）
        if (apkPaths.size() == 1) {
            Intent confirm = new Intent(this, InstallConfirmActivity.class);
            confirm.putExtra(EXTRA_APK_PATH, apkPaths.get(0));
            startActivity(confirm);
            finish();
            return;
        }

        // 批量：走批量安装（后续实现 BatchInstallActivity）
        startActivity(new Intent(this, BatchInstallActivity.class)
                .putStringArrayListExtra("apk_paths", new ArrayList<>(apkPaths)));
        finish();
    }

    /** 从 Intent 汇总 APK 文件：支持单 URI 与 ClipData 多选。 */
    private List<String> collectApks(Intent intent) {
        List<String> paths = new ArrayList<>();
        Uri uri = intent.getData();
        if (uri != null) {
            String p = materialize(uri);
            if (p != null) paths.add(p);
        }
        ClipData clip = intent.getClipData();
        if (clip != null) {
            for (int i = 0; i < clip.getItemCount(); i++) {
                Uri u = clip.getItemAt(i).getUri();
                String p = materialize(u);
                if (p != null) paths.add(p);
            }
        }
        return paths;
    }

    /** 把 content:// 拉到缓存目录，返回本地路径；file:// 与绝对路径直接返回。 */
    private String materialize(Uri uri) {
        String scheme = uri.getScheme();
        if ("file".equals(scheme)) {
            return uri.getPath();
        }
        if ("content".equals(scheme)) {
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                String name = "pkg_" + System.currentTimeMillis() + ".apk";
                File out = new File(getCacheDir(), name);
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    byte[] buf = new byte[65536];
                    int n;
                    while ((n = in.read(buf)) > 0) fos.write(buf, 0, n);
                }
                return out.getAbsolutePath();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}