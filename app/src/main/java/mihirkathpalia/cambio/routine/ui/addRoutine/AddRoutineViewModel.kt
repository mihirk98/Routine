package mihirkathpalia.cambio.routine.ui.addRoutine

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import mihirkathpalia.cambio.routine.database.Routine
import mihirkathpalia.cambio.routine.database.RoutinesDatabaseDao
import mihirkathpalia.cambio.routine.formatTime
import mihirkathpalia.cambio.routine.setTime
import mihirkathpalia.cambio.routine.updateWidgets


class AddRoutineViewModel(
    private val database: RoutinesDatabaseDao,
    private val application: Application,
    private val routineId: Long
) : ViewModel() {

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _updateRoutine = MutableLiveData<Routine?>().apply { postValue(null) }
    val updateRoutine: LiveData<Routine?>
        get() = _updateRoutine

    init {
        if (routineId != -1L) {
            uiScope.launch {
                _updateRoutine.value = getRoutine(routineId)
                if (_updateRoutine.value!!.recurrence.contains('1'))
                    mondayOnClick()
                if (_updateRoutine.value!!.recurrence.contains('2'))
                    tuesdayOnClick()
                if (_updateRoutine.value!!.recurrence.contains('3'))
                    wednesdayOnClick()
                if (_updateRoutine.value!!.recurrence.contains('4'))
                    thursdayOnClick()
                if (_updateRoutine.value!!.recurrence.contains('5'))
                    fridayOnClick()
                if (_updateRoutine.value!!.recurrence.contains('6'))
                    saturdayOnClick()
                if (_updateRoutine.value!!.recurrence.contains('7'))
                    sundayOnClick()
                setStartTime()
                setDuration()
            }
        }
    }

    private suspend fun getRoutine(routineId: Long): Routine? {
        return withContext(Dispatchers.IO) {
            database.get(routineId)
        }
    }

    private val _updateStartTime = MutableLiveData<String?>().apply { postValue(null) }
    val updateStartTime: LiveData<String?>
        get() = _updateStartTime

    private val _updateDuration = MutableLiveData<String?>().apply { postValue(null) }
    val updateDuration: LiveData<String?>
        get() = _updateDuration

    private fun setStartTime() {
        _updateStartTime.value = setTime(_updateRoutine.value!!.startTime)
    }

    private fun setDuration() {
        _updateDuration.value = mihirkathpalia.cambio.routine.setDuration(
            _updateRoutine.value!!.startTime,
            _updateRoutine.value!!.endTime
        )
    }

    private var recurrence = ""

    private val _mondayRecurrence = MutableLiveData<Boolean>().apply { postValue(false) }
    val mondayRecurrence: LiveData<Boolean>
        get() = _mondayRecurrence

    fun mondayOnClick() {
        when (_mondayRecurrence.value) {
            true -> {
                _mondayRecurrence.value = false
                recurrence = recurrence.replace("1", "")
            }
            false -> {
                _mondayRecurrence.value = true
                recurrence += "1"
            }
        }
    }

    private val _tuesdayRecurrence = MutableLiveData<Boolean>().apply { postValue(false) }
    val tuesdayRecurrence: LiveData<Boolean>
        get() = _tuesdayRecurrence

    fun tuesdayOnClick() {
        when (_tuesdayRecurrence.value) {
            true -> {
                _tuesdayRecurrence.value = false
                recurrence = recurrence.replace("2", "")
            }
            false -> {
                _tuesdayRecurrence.value = true
                recurrence += "2"
            }
        }
    }

    private val _wednesdayRecurrence = MutableLiveData<Boolean>().apply { postValue(false) }
    val wednesdayRecurrence: LiveData<Boolean>
        get() = _wednesdayRecurrence

    fun wednesdayOnClick() {
        when (_wednesdayRecurrence.value) {
            true -> {
                _wednesdayRecurrence.value = false
                recurrence = recurrence.replace("3", "")
            }
            false -> {
                _wednesdayRecurrence.value = true
                recurrence += "3"
            }
        }
    }

    private val _thursdayRecurrence = MutableLiveData<Boolean>().apply { postValue(false) }
    val thursdayRecurrence: LiveData<Boolean>
        get() = _thursdayRecurrence

    fun thursdayOnClick() {
        when (_thursdayRecurrence.value) {
            true -> {
                _thursdayRecurrence.value = false
                recurrence = recurrence.replace("4", "")
            }
            false -> {
                _thursdayRecurrence.value = true
                recurrence += "4"
            }
        }
    }

    private val _fridayRecurrence = MutableLiveData<Boolean>().apply { postValue(false) }
    val fridayRecurrence: LiveData<Boolean>
        get() = _fridayRecurrence

    fun fridayOnClick() {
        when (_fridayRecurrence.value) {
            true -> {
                _fridayRecurrence.value = false
                recurrence = recurrence.replace("5", "")
            }
            false -> {
                _fridayRecurrence.value = true
                recurrence += "5"
            }
        }
    }

    private val _saturdayRecurrence = MutableLiveData<Boolean>().apply { postValue(false) }
    val saturdayRecurrence: LiveData<Boolean>
        get() = _saturdayRecurrence

    fun saturdayOnClick() {
        when (_saturdayRecurrence.value) {
            true -> {
                _saturdayRecurrence.value = false
                recurrence = recurrence.replace("6", "")
            }
            false -> {
                _saturdayRecurrence.value = true
                recurrence += "6"
            }
        }
    }

    private val _sundayRecurrence = MutableLiveData<Boolean>().apply { postValue(false) }
    val sundayRecurrence: LiveData<Boolean>
        get() = _sundayRecurrence

    fun sundayOnClick() {
        when (_sundayRecurrence.value) {
            true -> {
                _sundayRecurrence.value = false
                recurrence = recurrence.replace("7", "")
            }
            false -> {
                _sundayRecurrence.value = true
                recurrence += "7"
            }
        }
    }

    fun weekdaysOnClick() {
        when (_mondayRecurrence.value) {
            false -> {
                _mondayRecurrence.value = true
                recurrence += "1"
            }
        }
        when (_tuesdayRecurrence.value) {
            false -> {
                _tuesdayRecurrence.value = true
                recurrence += "2"
            }
        }
        when (_wednesdayRecurrence.value) {
            false -> {
                _wednesdayRecurrence.value = true
                recurrence += "3"
            }
        }
        when (_thursdayRecurrence.value) {
            false -> {
                _thursdayRecurrence.value = true
                recurrence += "4"
            }
        }
        when (_fridayRecurrence.value) {
            false -> {
                _fridayRecurrence.value = true
                recurrence += "5"
            }
        }
        when (_saturdayRecurrence.value) {
            true -> {
                _saturdayRecurrence.value = false
                recurrence = recurrence.replace("6", "")
            }
        }
        when (_sundayRecurrence.value) {
            true -> {
                _sundayRecurrence.value = false
                recurrence = recurrence.replace("7", "")
            }
        }
    }

    fun weekendOnClick() {
        when (_mondayRecurrence.value) {
            true -> {
                _mondayRecurrence.value = false
                recurrence = recurrence.replace("1", "")
            }
        }
        when (_tuesdayRecurrence.value) {
            true -> {
                _tuesdayRecurrence.value = false
                recurrence = recurrence.replace("2", "")
            }
        }
        when (_wednesdayRecurrence.value) {
            true -> {
                _wednesdayRecurrence.value = false
                recurrence = recurrence.replace("3", "")
            }
        }
        when (_thursdayRecurrence.value) {
            true -> {
                _thursdayRecurrence.value = false
                recurrence = recurrence.replace("4", "")
            }
        }
        when (_fridayRecurrence.value) {
            true -> {
                _fridayRecurrence.value = false
                recurrence = recurrence.replace("5", "")
            }
        }
        when (_saturdayRecurrence.value) {
            false -> {
                _saturdayRecurrence.value = true
                recurrence += "6"
            }
        }
        when (_sundayRecurrence.value) {
            false -> {
                _sundayRecurrence.value = true
                recurrence += "7"
            }
        }
    }

    fun everydayOnClick() {
        when (_mondayRecurrence.value) {
            false -> {
                _mondayRecurrence.value = true
                recurrence += "1"
            }
        }
        when (_tuesdayRecurrence.value) {
            false -> {
                _tuesdayRecurrence.value = true
                recurrence += "2"
            }
        }
        when (_wednesdayRecurrence.value) {
            false -> {
                _wednesdayRecurrence.value = true
                recurrence += "3"
            }
        }
        when (_thursdayRecurrence.value) {
            false -> {
                _thursdayRecurrence.value = true
                recurrence += "4"
            }
        }
        when (_fridayRecurrence.value) {
            false -> {
                _fridayRecurrence.value = true
                recurrence += "5"
            }
        }
        when (_saturdayRecurrence.value) {
            false -> {
                _saturdayRecurrence.value = true
                recurrence += "6"
            }
        }
        when (_sundayRecurrence.value) {
            false -> {
                _sundayRecurrence.value = true
                recurrence += "7"
            }
        }
    }

    private val _navigateToRoutines = MutableLiveData<Boolean>().apply { postValue(false) }
    val navigateToRoutines: LiveData<Boolean>
        get() = _navigateToRoutines

    fun navigateToRoutinesComplete() {
        _navigateToRoutines.value = false
    }

    fun backOnClick() {
        _navigateToRoutines.value = true
    }

    private val _routineValidity = MutableLiveData<Boolean>().apply { postValue(true) }
    val routineValidity: LiveData<Boolean>
        get() = _routineValidity

    fun routineValidityDialogComplete() {
        _routineValidity.value = true
    }

    private val _routineValidating = MutableLiveData<Boolean?>().apply { postValue(null) }
    val routineValidating: LiveData<Boolean?>
        get() = _routineValidating

    fun routineValidatingComplete() {
        _routineValidating.value = null
    }

    private val _endTimeHourString = MutableLiveData<String>().apply { postValue("00") }
    val endTimeHourString: LiveData<String>
        get() = _endTimeHourString

    private val _endTimeMinuteString = MutableLiveData<String>().apply { postValue("00") }
    val endTimeMinuteString: LiveData<String>
        get() = _endTimeMinuteString

    private var endTime = 0.0
    private var startTime = 0.0

    fun endTimeChanged(
        startMinutes: Int,
        durationMinutes: Int,
        startHours: Int,
        durationHours: Int
    ) {
        startTime = formatTime(startHours, startMinutes)
        var totalHours = startHours + durationHours
        val totalMinutes = startMinutes + durationMinutes
        if (totalMinutes > 59) {
            totalHours += (totalMinutes / 60)
            setEndTime(totalHours, totalMinutes)
        } else {
            setEndTime(totalHours, totalMinutes)
        }
    }

    private fun setEndTime(h: Int, m: Int) {
        var hours = h
        var mins = m
        if (hours > 23)
            hours -= 24
        if (mins > 59)
            mins -= 60
        endTime = formatTime(hours, mins)
        if (hours <= 9) {
            _endTimeHourString.value = "0$hours"
        } else if (hours <= 23) {
            _endTimeHourString.value = hours.toString()
        }
        if (mins <= 9) {
            _endTimeMinuteString.value = "0$mins"
        } else if (mins <= 59) {
            _endTimeMinuteString.value = mins.toString()
        }
    }

    private val _routineTitleEmpty = MutableLiveData<Boolean>().apply { postValue(false) }
    val routineTitleEmpty: LiveData<Boolean>
        get() = _routineTitleEmpty

    private val _recurrenceEmpty = MutableLiveData<Boolean>().apply { postValue(false) }
    val recurrenceEmpty: LiveData<Boolean>
        get() = _recurrenceEmpty

    private val _durationValidity = MutableLiveData<Boolean>().apply { postValue(false) }
    val durationValidity: LiveData<Boolean>
        get() = _durationValidity

    fun durationValid() {
        _durationValidity.value = false
    }

    fun durationInvalid() {
        _durationValidity.value = true
    }

    fun onSave(routineTitle: String, durationHour: Int, durationMinute: Int) {
        uiScope.launch {
            _routineValidating.value = true
            _routineTitleEmpty.value = routineTitle.isEmpty()
            _durationValidity.value = durationHour == 0 && durationMinute == 0
            _recurrenceEmpty.value = recurrence.isEmpty()
            if (routineTitle.isNotEmpty() && recurrence.isNotEmpty() && (durationHour != 0 || durationMinute != 0)) {
                if (checkRoutines()) {
                    if (routineId == -1L) {
                        val newRoutine = Routine(
                            routineTitle = routineTitle,
                            startTime = startTime,
                            endTime = endTime,
                            recurrence = recurrence
                        )
                        insert(newRoutine)
                    } else {
                        val newRoutine = Routine(
                            routineId = routineId,
                            routineTitle = routineTitle,
                            startTime = startTime,
                            endTime = endTime,
                            recurrence = recurrence
                        )
                        update(newRoutine)
                    }
                    updateWidgets(application)
                    _navigateToRoutines.value = true
                } else {
                    _routineValidity.value = false
                }
            }
            _routineValidating.value = false
        }
    }

    private suspend fun checkRoutines(): Boolean {
        //Checking for routine overlapping
        //Explanation in documentation -> "Routine Overlapping Logic"

        return withContext(Dispatchers.IO) {
            var returnValue = true
            for (day in recurrence) {
                val routines = database.getDayRoutines("%$day%")
                for (routine in routines) {
                    if (routine.routineId != routineId) {
                        if(startTime < endTime) {
                            when {
                                startTime > routine.startTime -> {
                                    if (routine.endTime > startTime)
                                        returnValue = false
                                }
                                startTime == routine.startTime -> returnValue = false
                                startTime < routine.startTime -> {
                                    if (routine.startTime < endTime)
                                        returnValue = false
                                }
                            }
                        } else {
                            returnValue = false
                        }
                    }
                }
            }
            returnValue
        }
    }

    private suspend fun insert(night: Routine) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    private suspend fun update(night: Routine) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}