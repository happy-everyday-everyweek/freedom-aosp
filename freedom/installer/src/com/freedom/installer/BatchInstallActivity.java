package com.freedom.installer;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;

/** 批量安装占位：多个 APK 一次提交（后续实现会话合并与逐包回调）。 */
public class BatchInstallActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<String> paths = getIntent().getStringArrayListExtra("apk_paths");
        TextView tv = new TextView(this);
        tv.setText("批量安装（待实现）：共 " + (paths == null ? 0 : paths.size()) + " 个包");
        tv.setPadding(48, 48, 48, 48);
        setContentView(tv);
    }
}