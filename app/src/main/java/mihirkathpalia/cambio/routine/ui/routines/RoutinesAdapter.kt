package mihirkathpalia.cambio.routine.ui.routines

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import mihirkathpalia.cambio.routine.database.Routine
import mihirkathpalia.cambio.routine.databinding.ItemRoutineBinding

class RoutinesAdapter(private val routineClickListener: RoutineItemListener, private val routineDeleteClickListener: RoutineItemListener) : ListAdapter<Routine,
        RoutinesAdapter.ViewHolder>(RoutinesDiffCallback()) {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(routineClickListener, routineDeleteClickListener, item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(private val binding: ItemRoutineBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(routineClickListener: RoutineItemListener, routineDeleteClickListener: RoutineItemListener, item: Routine) {
            binding.routine = item
            binding.routineClickListener = routineClickListener
            binding.routineDeleteClickListener = routineDeleteClickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRoutineBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class RoutinesDiffCallback : DiffUtil.ItemCallback<Routine>() {
    override fun areItemsTheSame(oldItem: Routine, newItem: Routine): Boolean {
        return oldItem.routineId == newItem.routineId
    }

    override fun areContentsTheSame(oldItem: Routine, newItem: Routine): Boolean {
        return oldItem == newItem
    }
}

class RoutineItemListener(val clickListener: (routineId: Long) -> Unit) {
    fun onClick(routine: Routine) = clickListener(routine.routineId)
}