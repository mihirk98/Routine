package mihirkathpalia.cambio.routine.ui.addRoutine

import android.app.Activity
import android.app.Application
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import mihirkathpalia.cambio.routine.R
import mihirkathpalia.cambio.routine.database.RoutinesDatabase
import mihirkathpalia.cambio.routine.database.RoutinesDatabaseDao
import mihirkathpalia.cambio.routine.databinding.FragmentAddRoutineBinding


class AddRoutineFragment : Fragment() {

    private lateinit var addRoutineViewModel: AddRoutineViewModel
    private lateinit var addRoutineViewModelFactory: AddRoutineViewModelFactory
    private lateinit var binding: FragmentAddRoutineBinding
    private lateinit var dataSource: RoutinesDatabaseDao
    private lateinit var application: Application

    private val args: AddRoutineFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        application = requireNotNull(this.activity).application

        dataSource = RoutinesDatabase.getInstance(application).routinesDatabaseDao

        addRoutineViewModelFactory =
            AddRoutineViewModelFactory(dataSource, application, args.routineId)

        addRoutineViewModel =
            ViewModelProvider(this, addRoutineViewModelFactory).get(AddRoutineViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_routine,
            container,
            false
        )

        // Navigation observers
        navigateToRoutinesObserver()

        initializePickers()
        setStartTimePicker()
        setDurationPicker()
        hideEditTextOnDone()
        routineValidityDialog()
        routineValidatingDialog()
        recurrenceButtonOnClicks()
        endTimeSetter()

        binding.addRoutineViewModel = addRoutineViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    //---- Navigation ----

    //Routines
    private fun navigateToRoutinesObserver() {
        addRoutineViewModel.navigateToRoutines.observe(
            viewLifecycleOwner,
            { navigateEvent ->
                if (navigateEvent) {
                    backOnClickListener()
                    addRoutineViewModel.navigateToRoutinesComplete()
                }
            })
    }

    private fun backOnClickListener() {
        this.findNavController().popBackStack()
    }

    //---- Dialog ----

    //Routine timing overlap notification dialog
    private fun routineValidityDialog() {
        addRoutineViewModel.routineValidity.observe(viewLifecycleOwner, { newEvent ->
            if (!newEvent) {
                val dialogBuilder = AlertDialog.Builder(requireContext())
                dialogBuilder.setMessage(R.string.routine_timings_overlap)
                    .setCancelable(false)
                    .setPositiveButton(R.string.okay, DialogInterface.OnClickListener { dialog, _ ->
                        addRoutineViewModel.routineValidityDialogComplete()
                        dialog.dismiss()
                    })
                val alertDialog = dialogBuilder.create()
                alertDialog.window?.setBackgroundDrawableResource(R.drawable.drawable_dialog_background)
                alertDialog.show()
            }
        })
    }

    //Validating Routine
    private fun routineValidatingDialog() {

        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage(getString(R.string.loading))
            .setCancelable(false)
        val alertDialog = dialogBuilder.create()

        addRoutineViewModel.routineValidating.observe(viewLifecycleOwner, { validating ->
            validating?.let {
                if (validating) {
                    alertDialog.window?.setBackgroundDrawableResource(R.drawable.drawable_dialog_background)
                    alertDialog.show()
                } else {
                    alertDialog.hide()
                    addRoutineViewModel.routineValidatingComplete()
                }
            }
        })
    }

    //---- Misc ----

    private fun initializePickers() {
        val hours = arrayOf(
            "00",
            "01",
            "02",
            "03",
            "04",
            "05",
            "06",
            "07",
            "08",
            "09",
            "10",
            "11",
            "12",
            "13",
            "14",
            "15",
            "16",
            "17",
            "18",
            "19",
            "20",
            "21",
            "22",
            "23"
        )
        binding.startTimeHourPicker.minValue = 0
        binding.startTimeHourPicker.maxValue = hours.size - 1
        binding.startTimeHourPicker.displayedValues = hours
        binding.durationHourPicker.minValue = 0
        binding.durationHourPicker.maxValue = hours.size - 1
        binding.durationHourPicker.displayedValues = hours

        val minutes = arrayOf(
            "00",
            "05",
            "10",
            "15",
            "20",
            "25",
            "30",
            "35",
            "40",
            "45",
            "50",
            "55"
        )
        binding.startTimeMinutePicker.minValue = 0
        binding.startTimeMinutePicker.maxValue = minutes.size - 1
        binding.startTimeMinutePicker.displayedValues = minutes
        binding.durationMinutePicker.minValue = 0
        binding.durationMinutePicker.maxValue = minutes.size - 1
        binding.durationMinutePicker.displayedValues = minutes
    }

    private fun setStartTimePicker() {
        addRoutineViewModel.updateStartTime.observe(viewLifecycleOwner, { startTime ->
            startTime?.let {
                binding.startTimeHourPicker.value = Integer.valueOf(startTime.substringBefore(':'))
                binding.startTimeMinutePicker.value =
                    Integer.valueOf(startTime.substringAfter(':')) / 5
            }
        })
    }

