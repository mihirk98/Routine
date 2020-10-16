package mihirkathpalia.cambio.routine.ui.routineWidget

import android.content.Intent
import android.widget.RemoteViewsService

class RoutineWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return RoutineWidgetRemoteViewsFactory(this.applicationContext, intent)
    }
}
