package com.freedom.installer;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.freedom.installer.core.InstallOptions;
import com.freedom.installer.core.InstallRequest;

/**
 * 安装确认页：应用信息 + 高级功能菜单（默认展开，功能核心入口）。
 * UI 为极简原生 View 骨架，后续按 Material 3 Expressive 样式替换（参照 installerX 观感）。
 */
public class InstallConfirmActivity extends Activity {

    private String apkPath;
    private TextView appTitle;
    private Button install;
    private CheckBox silentBox, downgradeBox, keepDataBox, allUsersBox,
            testPackBox, grantPermsBox, asSystemBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        apkPath = getIntent().getStringExtra(InstallerActivity.EXTRA_APK_PATH);
        if (apkPath == null) {
            finish();
            return;
        }

        setContentView(buildUi());
    }

    private View buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 48, 48, 48);

        appTitle = new TextView(this);
        appTitle.setTextSize(20);
        appTitle.setText(describeApk());
        root.addView(appTitle);

        TextView tip = new TextView(this);
        tip.setText("高级功能（默认展开）：");
        tip.setTextSize(14);
        LinearLayout.LayoutParams tipLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tipLp.topMargin = 24;
        root.addView(tip, tipLp);

        silentBox = addOption(root, "静默安装（完成后无提示）");
        downgradeBox = addOption(root, "允许降级安装");
        keepDataBox = addOption(root, "保留数据重装（覆盖更新）");
        allUsersBox = addOption(root, "安装到全部用户");
        testPackBox = addOption(root, "允许测试包");
        grantPermsBox = addOption(root, "授予全部运行时权限");
        asSystemBox = addOption(root, "作为系统应用（特权模式）");

        install = new Button(this);
        install.setText("安装");
        install.setOnClickListener(v -> doInstall());
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        btnLp.topMargin = 32;
        root.addView(install, btnLp);

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        return sv;
    }

    private CheckBox addOption(LinearLayout root, String label) {
        CheckBox cb = new CheckBox(this);
        cb.setText(label);
        root.addView(cb);
        return cb;
    }

    private String describeApk() {
        String label = apkPath;
        PackageManager pm = getPackageManager();
        ApplicationInfo ai = pm.getApplicationArchiveInfo(apkPath, 0);
        if (ai != null) {
            CharSequence l = ai.loadLabel(pm);
            if (!TextUtils.isEmpty(l)) label = l.toString();
        }
        return "安装 " + label + "\n" + apkPath;
    }

    private void doInstall() {
        InstallOptions opts = new InstallOptions();
        opts.silent = silentBox.isChecked();
        opts.allowDowngrade = downgradeBox.isChecked();
        opts.keepData = keepDataBox.isChecked();
        opts.allUsers = allUsersBox.isChecked();
        opts.allowTest = testPackBox.isChecked();
        opts.grantAllRuntime = grantPermsBox.isChecked();
        opts.installAsSystem = asSystemBox.isChecked();

        new Thread(() -> {
            final String result = InstallRequest.execute(this, apkPath, opts);
            new Handler(Looper.getMainLooper()).post(() -> {
                appTitle.setText("安装结果：" + result);
                install.setEnabled(true);
            });
        }).start();
    }
}