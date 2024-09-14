package com.alan.carrybox

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.google.gson.Gson
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


/**
 * tools of a Tasker plugin to query usage.
 */

// 输入定义类
@TaskerInputRoot
class UsageTaskerInput @JvmOverloads constructor(
    @field:TaskerInputField("input", labelResIdName = "input") var input: String? = null
)

// 输出定义类
@TaskerOutputObject
class UsageTaskerOutput(
    @get:TaskerOutputVariable(
        "result",
        labelResIdName = "result label",
        htmlLabelResIdName = "result htmlLabel"
    ) var result: String?
)

// 配置
class UsageTaskerHelper(config: TaskerPluginConfig<UsageTaskerInput>) :
    TaskerPluginConfigHelper<UsageTaskerInput, UsageTaskerOutput, UsageTaskerRunner>(config) {
    override val inputClass: Class<UsageTaskerInput> = UsageTaskerInput::class.java
    override val outputClass: Class<UsageTaskerOutput> = UsageTaskerOutput::class.java
    override val runnerClass: Class<UsageTaskerRunner> = UsageTaskerRunner::class.java
}

// 执行类
class UsageTaskerRunner : TaskerPluginRunnerAction<UsageTaskerInput, UsageTaskerOutput>() {
    private var usageStatsService: UsageStatsService? = null

    // ServiceConnection to interact with UsageStatsService
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? UsageStatsService.LocalBinder
            usageStatsService = binder?.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            usageStatsService = null
        }
    }

    override fun run(
        context: Context,
        input: TaskerInput<UsageTaskerInput>
    ): TaskerPluginResultSucess<UsageTaskerOutput> {

        // Bind to the service
        val intent = Intent(context, UsageStatsService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        try {
            // Wait for up to 5 seconds, checking every 100ms
            val maxWaitTimeMs = 5000 // Total wait time
            val intervalMs = 100 // Check every 100 milliseconds
            var waitedTime = 0

            while (usageStatsService == null && waitedTime < maxWaitTimeMs) {
                Thread.sleep(intervalMs.toLong())
                waitedTime += intervalMs
            }

            return if (usageStatsService != null) {
                // Service is connected, proceed with the operation
                val appUsageList = usageStatsService?.queryTodayUsage() ?: emptyList()
                val json = Gson().toJson(appUsageList)

                // Unbind the service
                context.unbindService(serviceConnection)

                TaskerPluginResultSucess(UsageTaskerOutput(json))
            } else {
                // Timeout occurred
                context.unbindService(serviceConnection)
                TaskerPluginResultSucess(UsageTaskerOutput("Service binding timed out"))
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            context.unbindService(serviceConnection)
            return TaskerPluginResultSucess(UsageTaskerOutput("Service binding interrupted"))
        }
    }
}

// 编辑界面类
class ActivityConfigUsageTaskerAction : Activity(), TaskerPluginConfig<UsageTaskerInput> {

    override val context: Context
        get() = applicationContext
    private lateinit var taskerHelper: UsageTaskerHelper

    override val inputForTasker: TaskerInput<UsageTaskerInput>
        get() = TaskerInput(UsageTaskerInput(""))

    override fun assignFromInput(input: TaskerInput<UsageTaskerInput>) {
        input.regular.input?.let { saveToSharedPreferences(context, "input", it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerHelper =  UsageTaskerHelper(this)
        taskerHelper.finishForTasker()
    }

}
