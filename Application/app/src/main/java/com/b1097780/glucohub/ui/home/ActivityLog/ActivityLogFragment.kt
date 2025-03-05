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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ActivityLogFragment : Fragment() {

    private var _binding: FragmentActivityLogBinding? = null
    private val binding get() = _binding!!
    private lateinit var activityLogViewModel: ActivityLogViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityLogViewModel = ViewModelProvider(requireActivity()).get(ActivityLogViewModel::class.java)
        _binding = FragmentActivityLogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activityLogViewModel.loadRecentBloodEntry(requireContext())
        activityLogViewModel.loadActivityEntries(requireContext())

        setupObservers()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        activityLogViewModel.recentGlucoseTime.observe(viewLifecycleOwner) { time ->
            binding.recentTime.text = time
        }

        activityLogViewModel.recentGlucoseValue.observe(viewLifecycleOwner) { value ->
            binding.recentGlucose.text = value
        }

        activityLogViewModel.activityLogEntries.observe(viewLifecycleOwner) { activities ->
            updateActivityTables(activities)
        }
    }

    private fun updateActivityTables(activities: List<ActivityLogEntry>) {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        // Clear previous content
        binding.recentActivitiesTable.removeAllViews()
        binding.upcomingActivitiesTable.removeAllViews()

        if (activities.isEmpty()) {
            showNoActivityMessage()
            return
        }

        // Sort activities into "Recent" and "Upcoming"
        val recentActivities = mutableListOf<ActivityLogEntry>()
        val upcomingActivities = mutableListOf<ActivityLogEntry>()

        for (entry in activities) {
            val startTime = LocalTime.parse(entry.startTime, formatter)

            when {
                // Recent: The activity has started before or at the current time
                startTime <= now -> recentActivities.add(entry)

                // Upcoming: The activity starts in the future
                startTime > now -> upcomingActivities.add(entry)
            }
        }

        // Sort Recent by start time descending (most recent first) and take the closest 4
        recentActivities.sortByDescending { LocalTime.parse(it.startTime, formatter) }
        val displayedRecent = recentActivities.take(4)

        // Sort Upcoming by start time ascending (soonest first)
        upcomingActivities.sortBy { LocalTime.parse(it.startTime, formatter) }

        // Populate tables
        populateTable(binding.recentActivitiesTable, displayedRecent, "No recent activities")
        populateTable(binding.upcomingActivitiesTable, upcomingActivities, "No upcoming activities")
    }

    private fun populateTable(table: ViewGroup, activities: List<ActivityLogEntry>, emptyMessage: String) {
        if (activities.isEmpty()) {
            val noActivityText = TextView(requireContext()).apply {
                text = emptyMessage
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            table.addView(noActivityText)
            return
        }

        for (entry in activities) {
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
            table.addView(row)
        }
    }

    private fun showNoActivityMessage() {
        listOf(
            binding.recentActivitiesTable to "No recent activities",
            binding.upcomingActivitiesTable to "No upcoming activities"
        ).forEach { (table, message) ->
            val textView = TextView(requireContext()).apply {
                text = message
                textSize = 16f
                setPadding(16, 16, 16, 16)
            }
            table.addView(textView)
        }
    }
}
