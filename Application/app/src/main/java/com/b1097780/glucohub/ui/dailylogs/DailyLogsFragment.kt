package com.b1097780.glucohub.ui.dailylogs

import CalendarFragment
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.b1097780.glucohub.R

class DailyLogsFragment : Fragment() {

    private val viewModel: DailyLogsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_daily_logs, container, false)

        // Find UI elements
        val selectedDateTextView: TextView = root.findViewById(R.id.selected_date_text)
        val textTotalEntries: TextView = root.findViewById(R.id.text_total_glucose)
        val textAverageGlucose: TextView = root.findViewById(R.id.text_avg_glucose)
        val textTimeInRange: TextView = root.findViewById(R.id.text_time_in_range)
        val textTotalActivities: TextView = root.findViewById(R.id.text_total_activitys)
        val textHighestGlucose: TextView = root.findViewById(R.id.text_highest_glucose)
        val textLowestGlucose: TextView = root.findViewById(R.id.text_lowest_glucose)

        val buttonViewAllEntries: Button = root.findViewById(R.id.button_view_all_entries)
        val buttonDeleteEntry: Button = root.findViewById(R.id.button_delete_entry)

        // Apply theme color
        selectedDateTextView.setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))

        // ✅ Observe formatted date and update UI
        viewModel.formattedDate.observe(viewLifecycleOwner) { formattedDate ->
            selectedDateTextView.text = formattedDate
            requireContext().let { context ->
                viewModel.fetchGlucoseAndActivityData(context, viewModel.selectedDate.value ?: "")
            }
        }

        // ✅ Observe hasData status
        viewModel.hasData.observe(viewLifecycleOwner) { hasData ->
            updateSelectedDateUI(viewModel.formattedDate.value, hasData)
        }

        // ✅ Observe glucose and activity data
        viewModel.averageGlucose.observe(viewLifecycleOwner) { textAverageGlucose.text = it }
        viewModel.totalEntries.observe(viewLifecycleOwner) { textTotalEntries.text = it }
        viewModel.timeInRange.observe(viewLifecycleOwner) { textTimeInRange.text = it }
        viewModel.totalActivityEntries.observe(viewLifecycleOwner) { textTotalActivities.text = it }
        viewModel.highestGlucose.observe(viewLifecycleOwner) { textHighestGlucose.text = it }
        viewModel.lowestGlucose.observe(viewLifecycleOwner) { textLowestGlucose.text = it }

        // ✅ Handle button clicks
        buttonViewAllEntries.setOnClickListener {
            Log.d("DailyLogsFragment", "View All Entries clicked")
            // TODO: Implement logic to show all glucose entries
        }

        buttonDeleteEntry.setOnClickListener {
            Log.d("DailyLogsFragment", "Delete Entry clicked")
            // TODO: Implement logic to delete last entry
        }

        // Load CalendarFragment dynamically
        childFragmentManager.beginTransaction()
            .replace(R.id.calendar_fragment_container, CalendarFragment())
            .commit()

        return root
    }

    private fun updateSelectedDateUI(date: String?, hasData: Boolean?) {
        val selectedDateTextView: TextView? = view?.findViewById(R.id.selected_date_text)
        val dataContainer: LinearLayout? = view?.findViewById(R.id.data_container)

        Log.d("DailyLogsFragment", "Checking hasData: $hasData for date: $date")

        if (date != null && selectedDateTextView != null) {
            selectedDateTextView.text = date

            if (hasData == true) {
                Log.d("DailyLogsFragment", "Data found, showing UI elements")
                dataContainer?.visibility = View.VISIBLE
                selectedDateTextView.gravity = Gravity.START
            } else {
                Log.d("DailyLogsFragment", "No data available, updating UI")
                dataContainer?.visibility = View.GONE
                selectedDateTextView.text = "$date\n\nNo data available"
                selectedDateTextView.gravity = Gravity.CENTER

                // ✅ Ensure UI update persists
                selectedDateTextView.postDelayed({
                    if (hasData == false) { // Double-check after delay
                        selectedDateTextView.text = "$date\n\nNo data available"
                        Log.d("DailyLogsFragment", "Final UI confirmation: No data available shown")
                    }
                }, 500)
            }

            selectedDateTextView.setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
        }
    }

    private fun getThemeColor(context: android.content.Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
