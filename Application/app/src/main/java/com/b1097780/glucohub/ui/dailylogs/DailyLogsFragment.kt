package com.b1097780.glucohub.ui.dailylogs

import CalendarFragment
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Find the selected date TextView
        val selectedDateTextView: TextView = root.findViewById(R.id.selected_date_text)

        // Apply theme color
        selectedDateTextView.setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))

        viewModel.selectedDate.observe(viewLifecycleOwner) { date ->
            Log.d("DailyLogsFragment", "Selected date changed: $date")
            updateSelectedDateUI(date, viewModel.hasData.value)
        }

        viewModel.hasData.observe(viewLifecycleOwner) { hasData ->
            Log.d("DailyLogsFragment", "Has Data changed: $hasData")
            updateSelectedDateUI(viewModel.selectedDate.value, hasData)
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

        if (date != null && selectedDateTextView != null) {
            selectedDateTextView.text = date

            if (hasData == true) {
                // Show data section
                dataContainer?.visibility = View.VISIBLE
                selectedDateTextView.text = date // Keep the date above the stats
                selectedDateTextView.gravity = Gravity.START // Left align

                // Set placeholders (replace with real values later)
                view?.findViewById<TextView>(R.id.text_total_activitys)?.text = "Activity Entries: 0"
                view?.findViewById<TextView>(R.id.text_total_glucose)?.text = "Glucose Entries: 0"
                view?.findViewById<TextView>(R.id.text_avg_glucose)?.text = "Average Glucose: 0 mg/dL"
                view?.findViewById<TextView>(R.id.text_time_in_range)?.text = "Time in Range: 0%"
                view?.findViewById<TextView>(R.id.text_highest_glucose)?.text = "Highest Glucose: 0 mg/dL"
                view?.findViewById<TextView>(R.id.text_lowest_glucose)?.text = "Lowest Glucose: 0 mg/dL"

            } else {
                // Hide data section and show centered "No data available"
                dataContainer?.visibility = View.GONE
                selectedDateTextView.text = "$date\n\nNo data available"
                selectedDateTextView.gravity = Gravity.CENTER
            }

            // Apply theme color
            selectedDateTextView.setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
        }
    }





    private fun getThemeColor(context: android.content.Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
