package com.b1097780.glucohub.ui.home.ActivityLog

import android.graphics.Typeface
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

    // ✅ Lifecycle Method: Create View & Initialize ViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityLogViewModel = ViewModelProvider(requireActivity()).get(ActivityLogViewModel::class.java)
        _binding = FragmentActivityLogBinding.inflate(inflater, container, false)

        // ✅ Load data into the Activity Log
        activityLogViewModel.loadRecentBloodEntry(requireContext())
        activityLogViewModel.loadActivityEntries(requireContext())

        setupObservers()

        return binding.root
    }

    // ✅ Lifecycle Method: Cleanup on View Destroy
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ✅ Observe LiveData and update UI accordingly
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

    // ✅ Organizes activities into Recent & Upcoming sections
    private fun updateActivityTables(activities: List<ActivityLogEntry>) {
        val now = LocalTime.now()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")

        binding.recentActivitiesTable.removeAllViews()
        binding.upcomingActivitiesTable.removeAllViews()

        if (activities.isEmpty()) {
            showNoActivityMessage()
            return
        }

        val recentActivities = mutableListOf<ActivityLogEntry>()
        val upcomingActivities = mutableListOf<ActivityLogEntry>()

        // ✅ Sort activities into Recent or Upcoming
        for (entry in activities) {
            val startTime = LocalTime.parse(entry.startTime, formatter)
            if (startTime <= now) {
                recentActivities.add(entry)
            } else {
                upcomingActivities.add(entry)
            }
        }

        // ✅ Sort lists to display in order
        recentActivities.sortByDescending { LocalTime.parse(it.startTime, formatter) }
        upcomingActivities.sortBy { LocalTime.parse(it.startTime, formatter) }

        when {
            upcomingActivities.isEmpty() -> {
                val displayedRecent = recentActivities.take(7)
                binding.upcomingActivitiesLabel.visibility = View.GONE
                binding.upcomingActivitiesTable.visibility = View.GONE
                populateTable(binding.recentActivitiesTable, displayedRecent, "No recent activities")
            }
            upcomingActivities.size == 1 -> {
                val displayedRecent = recentActivities.take(4)
                binding.upcomingActivitiesLabel.visibility = View.VISIBLE
                binding.upcomingActivitiesTable.visibility = View.VISIBLE
                populateTable(binding.recentActivitiesTable, displayedRecent, "No recent activities")
                populateTable(binding.upcomingActivitiesTable, listOf(upcomingActivities.first()), "No upcoming activities")
            }
            else -> {
                val upcomingWithinHour = upcomingActivities.filter {
                    val startTime = LocalTime.parse(it.startTime, formatter)
                    startTime.isBefore(now.plusHours(1))
                }

                if (upcomingWithinHour.size >= 2) {
                    val displayedRecent = recentActivities.take(3)
                    val displayedUpcoming = upcomingWithinHour.take(2)
                    binding.upcomingActivitiesLabel.visibility = View.VISIBLE
                    binding.upcomingActivitiesTable.visibility = View.VISIBLE
                    populateTable(binding.recentActivitiesTable, displayedRecent, "No recent activities")
                    populateTable(binding.upcomingActivitiesTable, displayedUpcoming, "No upcoming activities")
                } else {
                    val displayedRecent = recentActivities.take(4)
                    val closestUpcoming = listOf(upcomingActivities.first())
                    binding.upcomingActivitiesLabel.visibility = View.VISIBLE
                    binding.upcomingActivitiesTable.visibility = View.VISIBLE
                    populateTable(binding.recentActivitiesTable, displayedRecent, "No recent activities")
                    populateTable(binding.upcomingActivitiesTable, closestUpcoming, "No upcoming activities")
                }
            }
        }
    }

    // ✅ Populates tables with activity data
    private fun populateTable(table: ViewGroup, activities: List<ActivityLogEntry>, emptyMessage: String) {
        if (activities.isEmpty()) {
            val noActivityText = TextView(requireContext()).apply {
                text = emptyMessage
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
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
                setTypeface(null, Typeface.BOLD)
                setPadding(8, 1, 8, 1)
            }

            val timeText = TextView(requireContext()).apply {
                text = if (entry.endTime != null) "${entry.startTime} - ${entry.endTime}" else entry.startTime
                textSize = 16f
                setTypeface(null, Typeface.BOLD)
                setPadding(8, 1, 8, 1)
            }

            val detailsText = TextView(requireContext()).apply {
                text = entry.description ?: "No details"
                textSize = 17f
                setTypeface(null, Typeface.BOLD)
                setPadding(8, 8, 8, 8)
            }

            row.addView(activityText)
            row.addView(timeText)
            row.addView(detailsText)
            table.addView(row)
        }
    }

    // ✅ Displays a message when there are no activities to show
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
