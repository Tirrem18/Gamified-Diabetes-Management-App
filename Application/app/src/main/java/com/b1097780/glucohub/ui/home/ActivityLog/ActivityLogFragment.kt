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
        activityLogViewModel = ViewModelProvider(requireActivity()).get(ActivityLogViewModel::class.java)
        _binding = FragmentActivityLogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Load latest blood glucose data
        activityLogViewModel.loadRecentBloodEntry(requireContext())

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
            updateActivityTable(activities)
        }
    }

    private fun updateActivityTable(activities: List<ActivityLogEntry>) {
        val table = binding.activityLogTable
        table.removeAllViews() // Clear previous entries

        for (entry in activities) {
            val row = TableRow(requireContext())

            val activityText = TextView(requireContext()).apply {
                text = entry.activity
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }

            val timeText = TextView(requireContext()).apply {
                text = entry.time
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }

            val detailsText = TextView(requireContext()).apply {
                text = entry.details
                textSize = 16f
                setPadding(8, 8, 8, 8)
            }

            row.addView(activityText)
            row.addView(timeText)
            row.addView(detailsText)

            table.addView(row)
        }
    }
}
