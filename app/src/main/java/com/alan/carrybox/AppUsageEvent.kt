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

// 输入定义类
@TaskerInputRoot
class AppUsageInput @JvmOverloads constructor(
    @field:TaskerInputField("path", labelResIdName = "Path") var path: String? = null
)

// 输出定义类
@TaskerOutputObject
class AppUsageOutput(
    @get:TaskerOutputVariable(
        "result",
        labelResIdName = "result label",
        htmlLabelResIdName = "result htmlLabel"
    ) var result: String?
)
@TaskerInputRoot
class AppUsageUpdate @JvmOverloads constructor(@field:TaskerInputField("app") var app: String? = null)

class AppUsageRunner : TaskerPluginRunnerConditionEvent<AppUsageInput, AppUsageOutput,AppUsageUpdate>() {

    override fun getSatisfiedCondition(
        context: Context,
        input: TaskerInput<AppUsageInput>,
        update: AppUsageUpdate?
    ): TaskerPluginResultCondition<AppUsageOutput> {
        return TaskerPluginResultConditionSatisfied(context, AppUsageOutput(update?.app))
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
        get() = TaskerInput(AppUsageInput(readFromSharedPreferences(context, "path")))

    override fun assignFromInput(input: TaskerInput<AppUsageInput>) {
        input.regular.path?.let { saveToSharedPreferences(context, "path", it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppUsageEventHelper(this).finishForTasker()
    }
}

fun Context.triggerAppUsageTaskerEvent(app: AppUsageUpdate) = ActivityConfigAppUsageEvent::class.java.requestQuery(this, app)
