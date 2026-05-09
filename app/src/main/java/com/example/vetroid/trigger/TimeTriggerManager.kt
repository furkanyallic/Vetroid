package com.example.vetroid.trigger

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.vetroid.receiver.TimeTriggerReceiver
import java.util.Calendar

class TimeTriggerManager(private val context: Context) {

    companion object {
        const val TAG = "TimeTriggerManager"
    }

    fun canScheduleExactAlarms(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val canSchedule = alarmManager.canScheduleExactAlarms()
            Log.d(TAG, "Exact alarm izni kontrolü: $canSchedule")
            return canSchedule
        }
        return true
    }

    fun scheduleAlarm(
        triggerId: Long,
        dayOfWeek: Int,  // 1=Pazartesi, 7=Pazar
        hour: Int,
        minute: Int
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
            action = TimeTriggerReceiver.ACTION_TIME_TRIGGER
            putExtra(TimeTriggerReceiver.EXTRA_TRIGGER_ID, triggerId)
            putExtra(TimeTriggerReceiver.EXTRA_DAY_OF_WEEK, dayOfWeek)
        }

        val requestCode = generateRequestCode(triggerId, dayOfWeek)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Alarm zamanını hesapla
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // Geçmişe gitmişse bir sonraki haftaya ekle
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        try {
            // setExactAndAllowWhileIdle kullanarak exact alarm kur
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                    Log.d(TAG, "Exact Alarm kuruldu: Gün $dayOfWeek, Saat $hour:$minute, RequestCode: $requestCode, Time: ${calendar.timeInMillis}")
                } else {
                    Log.w(TAG, "Exact alarm izni yok, inexact alarm kullanılıyor")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(TAG, "Exact Alarm kuruldu: Gün $dayOfWeek, Saat $hour:$minute, RequestCode: $requestCode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Alarm kurma hatası: ${e.message}", e)
        }
    }

    fun cancelAlarm(triggerId: Long, dayOfWeek: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val requestCode = generateRequestCode(triggerId, dayOfWeek)

        val intent = Intent(context, TimeTriggerReceiver::class.java).apply {
            action = TimeTriggerReceiver.ACTION_TIME_TRIGGER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Alarm iptal edildi: RequestCode: $requestCode")
    }

    fun cancelAllAlarms(triggerId: Long) {
        // Tüm günler için alarmları iptal et
        for (day in 1..7) {
            cancelAlarm(triggerId, day)
        }
    }

    private fun generateRequestCode(triggerId: Long, dayOfWeek: Int): Int {
        // Benzersiz request code için triggerId ve günü birleştir
        return (triggerId * 10 + dayOfWeek).toInt()
    }
}