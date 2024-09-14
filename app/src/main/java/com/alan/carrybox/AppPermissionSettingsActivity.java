package com.alan.carrybox;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class AppPermissionSettingsActivity extends AppCompatActivity {

    String MANAGE_ALL_FILES_ACCESS_PERMISSION = "android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION";
    // 示例权限数组，包含权限名称和相应的权限请求代码
    private String[][] permissions = {
            {"相机", android.Manifest.permission.CAMERA},
            {"定位", android.Manifest.permission.ACCESS_FINE_LOCATION},
            {"存储", android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
            {"无障碍", android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE},
            {"使用记录权限", Settings.ACTION_USAGE_ACCESS_SETTINGS},
            {"全部文件", MANAGE_ALL_FILES_ACCESS_PERMISSION}
            // 添加更多权限名称和权限请求代码
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 创建一个线性布局作为按钮的容器
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        // 动态创建按钮
        for (String[] permission : permissions) {
            Button button = new Button(this);
            button.setText("获取" + permission[0] + "权限");
            button.setTag(permission[1]); // 将权限请求代码作为按钮的 tag，用于点击事件处理

            // 设置按钮点击事件
            button.setOnClickListener(v -> {
                String perm = (String) v.getTag(); // 获取按钮的 tag，即权限请求代码
                if (filterAccessibility(perm)) {
                    return;
                }
                if (filterUsage(perm)) {
                    return;
                }
                if (filterAllFile(perm)) {
                    return;
                }
                ActivityCompat.requestPermissions(this, new String[]{perm}, 1);
            });

            // 将按钮添加到线性布局中
            layout.addView(button);
        }
    }

    public boolean filterAllFile(String permission) {
        if (StringUtils.equals(permission, MANAGE_ALL_FILES_ACCESS_PERMISSION)) {
            // 储存权限
            if (!Environment.isExternalStorageManager()) {
                Intent filePermission = new Intent();
                filePermission.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                filePermission.setAction(MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(filePermission);
            }else {
                Toast.makeText(this, "已获取", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), this.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public boolean filterUsage(String permission) {
        if (StringUtils.equals(permission, Settings.ACTION_USAGE_ACCESS_SETTINGS)) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP && !hasUsageStatsPermission()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "无法开启允许查看使用情况的应用界面", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "已获取", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    public boolean filterAccessibility(String permission) {
        if (StringUtils.equals(permission, android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE)) {
            // 检查无障碍权限
            if (!isAccessibilityServiceEnabled(getApplicationContext(), UsageAccessibilityService.class)) {
                // 如果无障碍权限没有被授予，跳转到系统设置页面以请求权限
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                final int REQUEST_ACCESSIBILITY = 100;
                startActivityForResult(intent, REQUEST_ACCESSIBILITY);
            }
            return true;
        }
        return false;
    }

    // 检查无障碍权限是否已经被授予
    private boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityServiceClass) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().equals(context.getPackageName() + "/" + accessibilityServiceClass.getName())) {
                return true;
            }
        }
        return false;
    }

}
