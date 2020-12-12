package mihirkathpalia.cambio.routine.ui.settings

import android.app.Activity
import android.app.Application
import android.content.*
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import mihirkathpalia.cambio.routine.BuildConfig
import mihirkathpalia.cambio.routine.R
import mihirkathpalia.cambio.routine.databinding.FragmentSettingsBinding
import mihirkathpalia.cambio.routine.refreshDelaySharedPref
import mihirkathpalia.cambio.routine.sharedPrefFile
import java.util.*


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var settingsViewModelFactory: SettingsViewModelFactory
    private lateinit var application: Application
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        application = requireNotNull(this.activity).application

        settingsViewModelFactory = SettingsViewModelFactory(application)

        settingsViewModel =
            ViewModelProvider(this, settingsViewModelFactory).get(SettingsViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_settings,
            container,
            false
        )

        binding.settingsViewModel = settingsViewModel
        binding.lifecycleOwner = this

        // Navigation observers
        navigateToRoutinesObserver()
        navigateToShareObserver()
        navigateToRateObserver()
        navigateToFeedbackObserver()
        navigateToPrivacyPolicyObserver()

        autoRefreshWidgetSwitchListener()

        return binding.root
    }

    //---- Navigation ----

    //Routines
    private fun navigateToRoutinesObserver() {
        settingsViewModel.navigateToRoutines.observe(viewLifecycleOwner, { navigateEvent ->
            if (navigateEvent) {
                backOnClickListener()
                settingsViewModel.navigateToRoutinesComplete()
            }
        })
    }

    private fun backOnClickListener() {
        this.findNavController().popBackStack()
    }

    //Share
    private fun navigateToShareObserver() {
        settingsViewModel.navigateToShare.observe(viewLifecycleOwner, { navigateEvent ->
            if(navigateEvent) {
                shareOnClickListener()
                settingsViewModel.shareOnClickComplete()
            }
        })
    }

    private fun shareOnClickListener() {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                "Routine - Visualize your day, week, life\nhttps://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
            )
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    //Rate
    private fun navigateToRateObserver() {
        settingsViewModel.navigateToRate.observe(viewLifecycleOwner, { navigateEvent ->
            if(navigateEvent) {
                rateOnClickListener()
                settingsViewModel.rateOnClickComplete()
            }
        })
    }

    private fun rateOnClickListener() {
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
            val marketApps = application.packageManager
                .queryIntentActivities(goToMarket, 0)
            var marketFound = false
            for(marketApp in marketApps) {
                if (marketApp.activityInfo.applicationInfo.packageName == "com.android.vending") {
                    marketFound = true
                    val marketAppActivity = marketApp.activityInfo
                    val componentInfo = ComponentName(marketAppActivity.applicationInfo.packageName, marketAppActivity.name)
                    goToMarket.component = componentInfo
                    startActivity(goToMarket)
                }
            }
            if(!marketFound) {
                val url = "http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
        } catch (e: ActivityNotFoundException) {
            val url = "http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    //Feedback
    private fun navigateToFeedbackObserver() {
        settingsViewModel.navigateToFeedback.observe(viewLifecycleOwner, { navigateEvent ->
            if(navigateEvent) {
                feedbackOnClickListener()
                settingsViewModel.feedbackOnCLickComplete()
            }
        })
    }

    private fun feedbackOnClickListener() {
        val i = Intent(Intent.ACTION_SENDTO)
        i.type = "message/rfc822"
        i.data = Uri.parse("mailto:")
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf("cambiomobile@gmail.com"))
        i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.routine_feedback))
        try {
            startActivity(
                Intent.createChooser(
                    i,
                    getString(R.string.send_feedback)
                )
            )
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                context,
                getString(R.string.kindly_email_us),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //Privacy Policy
    private fun navigateToPrivacyPolicyObserver() {
        settingsViewModel.navigateToPrivacyPolicy.observe(viewLifecycleOwner, { navigateEvent ->
            if(navigateEvent) {
                privacyPolicyClickListener()
                settingsViewModel.privacyPolicyOnClickComplete()
            }
        })
    }

    private fun privacyPolicyClickListener() {
        val url = "https://sites.google.com/view/routineapp"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    //---- Misc ----

    private fun autoRefreshWidgetSwitchListener() {
        binding.autoRefreshWidgetSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.autoRefreshWidgetCheckChanged(isChecked)
        }
    }
}