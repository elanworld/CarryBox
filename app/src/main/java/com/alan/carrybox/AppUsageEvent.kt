package com.alan.carrybox

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginRunnerConditionEvent
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionSatisfied
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionUnsatisfied

// 输入定义类
@TaskerInputRoot
class AppUsageInput @JvmOverloads constructor(
    @field:TaskerInputField("input", labelResIdName = "input") var input: String? = null,
    @field:TaskerInputField("text", labelResIdName = "text") var text: Boolean = false
)

// 输出定义类
@TaskerOutputObject
class AppUsageOutput(
    @get:TaskerOutputVariable(
        "result",
        labelResIdName = "result label",
        htmlLabelResIdName = "result htmlLabel"
    ) var result: String?,
    @get:TaskerOutputVariable(
        "text",
        labelResIdName = "text label",
        htmlLabelResIdName = "text htmlLabel"
    ) var text: String?
)
@TaskerInputRoot
class AppUsageUpdate @JvmOverloads constructor(
    @field:TaskerInputField("app") var app: String? = null,
    @field:TaskerInputField("text") var text: String? = null
)

class AppUsageRunner : TaskerPluginRunnerConditionEvent<AppUsageInput, AppUsageOutput,AppUsageUpdate>() {
    companion object {
        // 静态变量
        var previousPackageName: String? = null
    }
    // 定义一个全局变量用于存储上一个包名
//    private  var previousPackageName: String? = null
    override fun getSatisfiedCondition(
        context: Context,
        input: TaskerInput<AppUsageInput>,
        update: AppUsageUpdate?
    ): TaskerPluginResultCondition<AppUsageOutput> {
        if (input.regular.text) {
            return TaskerPluginResultConditionSatisfied(context, AppUsageOutput(update?.app,update?.text))
        } else {
            // 检查当前包名是否与上一个包名不同
            if (update != null) {
                if (update.app != previousPackageName) {
                    // 当包名发生变化时触发事件
                    previousPackageName = update.app
                    return TaskerPluginResultConditionSatisfied(context, AppUsageOutput(update?.app,update?.text))
                }
            }
            return TaskerPluginResultConditionUnsatisfied()
        }
    }

}
class AppUsageEventHelper(config: TaskerPluginConfig<AppUsageInput>) : TaskerPluginConfigHelper<AppUsageInput, AppUsageOutput, AppUsageRunner>(config) {

    override val inputClass: Class<AppUsageInput> = AppUsageInput::class.java
    override val outputClass: Class<AppUsageOutput>  = AppUsageOutput::class.java
    override val runnerClass: Class<AppUsageRunner>  = AppUsageRunner::class.java
}


class ActivityConfigAppUsageEvent : Activity(), TaskerPluginConfig<AppUsageInput> {


    override val context get() = applicationContext
    override val inputForTasker: TaskerInput<AppUsageInput>
        get() = TaskerInput(AppUsageInput(""))
    var inputText :List<String>  = arrayListOf()

    override fun assignFromInput(input: TaskerInput<AppUsageInput>) {
        input.regular.input = "inputText[0]"
        input.regular.text = inputText[1] == "1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inputParams = arrayOf("用户名", "密码")  // 输入框的提示文字
        showInputDialog(this, inputParams) { results ->
            // 处理用户的输入结果
            results.forEach { result ->
                println("用户输入: $result")
            }
            inputText = results
            AppUsageEventHelper(this).finishForTasker()
        }
    }
}

fun Context.triggerAppUsageTaskerEvent(app: AppUsageUpdate) = ActivityConfigAppUsageEvent::class.java.requestQuery(this, app)
