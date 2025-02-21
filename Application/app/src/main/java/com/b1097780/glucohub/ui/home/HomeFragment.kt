package com.b1097780.glucohub.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.databinding.FragmentHomeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.animation.AnimationUtils
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.b1097780.glucohub.R
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        lineChart = binding.lineChart
        setupGlucoseChart()
        setupButtons()
        setupObservers()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        homeViewModel.glucoseText.observe(viewLifecycleOwner) {
            binding.textGlucose.text = it
        }

        homeViewModel.plannerText.observe(viewLifecycleOwner) {
            binding.textPlanner.text = it
        }

        // Observe Glucose Data and update the chart
        homeViewModel.glucoseEntries.observe(viewLifecycleOwner) { glucoseData ->
            loadChartData(glucoseData)
        }
    }

    private fun setupGlucoseChart() {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        val roundedCurrentHour = if (currentMinute > 0) currentHour + 1 else currentHour
        val startHour = (roundedCurrentHour - 5).let { if (it < 0) it + 24 else it }

        configureLineChart(lineChart, startHour, roundedCurrentHour)
    }

    private fun configureLineChart(lineChart: LineChart, startHour: Int, endHour: Int) {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(false)
        lineChart.setPinchZoom(false)
        lineChart.setBackgroundColor(Color.TRANSPARENT)

        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = 6
        xAxis.axisMinimum = 0f // ✅ Start from 0 (mapped to `startHour`)
        xAxis.axisMaximum = 5f // ✅ End at 5 (mapped to `roundedCurrentHour`)
        xAxis.valueFormatter = getTimeValueFormatter(startHour)
        xAxis.textColor = Color.BLACK

        val leftAxis: YAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 20f
        leftAxis.granularity = 4f
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = Color.BLACK

        val rightAxis: YAxis = lineChart.axisRight
        rightAxis.isEnabled = false
    }


    private fun getTimeValueFormatter(startHour: Int): ValueFormatter {
        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val hour = (startHour + value.toInt()) % 24
                return String.format("%02d:00", hour)  // Format as HH:00
            }
        }
    }


    private fun loadChartData(entries: List<Entry>) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        val roundedCurrentHour = if (currentMinute > 0) currentHour + 1 else currentHour
        val startHour = (roundedCurrentHour - 5).let { if (it < 0) it + 24 else it } // Ensure 24-hour format

        // Filter to include only entries within the last 5 hours
        val filteredEntries = entries.filter { it.x in startHour.toFloat()..roundedCurrentHour.toFloat() }

        // Find if there’s a data point exactly at `startHour` or within `0.05`
        val exactOrCloseMatch = filteredEntries.any { Math.abs(it.x - startHour) <= 0.05 }

        // Find the last value before `startHour`
        var lastBeforeStart: Entry? = null
        for (entry in entries) {
            if (entry.x < startHour) {
                lastBeforeStart = entry // Keep updating until we find the last before `startHour`
            } else {
                break
            }
        }

        val adjustedEntries = mutableListOf<Entry>()

        // If there's no exact match and lastBeforeStart is within 20 minutes (0.33 hours), add it
        if (!exactOrCloseMatch && lastBeforeStart != null && (startHour - lastBeforeStart.x) <= 0.33) {
            adjustedEntries.add(Entry(0f, lastBeforeStart.y))
        }

        // Adjust X values so that the first entry starts at `0`
        adjustedEntries.addAll(filteredEntries.map { Entry(it.x - startHour, it.y) })

        val dataSet = LineDataSet(adjustedEntries, "Glucose Levels")
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 2f
        dataSet.color = Color.BLACK
        dataSet.setCircleColor(Color.WHITE)
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // Refresh chart
    }

    private fun setupButtons() {
        val button1 = binding.button1
        val button2 = binding.button2
        val shrinkAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button)

        val showPopup = {
            AlertDialog.Builder(requireContext())
                .setTitle("Notification")
                .setMessage("Button Clicked")
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        button1.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper()).postDelayed({
                showNumberInputPopup()
            }, 200)
        }

        button2.setOnClickListener {
            it.startAnimation(shrinkAnimation)
            Handler(Looper.getMainLooper()).postDelayed({ showPopup() }, 200)
        }
    }

    private fun showNumberInputPopup() {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("Enter Glucose Level")
            .setMessage("Enter a value between 0 and 20:")
            .setView(editText)
            .setPositiveButton("OK", null) // Set null to manually validate input
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val inputText = editText.text.toString()
                val number = inputText.toFloatOrNull()

                if (number != null && number in 0f..20f) {
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toFloat() +
                            Calendar.getInstance().get(Calendar.MINUTE) / 60f // Convert minutes to float

                    homeViewModel.addGlucoseEntry(currentHour, number) // ✅ Add to ViewModel
                    dialog.dismiss()
                } else {
                    editText.error = "Enter a valid number (0-20)"
                }
            }
        }

        dialog.show()
    }




}
