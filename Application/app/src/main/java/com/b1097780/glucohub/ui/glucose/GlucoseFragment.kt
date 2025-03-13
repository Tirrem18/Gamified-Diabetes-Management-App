package com.b1097780.glucohub.ui.glucose

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.PreferencesHelper
import com.b1097780.glucohub.R
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.text.SimpleDateFormat
import java.util.*

class GlucoseFragment : Fragment() {

    private lateinit var viewModel: GlucoseViewModel
    private lateinit var barChart: BarChart

    private lateinit var textAverageGlucose: TextView
    private lateinit var textSelectedRange: TextView
    private lateinit var textTotalEntries: TextView
    private lateinit var textTimeInRange: TextView

    private lateinit var btnDay: Button
    private lateinit var btnWeek: Button
    private lateinit var btnMonth: Button
    private lateinit var btnYear: Button
    private lateinit var buttons: List<Button>



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View {

        val root = inflater.inflate(R.layout.fragment_glucose, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[GlucoseViewModel::class.java]
        barChart = root.findViewById(R.id.bar_chart_time_in_range)



        // Initialize UI Elements
        textAverageGlucose = root.findViewById(R.id.text_average_glucose)
        textSelectedRange = root.findViewById(R.id.text_selected_range)
        textTotalEntries = root.findViewById(R.id.text_total_entries)
        textTimeInRange = root.findViewById(R.id.text_time_in_range)

        btnDay = root.findViewById(R.id.btn_day)
        btnWeek = root.findViewById(R.id.btn_week)
        btnMonth = root.findViewById(R.id.btn_month)
        btnYear = root.findViewById(R.id.btn_year)

        buttons = listOf(btnDay, btnWeek, btnMonth, btnYear)

        buttons.forEach { styleButton(it) }

        // Observe LiveData updates
        viewModel.selectedRange.observe(viewLifecycleOwner) { textSelectedRange.text = it }
        viewModel.averageGlucose.observe(viewLifecycleOwner) { textAverageGlucose.text = it }
        viewModel.totalEntries.observe(viewLifecycleOwner) { textTotalEntries.text = it }
        viewModel.timeInRange.observe(viewLifecycleOwner) { textTimeInRange.text = it }

        // Default selection
        updateDateRange(1, btnDay)



        btnDay.setOnClickListener { updateDateRange(1, btnDay) }
        btnWeek.setOnClickListener { updateDateRange(7, btnWeek) }
        btnMonth.setOnClickListener { updateDateRange(30, btnMonth) }
        btnYear.setOnClickListener { updateDateRange(365, btnYear) }

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)

                val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                calculateGlucoseStatistics(today, today) // Directly call function instead of clicking
            }
        })


        return root
    }

    private fun updateDateRange(days: Int, selectedButton: Button) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val today = dateFormat.format(calendar.time)
        val displayToday = displayFormat.format(calendar.time)

        val startDate: String
        val rangeText: String

        if (days == 1) {
            startDate = today
            rangeText = displayToday
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, -days + 1)
            startDate = dateFormat.format(calendar.time)
            val displayStartDate = displayFormat.format(calendar.time)
            rangeText = "$displayStartDate - $displayToday"
        }

        viewModel.setSelectedRange(rangeText)
        calculateGlucoseStatistics(startDate, today)
        updateButtonStyles(selectedButton)
    }


    private fun updateButtonStyles(selectedButton: Button) {
        buttons.forEach { button ->
            setSelectedStyle(button, button == selectedButton)
        }
    }

    private fun setSelectedStyle(button: Button, isSelected: Boolean) {
        if (isSelected) {
            button.setBackgroundColor(getThemeColor(requireContext(), R.attr.colorPrimaryVariant))
            button.setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
        } else {
            button.setBackgroundColor(getThemeColor(requireContext(), android.R.attr.colorPrimary))
            button.setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
        }
    }

    private fun styleButton(button: Button) {
        button.setPadding(20, 10, 20, 10)
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        button.setTypeface(null, android.graphics.Typeface.BOLD)
        button.setBackgroundColor(getThemeColor(requireContext(), android.R.attr.colorPrimary))
        button.setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
    }

    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    private fun calculateGlucoseStatistics(startDate: String, endDate: String) {
        val glucoseData = PreferencesHelper.getGlucoseEntriesBetweenDates(requireContext(), startDate, endDate)
        val allGlucoseValues = glucoseData.values.flatten().map { it.second }
        val mainContent = view?.findViewById<View>(R.id.bar_chart_time_in_range)

        val labels = listOf(
            R.id.text_percent_very_high,
            R.id.text_percent_high,
            R.id.text_percent_in_range,
            R.id.text_percent_low,
            R.id.text_percent_very_low,
            R.id.text_time_very_high,
            R.id.text_time_high,
            R.id.text_time_in_range,
            R.id.text_time_low,
            R.id.text_time_very_low
        )

        if (allGlucoseValues.isNotEmpty()) {
            val average = allGlucoseValues.average()
            val minGlucose = allGlucoseValues.minOrNull() ?: 0f
            val maxGlucose = allGlucoseValues.maxOrNull() ?: 0f
            val totalEntries = allGlucoseValues.size
            val veryLow = allGlucoseValues.count { it <= 2.5 } * 100f / totalEntries
            val low = allGlucoseValues.count { it in 2.6..3.9 } * 100f / totalEntries
            val inRange = allGlucoseValues.count { it in 4.0..10.0 } * 100f / totalEntries
            val high = allGlucoseValues.count { it in 10.1..20.0 } * 100f / totalEntries
            val veryHigh = allGlucoseValues.count { it > 20.0 } * 100f / totalEntries

            requireActivity().runOnUiThread {
                // Show the UI elements
                mainContent?.visibility = View.VISIBLE


                // Show all labels
                labels.forEach { id ->
                    view?.findViewById<View>(id)?.visibility = View.VISIBLE
                }

                view?.findViewById<TextView>(R.id.text_percent_very_high)?.apply {
                    text = "%.1f%%".format(veryHigh)
                    setTextColor(Color.parseColor("#FF4500")) // Dark Orange
                }

                view?.findViewById<TextView>(R.id.text_percent_high)?.apply {
                    text = "%.1f%%".format(high)
                    setTextColor(Color.parseColor("#FFA500")) // Orange
                }

                view?.findViewById<TextView>(R.id.text_percent_in_range)?.apply {
                    text = "%.1f%%".format(inRange)
                    setTextColor(Color.parseColor("#32CD32")) // Green
                }

                view?.findViewById<TextView>(R.id.text_percent_low)?.apply {
                    text = "%.1f%%".format(low)
                    setTextColor(Color.parseColor("#FF6347")) // Lighter Red
                }

                view?.findViewById<TextView>(R.id.text_percent_very_low)?.apply {
                    text = "%.1f%%".format(veryLow)
                    setTextColor(Color.parseColor("#cb0700")) // Dark Red
                }
            }

            viewModel.setAverageGlucose("Avg: %.2f mg/dL".format(average))
            viewModel.setTotalEntries("Total Entries: $totalEntries")

            updateBarChart(veryLow, low, inRange, high, veryHigh)

        } else {
            println("No glucose data available.")

            requireActivity().runOnUiThread {
                // Hide all UI elements
                mainContent?.visibility = View.GONE

                // Hide all labels
                labels.forEach { id ->
                    view?.findViewById<View>(id)?.visibility = View.GONE
                }
            }

            viewModel.setAverageGlucose("No Data")
            viewModel.setTotalEntries("Total Entries: 0")
            viewModel.setTimeInRange("Time in Range: --")

            updateBarChart(0f, 0f, 0f, 0f, 0f) // Empty bar chart
        }
    }




    private fun updateBarChart(veryLow: Float, low: Float, inRange: Float, high: Float, veryHigh: Float) {
        if (!::barChart.isInitialized) return // Prevents crashes if not initialized

        val total = veryLow + low + inRange + high + veryHigh

        println("Updating chart: Very Low = $veryLow%, Low = $low%, In Range = $inRange%, High = $high%, Very High = $veryHigh%")

        // If total is 0, show placeholder data
        val entries = if (total > 0) {
            listOf(BarEntry(1f, floatArrayOf(veryLow, low, inRange, high, veryHigh)))
        } else {
            listOf(BarEntry(1f, floatArrayOf(0f, 0f, 0f, 0f, 0f))) // Show empty bar
        }

        val dataSet = BarDataSet(entries, "Glucose Distribution").apply {
            colors = listOf(
                Color.parseColor("#cb0700"), // Very Low (Dark Red)
                Color.parseColor("#FF6347"), // Low (Lighter Red)
                Color.parseColor("#32CD32"), // In Range (Green)
                Color.parseColor("#FFA500"), // High (Orange)
                Color.parseColor("#FF4500")  // Very High (Dark Orange)
            )
            setDrawValues(false) // Remove numbers inside the chart
        }


        val barData = BarData(dataSet)
        barChart.data = barData

        // ✅ Remove background grid and axis labels
        barChart.axisLeft.setDrawGridLines(false)
        barChart.axisRight.setDrawGridLines(false)
        barChart.xAxis.setDrawGridLines(false)

        // ✅ Remove Y-axis labels (0-100)
        barChart.axisLeft.setDrawLabels(false)
        barChart.axisRight.setDrawLabels(false)

        // ✅ Hide Y-axis and X-axis lines
        barChart.axisLeft.setDrawAxisLine(false)
        barChart.axisRight.setDrawAxisLine(false)
        barChart.xAxis.setDrawAxisLine(false)
        barChart.xAxis.setDrawLabels(false)

        // ✅ Disable chart description
        barChart.description.isEnabled = false

        // ✅ Hide legend (optional)
        barChart.legend.isEnabled = false

        barChart.setFitBars(true)
        barChart.invalidate()
    }




}
