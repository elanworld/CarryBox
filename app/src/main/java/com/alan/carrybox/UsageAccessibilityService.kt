package com.alan.carrybox

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 页面标题获取
 */
class UsageAccessibilityService : AccessibilityService() {
    // 定义一个全局变量用于存储上一个包名
    private var previousPackageName: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // 检查事件类型，通常是 TYPE_WINDOW_STATE_CHANGED
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // 获取当前窗口的包名和页面标题
            val packageName = event.packageName?.toString() ?: return
            val pageTitle = event.text.toString()

            // 检查当前包名是否与上一个包名不同
            if (packageName != previousPackageName) {
                // 当包名发生变化时触发事件
                applicationContext.triggerAppUsageTaskerEvent(AppUsageUpdate(packageName))

                // 更新上一个包名
                previousPackageName = packageName
            }

            // 打印包名和页面标题
            println("$packageName $pageTitle")

            // 在此处保存页面标题数据，您可以选择使用 SharedPreferences、数据库等进行存储操作
            savePageTitle(packageName, pageTitle)

            if (event.source == null) {
                return
            }

            // 获取当前页面的根节点
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                // 遍历根节点下的所有子节点，提取文本信息
                traverseNode(rootNode)
            }
        }
    }

    override fun onInterrupt() {
        // 在服务中断时执行清理操作
    }

    private fun savePageTitle(packageName: String, pageTitle: String) {
        // 在这里实现保存页面标题数据的逻辑，例如使用 SharedPreferences
        val sharedPreferences = getSharedPreferences("pageTitleData", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(packageName, pageTitle)
        editor.apply()
    }

    private fun traverseNode(node: AccessibilityNodeInfo?) {
        if (node == null) {
            return
        }

        // 检查节点是否包含文本内容
        if (node.text != null) {
            val text = node.text.toString()
            // 处理文本内容，例如保存到本地或打印输出
            println("Text: $text")
        }

        // 递归遍历子节点
        for (i in 0 until node.childCount) {
            traverseNode(node.getChild(i))
        }
    }
}
