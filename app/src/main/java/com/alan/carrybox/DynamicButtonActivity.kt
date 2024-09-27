package com.alan.carrybox

import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import kotlin.random.Random

class DynamicButtonActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建一个线性布局
        val linearLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // 创建一个 TextView 来显示结果
        val resultTextView = TextView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = "结果将显示在这里"
            setTextIsSelectable(true)
        }

        // 动态创建多个按钮并添加到布局中
        val buttonNames = listOf("使用情况", "随机数")
        val buttonActions = listOf(this::action1, this::action2)

        buttonNames.forEachIndexed { index, name ->
            val button = Button(this).apply {
                text = name
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setOnClickListener {
                    // 调用对应的方法，并将结果显示在 TextView 中
                    val result = buttonActions[index]()
                    resultTextView.text = result
                }
            }
            // 将按钮添加到布局中
            linearLayout.addView(button)
        }

        // 将 TextView 添加到布局中
        linearLayout.addView(resultTextView)

        // 设置 Activity 的内容视图为创建的线性布局
        setContentView(linearLayout)
    }

    // 定义点击按钮后要执行的方法
    private fun action1(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usageArrayList = queryHourlyUsage(this)
            // 将使用数据转换为 JSON 字符串
            val gson = Gson()
            val jsonResult = gson.toJson(usageArrayList)

            // 返回 JSON 字符串
            return jsonResult
        }
        return "小于安卓6"
    }

    private fun action2(): String {
        return Random.nextDouble().toString()
    }
}
