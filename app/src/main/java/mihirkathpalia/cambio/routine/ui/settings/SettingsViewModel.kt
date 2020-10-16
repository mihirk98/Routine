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

    fun shareOnClick() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Routine - Visualize your day, week, life\nhttps://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        application.startActivity(shareIntent)
    }

    fun rateOnClick() {
        val uri: Uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        } else {
            goToMarket.addFlags(
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )
        }
        try {
            application.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            application.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                )
            )
        }
    }

    private val _noFeedbackClient = MutableLiveData<Boolean>().apply { postValue(false) }
    val noFeedbackClient: LiveData<Boolean>
        get() = _noFeedbackClient

    fun feedbackOnClick() {
        val i = Intent(Intent.ACTION_SENDTO)
        i.type = "message/rfc822"
        i.data = Uri.parse("mailto:")
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf("cambiomobile@gmail.com"))
        i.putExtra(Intent.EXTRA_SUBJECT, application.getString(R.string.routine_feedback))
        try {
            application.startActivity(
                Intent.createChooser(
                    i,
                    application.getString(R.string.send_feedback)
                )
            )
        } catch (ex: ActivityNotFoundException) {
            _noFeedbackClient.value = true
        }
    }

    fun feedbackOnCLickComplete() {
        _noFeedbackClient.value = false
    }
}