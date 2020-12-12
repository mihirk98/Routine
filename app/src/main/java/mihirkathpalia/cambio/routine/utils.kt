package mihirkathpalia.cambio.routine

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import mihirkathpalia.cambio.routine.ui.routineWidget.RoutineWidgetAlarm
import mihirkathpalia.cambio.routine.ui.routineWidget.RoutinesWidget
import mihirkathpalia.cambio.routine.work.WidgetSyncWorker
import java.util.*
import java.util.concurrent.TimeUnit

const val syncWork = "syncWorker"
const val routineWidgetAlarm = "routineWidgetAlarm"
const val sharedPrefFile = "routineSharedPrefFile"
const val themeSharedPref = "themeSharedPref"
const val syncWorkSharedPref = "syncWorkSharedPref"
const val refreshDelaySharedPref = "refreshDelaySharedPref"

fun updateWidgets(context: Context) {
    Log.i("Tag", "updateWidgets()")
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val appWidgetIds = appWidgetManager.getAppWidgetIds(
        ComponentName(context, RoutinesWidget::class.java)
    )
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.routinesList)
}

fun syncWork(context: Context) {
    Log.i("Tag", "syncWork()")

    updateWidgets(context)

    val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        sharedPrefFile,
        Context.MODE_PRIVATE
    )
    val delay = sharedPreferences.getLong(refreshDelaySharedPref, 0L)

    Log.i("Tag", "delay: $delay")

    scheduleWork(context, delay)

    scheduleAlarm(context, delay)
}

fun scheduleWork(context: Context, delay: Long) {

    val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()

    val syncWorkRequest = OneTimeWorkRequestBuilder<WidgetSyncWorker>()
        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
        .setConstraints(constraints)
        .addTag(syncWork)
        .setBackoffCriteria(
            BackoffPolicy.LINEAR,
            OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
            TimeUnit.MILLISECONDS
        )
        .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork(syncWork, ExistingWorkPolicy.REPLACE, syncWorkRequest)
}

fun scheduleAlarm(context: Context, delay: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, RoutineWidgetAlarm::class.java)
    intent.action = routineWidgetAlarm
    intent.putExtra(routineWidgetAlarm, "Routine Widget Sync Alarm")

    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)

    val alarmTimeAtUTC = System.currentTimeMillis() + delay

    when {
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M -> {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pendingIntent)
        }
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP -> {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pendingIntent)
        }
        else -> {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeAtUTC, pendingIntent)
        }
    }
}

fun formatTime(hour: Int, minute: Int): Double {
    return hour + (minute / 100.0)
}

fun setTime(time: Double): String {
    val timeString = time.toString()
    val indexOfDecimal = timeString.indexOf('.')

    var hour = timeString.subSequence(0, indexOfDecimal)
    if (hour.length == 1)
        hour = "0$hour"

    var minute = timeString.subSequence(indexOfDecimal + 1, timeString.length).toString()
    if (!minute.startsWith('0') && minute.length == 1)
        minute += "0"
    else if (minute.length == 1)
        minute = "0$minute"

    return "$hour:$minute"

}

fun setDuration(endTime: Double, nextStartTime: Double): String {
    val endTimeString = endTime.toString()
    val indexOfEnd = endTimeString.indexOf('.')

    val endHour = endTimeString.subSequence(0, indexOfEnd)
    var endMinute = endTimeString.subSequence(indexOfEnd + 1, endTimeString.length).toString()
    if (endMinute.startsWith('0'))
        endMinute = endMinute.subSequence(endMinute.length - 1, endMinute.length).toString()
    else if (endMinute.length == 1)
        endMinute += "0"

    val nextStartString = nextStartTime.toString()
    val indexOfNextStart = nextStartString.indexOf('.')

    val nextStartHour = nextStartString.subSequence(0, indexOfNextStart)
    var nextStartMinute =
        nextStartString.subSequence(indexOfNextStart + 1, nextStartString.length).toString()
    if (nextStartMinute.startsWith('0'))
        nextStartMinute =
            nextStartMinute.subSequence(nextStartMinute.length - 1, nextStartMinute.length)
                .toString()
    else if (nextStartMinute.length == 1)
        nextStartMinute += "0"

    val endCalendar = Calendar.getInstance()
    endCalendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(endHour.toString()))
    endCalendar.set(Calendar.MINUTE, Integer.valueOf(endMinute))

    val nextStartCalendar = Calendar.getInstance()
    nextStartCalendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(nextStartHour.toString()))
    nextStartCalendar.set(Calendar.MINUTE, Integer.valueOf(nextStartMinute))

    val diff: Long = nextStartCalendar.timeInMillis - endCalendar.timeInMillis
    val hours = diff / 3600000
    val mins = diff / 60000 - hours * 60

    return when {
        hours == 0L -> {
            "$mins mins"
        }
        mins == 0L -> {
            if (hours == 1L) {
                "$hours hr"
            } else {
                "$hours hrs"
            }
        }
        else -> {
            if (hours == 1L) {
                "$hours hr\n$mins mins"
            } else {
                "$hours hrs\n$mins mins"
            }
        }
    }
}

fun getMinute(minute: String): Int {
    val minuteInt = Integer.valueOf(minute)
    if (minuteInt in 1..9) {
        if(!minute.startsWith('0'))
            return minuteInt * 10
    }
    return minuteInt
}