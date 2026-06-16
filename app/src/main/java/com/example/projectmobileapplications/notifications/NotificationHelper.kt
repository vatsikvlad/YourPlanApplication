package com.example.projectmobileapplications.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.example.projectmobileapplications.phone.ScheduleItem
import java.util.*
import kotlin.math.roundToInt

class NotificationHelper(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleEventNotifications(item: ScheduleItem) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                return
            }
        }

        val calendars = getEventStartTimes(item)
        calendars.forEach { calendar ->
            scheduleAlarm(
                calendar.timeInMillis,
                item.id.hashCode(),
                item.title,
                "Your event \"${item.title}\" is starting now!"
            )

            val warningTime = calendar.timeInMillis - 10 * 60 * 1000
            if (warningTime > System.currentTimeMillis()) {
                scheduleAlarm(
                    warningTime,
                    item.id.hashCode() + 1,
                    item.title,
                    "Event \"${item.title}\" starts in 10 minutes"
                )
            }
        }
    }

    private fun scheduleAlarm(timeInMillis: Long, requestId: Int, title: String, message: String) {
        if (timeInMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notificationId", requestId)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelNotifications(item: ScheduleItem) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntentStart = PendingIntent.getBroadcast(
            context,
            item.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pendingIntentWarning = PendingIntent.getBroadcast(
            context,
            item.id.hashCode() + 1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntentStart)
        alarmManager.cancel(pendingIntentWarning)
    }

    private fun getEventStartTimes(item: ScheduleItem): List<Calendar> {
        val results = mutableListOf<Calendar>()
        val hour = item.startTime.toInt()
        val minute = ((item.startTime - hour) * 60).roundToInt()

        if (item.isRepeating) {
            item.daysOfWeek.forEach { dayIndex ->
                val cal = Calendar.getInstance()
                val targetDay = if (dayIndex == 6) Calendar.SUNDAY else dayIndex + 2
                
                cal.set(Calendar.DAY_OF_WEEK, targetDay)
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)

                if (cal.timeInMillis <= System.currentTimeMillis()) {
                    cal.add(Calendar.WEEK_OF_YEAR, 1)
                }
                results.add(cal)
            }
        } else {
            item.specificDate?.let { date ->
                val cal = date.clone() as Calendar
                cal.set(Calendar.HOUR_OF_DAY, hour)
                cal.set(Calendar.MINUTE, minute)
                cal.set(Calendar.SECOND, 0)
                if (cal.timeInMillis > System.currentTimeMillis()) {
                    results.add(cal)
                }
            }
        }
        return results
    }
}
