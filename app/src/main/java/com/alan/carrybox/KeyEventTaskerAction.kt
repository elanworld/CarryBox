package com.alan.carrybox

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess


/**
 * tools of a Tasker plugin to query KeyEvent.
 */

// 输入定义类
@TaskerInputRoot
class KeyEventTaskerInput @JvmOverloads constructor(
    @field:TaskerInputField("input", labelResIdName = "input") var input: String? = null
)

// 输出定义类
@TaskerOutputObject
class KeyEventTaskerOutput(
    @get:TaskerOutputVariable(
        "result",
        labelResIdName = "result label",
        htmlLabelResIdName = "result htmlLabel"
    ) var result: String?
)

// 配置
class KeyEventTaskerHelper(config: TaskerPluginConfig<KeyEventTaskerInput>) :
    TaskerPluginConfigHelper<KeyEventTaskerInput, KeyEventTaskerOutput, KeyEventTaskerRunner>(config) {
    override val inputClass: Class<KeyEventTaskerInput> = KeyEventTaskerInput::class.java
    override val outputClass: Class<KeyEventTaskerOutput> = KeyEventTaskerOutput::class.java
    override val runnerClass: Class<KeyEventTaskerRunner> = KeyEventTaskerRunner::class.java
}

// 执行类
class KeyEventTaskerRunner : TaskerPluginRunnerAction<KeyEventTaskerInput, KeyEventTaskerOutput>() {
    private var serviceMessenger: Messenger? = null
    private var isBound = false

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            serviceMessenger = Messenger(service)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceMessenger = null
            isBound = false
        }
    }


    override fun run(
        context: Context,
        input: TaskerInput<KeyEventTaskerInput>
    ): TaskerPluginResultSucess<KeyEventTaskerOutput> {
        // 启动无障碍服务
        val serviceIntent = Intent(context, KeyEventRunAccessibilityService::class.java)
        input.regular.input?.let { serviceIntent.putExtra("key_code", it.toInt()) }
        context.startService(serviceIntent)
        return TaskerPluginResultSucess()

    }
}

// 编辑界面类
class ActivityConfigKeyEventTaskerAction : Activity(), TaskerPluginConfig<KeyEventTaskerInput> {

    override val context: Context
        get() = applicationContext
    private lateinit var taskerHelper: KeyEventTaskerHelper

    override val inputForTasker: TaskerInput<KeyEventTaskerInput>
        get() = TaskerInput(KeyEventTaskerInput(inputText[0]))
    var inputText: List<String> = arrayListOf()

    override fun assignFromInput(input: TaskerInput<KeyEventTaskerInput>) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerHelper = KeyEventTaskerHelper(this)
        val inputParams = arrayOf("按键code")  // 输入框的提示文字
        showInputDialog(this, inputParams) { results ->
            // 处理用户的输入结果
            results.forEach { result ->
                println("用户输入: $result")
            }
            inputText = results
            KeyEventTaskerHelper(this).finishForTasker()
        }
    }

}
