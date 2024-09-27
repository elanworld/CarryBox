package com.alan.carrybox

import android.app.usage.UsageEvents
import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.app.usage.UsageStatsManager
import android.os.Build
import androidx.annotation.RequiresApi

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun queryHourlyUsage(context: Context): ArrayList<AppUsage> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // 获取今天 00:00 到当前时间的事件
        val startCalendar = Calendar.getInstance()
        startCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startCalendar.set(Calendar.MINUTE, 0)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)
        val startTime = startCalendar.timeInMillis
        val endTime = System.currentTimeMillis()

        // 查询应用的使用事件
        val events = usageStatsManager.queryEvents(startTime, endTime)

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val eventMap = mutableMapOf<String, MutableList<Pair<Long, Long>>>()

        var event: UsageEvents.Event? = UsageEvents.Event()
        var lastForegroundTime = 0L
        var lastPackageName = ""


        while (events.hasNextEvent()) {
            events.getNextEvent(event)

            when (event?.eventType) {
                UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                    // 记录当前应用切换到前台的时间和包名
                    lastForegroundTime = event.timeStamp
                    lastPackageName = event.packageName
                }
                UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                    // 判断包名是否一致，确保是同一个应用
                    if (lastForegroundTime > 0 && event.packageName == lastPackageName) {
                        val usageDuration = event.timeStamp - lastForegroundTime
                        // 按包名存储前台使用时长
                        eventMap.getOrPut(lastPackageName) { mutableListOf() }
                            .add(Pair(lastForegroundTime, usageDuration))
                    }
                    // 重置前台时间
                    lastForegroundTime = 0L
                    lastPackageName = ""
                }
            }
        }
        val appUsageArrayList = arrayListOf<AppUsage>()
        // 输出每个小时的使用时长
        eventMap.forEach { (packageName, usageList) ->
            val hourlyUsage = mutableMapOf<Int, Long>()
            usageList.forEach { (startTime, duration) ->
                val calendar = Calendar.getInstance().apply { timeInMillis = startTime }
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                hourlyUsage[hour] = (hourlyUsage[hour] ?: 0L) + duration
            }

            val element = AppUsage()
            Log.e("UsageStats", "应用: $packageName")
            hourlyUsage.forEach { (hour, duration) ->
                Log.e("UsageStats", "小时: $hour, 使用时长: ${duration / 1000} 秒")
                // 动态设置属性，通过反射访问
                val propertyName = "subtime$hour"
                val field = element::class.java.getDeclaredField(propertyName)
                field.isAccessible = true
                field.set(element, duration.toString())
            }
            element.date = startTime.toString()
            element.packagename = packageName
            element.totalforegroundtime = usageList.sumOf { it.second }.toString()
            appUsageArrayList.add(element)
        }
        return appUsageArrayList
    }

