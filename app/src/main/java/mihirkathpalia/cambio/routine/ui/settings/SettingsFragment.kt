package mihirkathpalia.cambio.routine.ui.settings

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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

        autoRefreshWidgetSwitchListener()
        listenToNoFeedbackClient()

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

    //---- Misc ----

    private fun autoRefreshWidgetSwitchListener() {
        binding.autoRefreshWidgetSwitch.setOnCheckedChangeListener { _, isChecked ->
            settingsViewModel.autoRefreshWidgetCheckChanged(isChecked)
        }
    }

    private fun listenToNoFeedbackClient() {
        settingsViewModel.noFeedbackClient.observe(viewLifecycleOwner, { newEvent ->
            if (newEvent) {
                Toast.makeText(
                    context,
                    getString(R.string.kindly_email_us),
                    Toast.LENGTH_LONG
                ).show()
                settingsViewModel.feedbackOnCLickComplete()
            }
        })
    }
}