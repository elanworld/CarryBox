package com.alan.carrybox;

import android.app.AppOpsManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


/**
 * service
 *
 * @author wu xianNeng
 * @date 2023/10/19 16:47
 * @since JDK1.8
 */
public class UsageStatsService extends Service {

    private final IBinder binder = new LocalBinder();
    UsageStatsManager usageStatsManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // LocalBinder class to provide the service instance
    public class LocalBinder extends Binder {
        public UsageStatsService getService() {
            return UsageStatsService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.usageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public List<AppUsage> queryTodayUsage() {
        AtomicLong startTime = new AtomicLong();
        AtomicLong endTime = new AtomicLong();
        startTime.set(getDayTimeInMillis(null, true));
        endTime.set(getDayTimeInMillis(null, false));
        List<AppUsage> usageStatsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            usageStatsList = queryUsageStats(startTime.get(), endTime.get());
            return usageStatsList;
        }

        Toast.makeText(this, "版本不支持", Toast.LENGTH_SHORT).show();
        return usageStatsList;
    }


    private long getDayTimeInMillis(Long time, boolean dayStart) {
        Calendar calendar = Calendar.getInstance();
        if (time != null) {
            calendar.setTimeInMillis(time);
        }
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        if (!dayStart) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        return calendar.getTimeInMillis();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private List<AppUsage> queryUsageStats(long startTime, long endTime) {

        // 首先判断获取到的list是否为空 if (packageInfoList == null)
        if (!hasUsageStatsPermission()) {
            try {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "无法开启允许查看使用情况的应用界面", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return UsageStatsServiceKotlinKt.queryHourlyUsage(this);
        }
        List<UsageStats> usageStatsList = queryUsageStatsInRange(this, startTime, endTime);
        Map<String, AppUsage> appUsageMap = new HashMap<>();
        for (UsageStats usageStats : usageStatsList) {
            String packageName = usageStats.getPackageName();
            long totalTimeInForeground = usageStats.getTotalTimeInForeground();

            if (totalTimeInForeground > 0) {
                // Calculate the start of the day for the usageStats timestamp
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(usageStats.getLastTimeStamp());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long dateStartOfDay = calendar.getTimeInMillis();

                String key = dateStartOfDay + "_" + packageName;
                if (appUsageMap.containsKey(key)) {
                    AppUsage existingUsage = appUsageMap.get(key);
                    long updatedTime = Long.parseLong(existingUsage.getTotalforegroundtime()) + totalTimeInForeground;
                    existingUsage.setTotalforegroundtime(String.valueOf(updatedTime));
                } else {
                    AppUsage value = new AppUsage();
                    value.setPackagename(packageName);
                    value.setTotalforegroundtime(String.valueOf(totalTimeInForeground));
                    value.setDate(String.valueOf(dateStartOfDay));
                    appUsageMap.put(key, value);
                }
            }
        }
        return new ArrayList<>(appUsageMap.values());

    }

    public List<UsageStats> queryUsageStatsInRange(Context context, long startTime, long endTime) {
        List<UsageStats> usageStatsList = new ArrayList<>();
        do {
            // 查询使用统计数据
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
            }

            // 如果返回数据为空且结束时间不超过今天，则将结束时间增加一天
            if (usageStatsList.isEmpty() && endTime < System.currentTimeMillis()) {
                // 增加一天
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(endTime);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                endTime = calendar.getTimeInMillis();
            } else {
                // 如果返回数据不为空或者结束时间超过今天，则退出循环
                break;
            }
        } while (true);

        return usageStatsList;
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) this.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), this.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

}