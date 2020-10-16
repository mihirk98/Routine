package mihirkathpalia.cambio.routine.work

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import mihirkathpalia.cambio.routine.*
import mihirkathpalia.cambio.routine.R
import mihirkathpalia.cambio.routine.ui.routineWidget.RoutinesWidget
import java.util.*
import java.util.concurrent.TimeUnit


class WidgetSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val sharedPreferences: SharedPreferences = appContext.getSharedPreferences(
        sharedPrefFile,
        Context.MODE_PRIVATE
    )

    override fun doWork(): Result {

        val refreshState = sharedPreferences.getBoolean(syncWorkSharedPref, false)

        // Do work and reschedule
        if(refreshState) {
            syncWork(applicationContext)
        }

        return Result.success()
    }
}


