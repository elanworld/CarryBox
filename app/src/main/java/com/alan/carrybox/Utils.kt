package com.alan.carrybox

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText
import android.widget.LinearLayout


// 写入数据到 SharedPreferences
fun saveToSharedPreferences(context: Context, key: String, value: String) {
    val sharedPreferences = context.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(key, value)
    editor.apply()
}

// 从 SharedPreferences 读取数据
fun readFromSharedPreferences(context: Context, key: String): String? {
    val sharedPreferences = context.getSharedPreferences("mySharedPreferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, null)
}

fun stringEquals(a:String?, b: String?) : Boolean {
    if (a == null || b==null)
        return false
    if (a.equals(b)) {
        return true
    }
    return false
}
fun showInputDialog(context: Context, inputParams: Array<String>, onResult: (List<String>) -> Unit) {
    // 创建 AlertDialog.Builder
    val builder = AlertDialog.Builder(context)
    builder.setTitle("请输入信息")

    // 动态创建输入框的容器
    val container = LinearLayout(context)
    container.orientation = LinearLayout.VERTICAL
    val inputFields = mutableListOf<EditText>()

    // 动态创建输入框
    inputParams.forEach { param ->
        val editText = EditText(context)
        editText.hint = "请输入 $param"  // 设置提示文字
        container.addView(editText)
        inputFields.add(editText)  // 将输入框保存到列表中
    }

    // 将容器添加到对话框中
    builder.setView(container)

    // 添加确认按钮
    builder.setPositiveButton("提交") { dialog, which ->
        val results = inputFields.map { it.text.toString() }  // 获取每个输入框的文本内容
        onResult(results)  // 通过回调函数返回输入结果
    }

    // 添加取消按钮
    builder.setNegativeButton("取消", null)

    // 显示对话框
    builder.show()
}
