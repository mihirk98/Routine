package mihirkathpalia.cambio.routine.ui.routines

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import mihirkathpalia.cambio.routine.R
import mihirkathpalia.cambio.routine.database.Routine
import mihirkathpalia.cambio.routine.database.RoutinesDatabaseDao
import mihirkathpalia.cambio.routine.sharedPrefFile
import mihirkathpalia.cambio.routine.themeSharedPref
import mihirkathpalia.cambio.routine.updateWidgets

class RoutinesViewModel(val database: RoutinesDatabaseDao, private val application: Application) :
    ViewModel() {

    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        sharedPrefFile,
        Context.MODE_PRIVATE
    )
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    val routines = database.getAllRoutines()

    private val _navigateToAddRoutine = MutableLiveData<Boolean>()
    val navigateToAddRoutine: LiveData<Boolean>
        get() = _navigateToAddRoutine

    private val _navigateToSettings = MutableLiveData<Boolean>()
    val navigateToSettings: LiveData<Boolean>
        get() = _navigateToSettings

    private val _navigateToSelf = MutableLiveData<Boolean>()
    val navigateToSelf: LiveData<Boolean>
        get() = _navigateToSelf

    private val _navigateToEditRoutine = MutableLiveData<Long>()
    val navigateToEditRoutine
        get() = _navigateToEditRoutine

    fun navigateToAddRoutineComplete() {
        _navigateToAddRoutine.value = false
    }

    fun addOnClick() {
        _navigateToAddRoutine.value = true
    }

    fun navigateToSettingsComplete() {
        _navigateToSettings.value = false
    }

    fun settingsOnClick() {
        _navigateToSettings.value = true
    }

    fun navigateToSelfComplete() {
        _navigateToSelf.value = false
    }

    fun onRoutineClicked(id: Long) {
        _navigateToEditRoutine.value = id
    }

    fun onRoutineClickedComplete() {
        _navigateToEditRoutine.value = null
    }

    fun onRoutineDeleteClicked(id: Long) {
        uiScope.launch {
            delete(id)
            updateWidgets(application)
        }
    }

    private suspend fun delete(routineId: Long) {
        withContext(Dispatchers.IO) {
            database.delete(routineId)
        }
    }

    fun themeOnSwitch() {
        if (sharedPreferences.getInt(themeSharedPref, 0) == 0) {
            editor.putInt(themeSharedPref, 1)
        } else {
            editor.putInt(themeSharedPref, 0)
        }
        editor.apply()
        _navigateToSelf.value = true
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}