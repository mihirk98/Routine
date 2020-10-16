package mihirkathpalia.cambio.routine.ui.routines

import android.app.Application
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import mihirkathpalia.cambio.routine.R
import mihirkathpalia.cambio.routine.database.RoutinesDatabase
import mihirkathpalia.cambio.routine.database.RoutinesDatabaseDao
import mihirkathpalia.cambio.routine.databinding.FragmentRoutinesBinding

class RoutinesFragment : Fragment() {

    private lateinit var routinesViewModel: RoutinesViewModel
    private lateinit var routinesViewModelFactory: RoutinesViewModelFactory
    private lateinit var binding: FragmentRoutinesBinding
    private lateinit var dataSource: RoutinesDatabaseDao
    private lateinit var application: Application

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        application = requireNotNull(this.activity).application

        dataSource = RoutinesDatabase.getInstance(application).routinesDatabaseDao

        routinesViewModelFactory = RoutinesViewModelFactory(dataSource, application)

        routinesViewModel =
            ViewModelProvider(this, routinesViewModelFactory).get(RoutinesViewModel::class.java)

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_routines,
            container,
            false
        )

        // Set recyclerView layoutManager to linearLayout
        val manager = LinearLayoutManager(activity)
        binding.routinesList.layoutManager = manager

        //  Set recyclerView adapter
        val adapter = RoutinesAdapter(RoutineItemListener { routineId ->
            routinesViewModel.onRoutineClicked(routineId)
        }, RoutineItemListener { routineId -> routinesViewModel.onRoutineDeleteClicked(routineId) })
        binding.routinesList.adapter = adapter

        binding.routinesViewModel = routinesViewModel
        binding.lifecycleOwner = this

        routinesViewModel.navigateToAddRoutine.observe(
            viewLifecycleOwner,
            { navigateEvent ->
                if (navigateEvent) {
                    addRoutineOnClickListener()
                    routinesViewModel.navigateToAddRoutineComplete()
                }
            })

        routinesViewModel.navigateToSettings.observe(viewLifecycleOwner, { navigateEvent ->
            if (navigateEvent) {
                settingsOnClickListener()
                routinesViewModel.navigateToSettingsComplete()
            }
        })

        routinesViewModel.navigateToSelf.observe(viewLifecycleOwner, { navigateEvent ->
            if (navigateEvent) {
                selfOnClickListener()
                routinesViewModel.navigateToSelfComplete()
            }
        })

        routinesViewModel.navigateToEditRoutine.observe(viewLifecycleOwner, { routineId ->
            routineId?.let {
                routineOnClickListener(routineId)
                routinesViewModel.onRoutineClickedComplete()
            }
        })

        routinesViewModel.routines.observe(viewLifecycleOwner, { routineList ->
            if (routineList.isNullOrEmpty()) {
                binding.routinesList.visibility = GONE
                binding.emptyView.visibility = VISIBLE
            } else {
                binding.routinesList.visibility = VISIBLE
                binding.emptyView.visibility = GONE
                adapter.submitList(routineList)
            }
        })

        return binding.root
    }

    private fun selfOnClickListener() {
        activity?.recreate()
    }

    private fun settingsOnClickListener() {
        this.findNavController().navigate(R.id.action_routinesFragment_to_settingsFragment)
    }

    private fun addRoutineOnClickListener() {
        val action = RoutinesFragmentDirections.actionRoutinesFragmentToAddRoutineFragment(-1L)
        this.findNavController().navigate(action)
    }

    private fun routineOnClickListener(routineId: Long) {
        val action =
            RoutinesFragmentDirections.actionRoutinesFragmentToAddRoutineFragment(routineId)
        this.findNavController().navigate(action)
    }
}
