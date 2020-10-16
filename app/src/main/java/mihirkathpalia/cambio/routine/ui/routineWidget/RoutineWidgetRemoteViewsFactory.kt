package mihirkathpalia.cambio.routine.ui.routineWidget

import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import mihirkathpalia.cambio.routine.*
import mihirkathpalia.cambio.routine.database.Routine
import mihirkathpalia.cambio.routine.database.RoutinesDatabase
import mihirkathpalia.cambio.routine.database.RoutinesDatabaseDao
import java.util.*


class RoutineWidgetRemoteViewsFactory(
    private var applicationContext: Context,
    private var intent: Intent
) :
    RemoteViewsService.RemoteViewsFactory {

    private lateinit var dataSource: RoutinesDatabaseDao
    private lateinit var routines: List<Routine>
    private val routineBuffer = mutableListOf<String>()
    private val routineViewType = mutableListOf<Int>()
    private lateinit var context: Context
    private var appWidgetId = 0
    private var hourOfDay = 0
    private var minuteOfDay = 0

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @SuppressLint("CommitPrefEdits")
    override fun onCreate() {
        context = applicationContext
        sharedPreferences = context.getSharedPreferences(
            sharedPrefFile,
            Context.MODE_PRIVATE
        )
        editor = sharedPreferences.edit()
        appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        dataSource = RoutinesDatabase.getInstance(context).routinesDatabaseDao
    }

    override fun onDataSetChanged() {
        Log.i("Tag", "onDataSetChanged()")
        hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        minuteOfDay = Calendar.getInstance().get(Calendar.MINUTE)

        val dayOfWeek = setDayOfWeek()
        routines = setRoutinesList(dayOfWeek)

        routinesSetUp()
    }

    override fun onDestroy() {

    }

    override fun getCount(): Int {
        return routines.size
    }

    override fun getViewAt(p0: Int): RemoteViews {
        val startTime = routines[p0].startTime
        val endTime = routines[p0].endTime
        val startTimeString = setTime(startTime)
        val endTimeString = setTime(endTime)

        val routineWidget = RemoteViews(
            context.packageName,
            when {
                routineViewType[p0] == 1 -> R.layout.widget_routine_active
                routineViewType[p0] == 2 -> R.layout.widget_routine_active_upcoming
                routineViewType[p0] == 3 -> R.layout.widget_routine_end_of_day
                else -> R.layout.widget_routine_upcoming
            }
        )

        if (routineViewType[p0] != 3) {
            routineWidget.setTextViewText(R.id.routineTitle, routines[p0].routineTitle)
            routineWidget.setTextViewText(R.id.routineStartTime, startTimeString)
            routineWidget.setTextViewText(R.id.routineEndTime, endTimeString)
            routineWidget.setTextViewText(R.id.routineDuration, setDuration(startTime, endTime))
            routineWidget.setTextViewText(
                R.id.breakTimeTextView,
                routineBuffer[p0]
            )
        }

        return routineWidget
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 3
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    // Initializer functions

    private fun setDayOfWeek(): Int {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 7
        }
    }

    private fun setRoutinesList(dayOfWeek: Int): List<Routine> {
        val routinesToday = dataSource.getDayRoutines("%$dayOfWeek%")
        return if (routinesToday.isNotEmpty()) {
            val remainingRoutines = dataSource.getRemainingDayRoutines(
                day = "%$dayOfWeek%",
                time = formatTime(hourOfDay, minuteOfDay)
            )
            if (remainingRoutines.isEmpty())
                listOf(routinesToday[routinesToday.size - 1])
            else
                remainingRoutines
        } else {
            emptyList()
        }
    }

    private fun routinesSetUp() {
        routineBuffer.clear()
        routineViewType.clear()

        for (i in routines.indices) {
            val startTime = routines[i].startTime
            val endTime = routines[i].endTime

            val startHour = Integer.valueOf(startTime.toString().substringBefore("."))
            val startMinute = getMinute(startTime.toString().substringAfter("."))

            val endHour = Integer.valueOf(endTime.toString().substringBefore("."))
            val endMinute = getMinute(endTime.toString().substringAfter("."))

            // Routines ViewType
            // 1 -> active
            // 0 -> upcoming
            // 2 -> active upcoming
            // 3 -> end of day
            if (hourOfDay in (startHour + 1) until endHour) {
                routineViewType.add(1)
            } else if (hourOfDay == startHour) {
                when {
                    minuteOfDay >= startMinute ->
                        if (hourOfDay == endHour) {
                            if (minuteOfDay < endMinute) {
                                routineViewType.add(1)
                            } else
                                routineViewType.add(3)
                        } else
                            routineViewType.add(1)
                    i == 0 -> routineViewType.add(2)
                    else -> routineViewType.add(0)
                }
            } else if (hourOfDay == endHour) {
                if (minuteOfDay < endMinute)
                    routineViewType.add(1)
                else
                    routineViewType.add(3)
            } else if (hourOfDay < startHour && i == 0) {
                routineViewType.add(2)
            } else if (hourOfDay > endHour) {
                routineViewType.add(3)
            } else
                routineViewType.add(0)

            //Set Work
            if (routineViewType[i] == 1 || routineViewType[i] == 2 || routineViewType[i] == 3)
                setNextWork(routineViewType[i], i)

            //Routines Buffer
            if (i == routines.lastIndex) {
                routineBuffer.add(context.getString(R.string.end_of_day))
            } else {
                val nextStartTime = routines[i + 1].startTime
                if (nextStartTime == routines[i].endTime) {
                    routineBuffer.add(
                        context.getString(R.string.no) + " " + context.getString(
                            R.string.breakString
                        )
                    )
                } else {
                    routineBuffer.add(
                        setDuration(
                            routines[i].endTime,
                            nextStartTime
                        ) + " " + context.getString(
                            R.string.breakString
                        )
                    )
                }
            }

        }
    }

    private fun setNextWork(active: Int, index: Int) {
        Log.i("Tag", "setNextWork()")
        val now = Calendar.getInstance()
        val next = Calendar.getInstance()

        val nextTime = when (active) {
            1 -> routines[index].endTime.toString()
            2 -> routines[index].startTime.toString()
            else -> "24.0"
        }

        next.set(Calendar.HOUR_OF_DAY, Integer.valueOf(nextTime.substringBefore('.')))
        next.set(Calendar.MINUTE, getMinute(nextTime.substringAfter('.')))

        editor.putLong(refreshDelaySharedPref, next.timeInMillis - now.timeInMillis)
        editor.apply()
    }

}