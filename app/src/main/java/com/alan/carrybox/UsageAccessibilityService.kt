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
            var text:String? = null
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
                val textList = mutableListOf<String>()
                traverseNode(rootNode, textList)
                text = textList.joinToString(separator = " ")
            }
            applicationContext.triggerAppUsageTaskerEvent(AppUsageUpdate(packageName,text))
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

    private fun traverseNode(node: AccessibilityNodeInfo?, textList: MutableList<String>): MutableList<String> {
        if (node == null) {
            return textList
        }

        // 检查节点是否包含文本内容
        if (node.text != null) {
            val text = node.text.toString()
            // 将文本内容添加到列表
            textList.add(text)
        }

        // 递归遍历子节点
        for (i in 0 until node.childCount) {
            traverseNode(node.getChild(i), textList)
        }

        return textList
    }
}
