package com.alan.carrybox

import android.content.Context


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
