package mihirkathpalia.cambio.routine.ui.routineWidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import mihirkathpalia.cambio.routine.routineWidgetAlarm
import mihirkathpalia.cambio.routine.sharedPrefFile
import mihirkathpalia.cambio.routine.syncWork
import mihirkathpalia.cambio.routine.syncWorkSharedPref

class RoutineWidgetAlarm : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            sharedPrefFile,
            Context.MODE_PRIVATE
        )

        if (intent.action == routineWidgetAlarm) {

            val refreshState = sharedPreferences.getBoolean(syncWorkSharedPref, false)

            // Do work and reschedule
            if(refreshState) {
                syncWork(context)
            }
        }
    }
}