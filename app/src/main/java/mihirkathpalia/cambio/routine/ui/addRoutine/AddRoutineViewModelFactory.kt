package mihirkathpalia.cambio.routine.ui.addRoutine

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import mihirkathpalia.cambio.routine.database.RoutinesDatabaseDao

class AddRoutineViewModelFactory(
    private val dataSource: RoutinesDatabaseDao,
    private val application: Application,
    private val routineId: Long
) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddRoutineViewModel::class.java)) {
            return AddRoutineViewModel(dataSource, application, routineId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}