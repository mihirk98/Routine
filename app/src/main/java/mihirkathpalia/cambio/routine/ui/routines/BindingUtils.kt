package mihirkathpalia.cambio.routine.ui.routines

import android.widget.TextView
import androidx.databinding.BindingAdapter
import mihirkathpalia.cambio.routine.R
import mihirkathpalia.cambio.routine.database.Routine
import mihirkathpalia.cambio.routine.setTime

@BindingAdapter("routineTitleString")
fun TextView.setRoutineTitleString(item: Routine?) {
    item?.let {
        text = item.routineTitle
    }
}

@BindingAdapter("routineRecurrenceString")
fun TextView.setRoutineRecurrenceString(item: Routine?) {
    item?.let {
        val monday = resources.getString(R.string.monday)
        val tuesday = resources.getString(R.string.tuesday)
        val wednesday = resources.getString(R.string.wednesday)
        val thursday = resources.getString(R.string.thursday)
        val friday = resources.getString(R.string.friday)
        val saturday = resources.getString(R.string.saturday)
        val sunday = resources.getString(R.string.sunday)
        var recurrence = ""
        if (item.recurrence.contains('1'))
            recurrence += "$monday "
        if (item.recurrence.contains('2'))
            recurrence += "$tuesday "
        if (item.recurrence.contains('3'))
            recurrence += "$wednesday "
        if (item.recurrence.contains('4'))
            recurrence += "$thursday "
        if (item.recurrence.contains('5'))
            recurrence += "$friday "
        if (item.recurrence.contains('6'))
            recurrence += "$saturday "
        if (item.recurrence.contains('7'))
            recurrence += "$sunday "
        recurrence = recurrence.removeSuffix(" ")
        when (recurrence) {
            "$monday $tuesday $wednesday $thursday $friday $saturday $sunday" -> recurrence =
                resources.getString(R.string.everyday)
            "$monday $tuesday $wednesday $thursday $friday" -> recurrence =
                context.getString(R.string.weekdays)
            "$saturday $sunday" -> recurrence = context.getString(R.string.weekend)
        }
        text = recurrence
    }
}

@BindingAdapter("routineStartTimeString")
fun TextView.routineStartTimeString(item: Routine?) {
    item?.let {
        text = setTime(item.startTime)
    }
}

@BindingAdapter("routineEndTimeString")
fun TextView.routineEndTimeString(item: Routine?) {
    item?.let {
        text = setTime(item.endTime)
    }
}