package com.alan.carrybox;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.KeyEvent;

public class KeyEventRunAccessibilityService extends AccessibilityService {


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = super.onStartCommand(intent, flags, startId);
        if (intent != null) {
            int keyCode = intent.getIntExtra("key_code", -1);
            Log.d("MyAccessibilityService", "Received key code: " + keyCode);
            // 在这里处理 keyCode
            if (keyCode != -1) {
                performGlobalAction(keyCode);
            }
        }
        return command;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 处理事件
    }

    @Override
    public void onInterrupt() {
        // 中断时的处理
    }

    // 模拟主页键
    private void simulateHomeKey() {
        performGlobalAction(GLOBAL_ACTION_HOME);
    }

    // 模拟返回键
    private void simulateBackKey() {
        performGlobalAction(GLOBAL_ACTION_BACK);
    }

    // 模拟菜单键
    private void simulateMenuKey() {
        // 无法直接模拟菜单键，可能需要其他方式
    }

    // 模拟上下左右确认键
    private void simulateDirectionalKey(int direction) {
        switch (direction) {
            case KeyEvent.KEYCODE_DPAD_UP:
                // 模拟向上
                performGlobalAction(GLOBAL_ACTION_DPAD_UP);
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                // 模拟向下
                performGlobalAction(GLOBAL_ACTION_DPAD_DOWN);
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                // 模拟向左（无障碍服务通常不支持）
                performGlobalAction(GLOBAL_ACTION_DPAD_LEFT);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // 模拟向右（无障碍服务通常不支持）
                performGlobalAction(GLOBAL_ACTION_DPAD_RIGHT);
                break;
            case KeyEvent.KEYCODE_ENTER:
                // 模拟确认
                performGlobalAction(GLOBAL_ACTION_DPAD_CENTER);
                break;
            default:
                break;
        }
    }
}
