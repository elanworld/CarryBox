package com.alan.carrybox

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.Toast
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
import java.io.File

/**
 * Example of a Tasker plugin to refresh media cache at specified path.
 */

// 配置
class RefreshMediaCacheHelper(config: TaskerPluginConfig<RefreshMediaCacheInput>) :
    TaskerPluginConfigHelper<RefreshMediaCacheInput, RefreshMediaCacheOutput, RefreshMediaCacheRunner>(config) {
    override val inputClass: Class<RefreshMediaCacheInput> = RefreshMediaCacheInput::class.java
    override val outputClass: Class<RefreshMediaCacheOutput> = RefreshMediaCacheOutput::class.java
    override val runnerClass: Class<RefreshMediaCacheRunner> = RefreshMediaCacheRunner::class.java
}

// 执行类
class RefreshMediaCacheRunner : TaskerPluginRunnerAction<RefreshMediaCacheInput, RefreshMediaCacheOutput>() {
    override fun run(
        context: Context,
        input: TaskerInput<RefreshMediaCacheInput>
    ): TaskerPluginResultSucess<RefreshMediaCacheOutput> {
        val path = input.regular.path ?: ""
        refreshMediaCacheAtPath(context, path)
        return TaskerPluginResultSucess(RefreshMediaCacheOutput("success"))
    }

    private fun refreshMediaCacheAtPath(context: Context, path: String) {
        // 创建一个 File 对象表示指定的路径
        val file = File(path)

        // 确保指定路径是一个目录，并且存在
        if (file.isDirectory && file.exists()) {
            // 获取目录下的所有文件
            val files = file.listFiles()

            // 如果文件列表不为空
            if (files != null && files.isNotEmpty()) {
                // 获取文件路径和 MIME 类型
                val paths = files.map { it.absolutePath }.toTypedArray()
                val mimeTypes = Array(files.size) { "image/jpeg" } // 以图片文件为例，如果有其他类型的文件，需要相应调整 MIME 类型

                // 扫描文件
                MediaScannerConnection.scanFile(context, paths, mimeTypes, null)
            } else {
                // 如果文件列表为空，提示用户
                showToast(context, "No media files found at path: $path")
            }
        } else {
            // 如果路径不是目录或者不存在，提示用户
            showToast(context, "Invalid directory: $path")
        }
    }


    private fun showToast(context: Context, message: String) {
        Handler(Looper.getMainLooper()).post { Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
    }
}

// 输入定义类
@TaskerInputRoot
class RefreshMediaCacheInput @JvmOverloads constructor(
    @field:TaskerInputField("path", labelResIdName = "Path") var path: String? = null
)

// 输出定义类
@TaskerOutputObject
class RefreshMediaCacheOutput(
    @get:TaskerOutputVariable(
        "result",
        labelResIdName = "result label",
        htmlLabelResIdName = "result htmlLabel"
    ) var result: String?
)

// 编辑界面类
class ActivityConfigRefreshMediaCacheAction : Activity(), TaskerPluginConfig<RefreshMediaCacheInput> {
    private lateinit var editText: EditText
    private lateinit var taskerHelper: RefreshMediaCacheHelper

    override val context: Context
        get() = applicationContext

    override val inputForTasker: TaskerInput<RefreshMediaCacheInput>
        get() = TaskerInput(RefreshMediaCacheInput(readFromSharedPreferences(context, "path")))

    override fun assignFromInput(input: TaskerInput<RefreshMediaCacheInput>) {
        input.regular.path?.let { saveToSharedPreferences(context, "path", it) }
        editText.setText(input.regular.path)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editText = EditText(this)
        editText.setText(readFromSharedPreferences(this, "path"))
        taskerHelper = RefreshMediaCacheHelper(this)

        // 创建一个对话框，并将 EditText 控件添加到对话框中
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Enter Path")
            .setView(editText)
            .setPositiveButton("OK") { _, _ ->
                // 当用户点击确定按钮时，将输入的文本返回给 Tasker
                val path = editText.text.toString()
                saveToSharedPreferences(context, "path", path)
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
