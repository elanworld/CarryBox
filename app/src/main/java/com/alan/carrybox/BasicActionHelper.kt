package com.alan.carrybox

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess

/**
 * Example of tasker plugin.
 */

// 配置
class SampleHelper(
    config: TaskerPluginConfig<SampleInput>
) : TaskerPluginConfigHelper<SampleInput, SampleOutput, SampleRunner>(config) {
    override val inputClass: Class<SampleInput> = SampleInput::class.java
    override val outputClass: Class<SampleOutput> = SampleOutput::class.java
    override val runnerClass: Class<SampleRunner> = SampleRunner::class.java
}

// 执行类
class SampleRunner : TaskerPluginRunnerAction<SampleInput, SampleOutput>() {
    override fun run(
        context: Context,
        input: TaskerInput<SampleInput>
    ): TaskerPluginResult<SampleOutput> {
        val value = input.regular.input1
        println("input1: $value")
        return TaskerPluginResultSucess(SampleOutput("path $value"))
    }
}

// 输入定义类
@TaskerInputRoot
class SampleInput @JvmOverloads constructor(
    @field:TaskerInputField("input1", labelResIdName = "input1 label") var input1: String? = null
)

// 输出定义类
@TaskerOutputObject
class SampleOutput(
    @get:TaskerOutputVariable(
        "result",
        labelResIdName = "result label",
        htmlLabelResIdName = "result htmlLabel"
    ) var result: String?
)

// 编辑界面类
class ActivityConfigBasicAction : Activity(), TaskerPluginConfig<SampleInput> {
    private lateinit var editText: EditText
    private lateinit var taskerHelper: SampleHelper

    override val context: Context
        get() = applicationContext

    override val inputForTasker: TaskerInput<SampleInput>
        get() = TaskerInput(SampleInput(readFromSharedPreferences(context,"input1")))

    override fun assignFromInput(input: TaskerInput<SampleInput>) {
        input.regular.input1?.let { saveToSharedPreferences(context,"input1", it) }
        editText.setText(input.regular.input1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editText = EditText(this)
        editText.setText(readFromSharedPreferences(this,"input1"))
        taskerHelper = SampleHelper(this)

        // 创建一个对话框，并将 EditText 控件添加到对话框中
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Enter Text")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                // 当用户点击确定按钮时，将输入的文本返回给 Tasker
                val text = editText.text.toString()
                saveToSharedPreferences(context,"input1",text)
                taskerHelper.finishForTasker()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // 当用户点击取消按钮时，关闭对话框
                dialog.dismiss()
                taskerHelper.finishForTasker()
            }
            .create()

        // 显示对话框
        alertDialog.show()
    }
}

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