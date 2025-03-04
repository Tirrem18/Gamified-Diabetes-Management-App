package com.b1097780.glucohub.ui.home.ActivityLog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.databinding.FragmentActivityLogBinding

class ActivityLogFragment : Fragment() {

    private var _binding: FragmentActivityLogBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityLogViewModel: ActivityLogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ViewModel
        activityLogViewModel = ViewModelProvider(requireActivity()).get(ActivityLogViewModel::class.java)
        _binding = FragmentActivityLogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // ✅ Load the most recent blood glucose entry (NO CHANGES HERE)
        activityLogViewModel.loadRecentBloodEntry(requireContext())

        // ✅ Load today's activity logs from SharedPreferences
        activityLogViewModel.loadActivityEntries(requireContext())

        // Setup LiveData observers
        setupObservers()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        activityLogViewModel.recentGlucoseTime.removeObservers(viewLifecycleOwner)
        activityLogViewModel.recentGlucoseTime.observe(viewLifecycleOwner) { time ->
            binding.recentTime.text = time
        }

        activityLogViewModel.recentGlucoseValue.removeObservers(viewLifecycleOwner)
        activityLogViewModel.recentGlucoseValue.observe(viewLifecycleOwner) { value ->
            binding.recentGlucose.text = value
        }

        activityLogViewModel.activityLogEntries.removeObservers(viewLifecycleOwner)
        activityLogViewModel.activityLogEntries.observe(viewLifecycleOwner) { activities ->
            updateActivityTable(activities)
        }
    }


    private fun updateActivityTable(activities: List<ActivityLogEntry>) {
        if (activities.isEmpty() && binding.activityLogTable.childCount > 0) {
            return // ✅ No need to clear and re-add the same "No activities logged" message
        }

        binding.activityLogTable.removeAllViews()

        if (activities.isEmpty()) {
            val noActivityText = TextView(requireContext()).apply {
                text = "No activities logged for today"
                textSize = 18f
                setPadding(16, 16, 16, 16)
            }
            binding.activityLogTable.addView(noActivityText)
            return
        }

        val latestActivities = activities.takeLast(5).reversed()

        for (entry in latestActivities) {
            val row = TableRow(requireContext())

            val activityText = TextView(requireContext()).apply {
                text = entry.name
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }

            val timeText = TextView(requireContext()).apply {
                text = if (entry.endTime != null) "${entry.startTime} - ${entry.endTime}" else entry.startTime
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }

            val detailsText = TextView(requireContext()).apply {
                text = entry.description ?: "No details"
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }

            row.addView(activityText)
            row.addView(timeText)
            row.addView(detailsText)
            binding.activityLogTable.addView(row)
        }
    }

}