    private fun setDurationPicker() {
        addRoutineViewModel.updateDuration.observe(viewLifecycleOwner, { duration ->
            duration?.let {
                val durationRaw = duration.replace(" ", "")
                if (durationRaw.contains("hr"))
                    binding.durationHourPicker.value =
                        Integer.valueOf(durationRaw.substringBefore('h'))
                else
                    binding.durationHourPicker.value = 0

                if (durationRaw.contains("mins")) {
                    if (durationRaw.contains("hrs") || durationRaw.contains("hr"))
                        binding.durationMinutePicker.value =
                            Integer.valueOf(
                                durationRaw.substringAfter('\n').replace("mins", "")
                            ) / 5
                    else
                        binding.durationMinutePicker.value =
                            Integer.valueOf(durationRaw.substringBefore('m')) / 5
                } else
                    binding.durationMinutePicker.value = 0
                endTimeChanged()
            }
        })
    }

    private fun hideEditTextOnDone() {
        binding.routineNameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val imm: InputMethodManager =
                    requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireView().windowToken, 0)
            }
            false
        }
    }

    private fun recurrenceButtonOnClicks() {
        addRoutineViewModel.mondayRecurrence.observe(viewLifecycleOwner, {
            if (it)
                binding.mondayButton.setBackgroundResource(R.drawable.drawable_recurrence_button_selected)
            else
                binding.mondayButton.setBackgroundResource(R.drawable.drawable_widget_item_background)
        })

        addRoutineViewModel.tuesdayRecurrence.observe(viewLifecycleOwner, {
            if (it)
                binding.tuesdayButton.setBackgroundResource(R.drawable.drawable_recurrence_button_selected)
            else
                binding.tuesdayButton.setBackgroundResource(R.drawable.drawable_widget_item_background)
        })

        addRoutineViewModel.wednesdayRecurrence.observe(viewLifecycleOwner, {
            if (it)
                binding.wednesdayButton.setBackgroundResource(R.drawable.drawable_recurrence_button_selected)
            else
                binding.wednesdayButton.setBackgroundResource(R.drawable.drawable_widget_item_background)
        })

        addRoutineViewModel.thursdayRecurrence.observe(viewLifecycleOwner, {
            if (it)
                binding.thursdayButton.setBackgroundResource(R.drawable.drawable_recurrence_button_selected)
            else
                binding.thursdayButton.setBackgroundResource(R.drawable.drawable_widget_item_background)
        })

        addRoutineViewModel.fridayRecurrence.observe(viewLifecycleOwner, {
            if (it)
                binding.fridayButton.setBackgroundResource(R.drawable.drawable_recurrence_button_selected)
            else
                binding.fridayButton.setBackgroundResource(R.drawable.drawable_widget_item_background)
        })

        addRoutineViewModel.saturdayRecurrence.observe(viewLifecycleOwner, {
            if (it)
                binding.saturdayButton.setBackgroundResource(R.drawable.drawable_recurrence_button_selected)
            else
                binding.saturdayButton.setBackgroundResource(R.drawable.drawable_widget_item_background)
        })

        addRoutineViewModel.sundayRecurrence.observe(viewLifecycleOwner, {
            if (it)
                binding.sundayButton.setBackgroundResource(R.drawable.drawable_recurrence_button_selected)
            else
                binding.sundayButton.setBackgroundResource(R.drawable.drawable_widget_item_background)
        })
    }

    private fun endTimeSetter() {
        // Hour
        binding.startTimeHourPicker.setOnValueChangedListener { _, _, _ ->
            endTimeChanged()
        }
        binding.durationHourPicker.setOnValueChangedListener { _, _, newVal ->
            endTimeChanged()
            if (newVal != 0)
                addRoutineViewModel.durationValid()
            else if (binding.durationMinutePicker.value == 0) {
                addRoutineViewModel.durationInvalid()
            }
        }

        // Minute
        binding.startTimeMinutePicker.setOnValueChangedListener { _, _, _ ->
            endTimeChanged()
        }
        binding.durationMinutePicker.setOnValueChangedListener { _, _, newVal ->
            endTimeChanged()
            if (newVal != 0)
                addRoutineViewModel.durationValid()
            else if (binding.durationHourPicker.value == 0) {
                addRoutineViewModel.durationInvalid()
            }
        }
    }

    private fun endTimeChanged() {
        val startMinutes = binding.startTimeMinutePicker.value * 5
        val durationMinutes = binding.durationMinutePicker.value * 5
        val startHours = binding.startTimeHourPicker.value
        val durationHours = binding.durationHourPicker.value
        addRoutineViewModel.endTimeChanged(startMinutes, durationMinutes, startHours, durationHours)
    }

}