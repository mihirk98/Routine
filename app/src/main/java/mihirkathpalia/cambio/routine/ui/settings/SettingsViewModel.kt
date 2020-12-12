package mihirkathpalia.cambio.routine.ui.settings

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.*
import mihirkathpalia.cambio.routine.*


class SettingsViewModel(val application: Application) : ViewModel() {

    private val sharedPreferences: SharedPreferences = application.getSharedPreferences(
        sharedPrefFile,
        Context.MODE_PRIVATE
    )
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private val _autoRefreshWidgetState = MutableLiveData<Boolean>()
    val autoRefreshWidgetState: LiveData<Boolean>
        get() = _autoRefreshWidgetState

    private val _autoRefreshSwitchState = MutableLiveData<Boolean>().apply { postValue(false) }
    val autoRefreshSwitchState: LiveData<Boolean>
        get() = _autoRefreshSwitchState

    val appVersion = MutableLiveData<String>()

    init {
        checkSyncWorkState()
        try {
            val pInfo: PackageInfo = application.packageManager.getPackageInfo(
                application.packageName,
                0
            )
            appVersion.value = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            appVersion.value = application.getString(R.string.error)
            e.printStackTrace()
        }
    }

    private fun checkSyncWorkState() {
        uiScope.launch {
            suspend {
                withContext(Dispatchers.IO) {
                    var syncWorkState = false
                    for (workInfo in WorkManager.getInstance(application)
                        .getWorkInfosByTag(syncWork).get()) {
                        if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                            syncWorkState = true
                            break
                        }
                    }
                    editor.putBoolean(syncWorkSharedPref, syncWorkState)
                    editor.apply()
                }
            }
            _autoRefreshSwitchState.value = true
            setAutoRefreshWidgetState()
        }
    }

    fun autoRefreshWidgetCheckChanged(isChecked: Boolean) {
        editor.putBoolean(syncWorkSharedPref, isChecked)
        editor.apply()
        if (isChecked) {
            Log.i("Tag", "autoRefreshWidgetCheckChanged(): isChecked=true")
            syncWork(application)
        } else
            WorkManager.getInstance(application).cancelAllWorkByTag(syncWork)
        setAutoRefreshWidgetState()
    }

    private fun setAutoRefreshWidgetState() {
        _autoRefreshWidgetState.value = sharedPreferences.getBoolean(syncWorkSharedPref, false)
    }

    private val _navigateToRoutines = MutableLiveData<Boolean>()
    val navigateToRoutines: LiveData<Boolean>
        get() = _navigateToRoutines

    fun navigateToRoutinesComplete() {
        _navigateToRoutines.value = false
    }

    fun backOnClick() {
        _navigateToRoutines.value = true
    }

    private val _navigateToShare = MutableLiveData<Boolean>()
    val navigateToShare: LiveData<Boolean>
        get() = _navigateToShare

    fun shareOnClick() {
        _navigateToShare.value = true
    }

    fun shareOnClickComplete() {
        _navigateToShare.value = false
    }

    private val _navigateToRate = MutableLiveData<Boolean>()
    val navigateToRate: LiveData<Boolean>
        get() = _navigateToRate

    fun rateOnClick() {
        _navigateToRate.value = true
    }

    fun rateOnClickComplete() {
        _navigateToRate.value = false
    }

    private val _navigateToFeedback = MutableLiveData<Boolean>()
    val navigateToFeedback: LiveData<Boolean>
        get() = _navigateToFeedback

    fun feedbackOnClick() {
        _navigateToFeedback.value = true
    }

    fun feedbackOnCLickComplete() {
        _navigateToFeedback.value = false
    }

    private val _navigateToPrivacyPolicy = MutableLiveData<Boolean>()
    val navigateToPrivacyPolicy: LiveData<Boolean>
        get() = _navigateToPrivacyPolicy

    fun privacyPolicyOnClick() {
        _navigateToPrivacyPolicy.value = true
    }

    fun privacyPolicyOnClickComplete() {
        _navigateToPrivacyPolicy.value = false
    }
}