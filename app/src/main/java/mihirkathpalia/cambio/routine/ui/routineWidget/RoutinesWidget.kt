package mihirkathpalia.cambio.routine.ui.routineWidget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.work.*
import mihirkathpalia.cambio.routine.*
import mihirkathpalia.cambio.routine.R
import mihirkathpalia.cambio.routine.database.Routine
import mihirkathpalia.cambio.routine.work.WidgetSyncWorker
import java.util.concurrent.TimeUnit

const val syncClicked = "android.appwidget.action.SYNC"

class RoutinesWidget : AppWidgetProvider() {

    override fun onEnabled(context: Context?) {
        context?.let {

            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                sharedPrefFile,
                Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putBoolean(syncWorkSharedPref, true)
            editor.apply()

            syncWork(context)
        }
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context?) {
        context?.let {

            val sharedPreferences: SharedPreferences = context.getSharedPreferences(
                sharedPrefFile,
                Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putBoolean(syncWorkSharedPref, false)
            editor.apply()

            WorkManager.getInstance(context).cancelAllWorkByTag(syncWork)
        }
        super.onDisabled(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (syncClicked == intent.action) {
                context?.let { syncWork(it) }
                Toast.makeText(
                    context,
                    context?.getString(R.string.routine_synced),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        super.onReceive(context, intent)
    }

}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_routines)

    val routinesListIntent = Intent(context, RoutineWidgetService::class.java)
    routinesListIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    routinesListIntent.data = Uri.parse(routinesListIntent.toUri(Intent.URI_INTENT_SCHEME))

    views.setRemoteAdapter(R.id.routinesList, routinesListIntent)
    views.setEmptyView(R.id.routinesList, R.id.emptyView)

    val routineActivityIntent = Intent(context, MainActivity::class.java)
    val pendingRAI = PendingIntent.getActivity(context, 0, routineActivityIntent, 0)

    views.setOnClickPendingIntent(R.id.routinesWidgetLinearLayout, pendingRAI)

    val intentSync = Intent(context, RoutinesWidget::class.java)
    intentSync.action = syncClicked
    val pendingIntentSync =
        PendingIntent.getBroadcast(context, 0, intentSync, PendingIntent.FLAG_UPDATE_CURRENT)
    views.setOnClickPendingIntent(R.id.widgetRefresh, pendingIntentSync)

    appWidgetManager.updateAppWidget(appWidgetId, views)
}