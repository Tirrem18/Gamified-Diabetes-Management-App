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
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 5f
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
                return String.format("%02d:00", hour)
            }
        }
    }

    private fun setupButtons() {
        binding.button1.setOnClickListener {
            it.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button))
            Handler(Looper.getMainLooper()).postDelayed({
                showNumberInputPopup()
            }, 200)
        }
    }

    private fun showNumberInputPopup() {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_CLASS_NUMBER
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("Enter Glucose Level")
            .setMessage("Please enter your current glucose level:")
            .setView(editText)
            .setPositiveButton("OK", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val inputText = editText.text.toString()
                val number = inputText.toFloatOrNull()

                if (number == null || number < 0f || number > 40f) {
                    editText.error = "This number is too high. Please enter a value below 40."
                    return@setOnClickListener
                }

                val currentTime = getCurrentTime()

                when {
                    number > 25f -> {
                        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                            .setTitle("High Glucose Alert")
                            .setMessage("A glucose level above 25 is dangerously high. Are you sure?")
                            .setPositiveButton("Confirm") { _, _ ->
                                homeViewModel.addGlucoseEntry(currentTime, number)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { warnDialog, _ -> warnDialog.dismiss() }
                            .show()
                    }
                    number < 2f -> {
                        AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                            .setTitle("Low Glucose Warning")
                            .setMessage("A glucose level below 2 is critically low. Are you sure?")
                            .setPositiveButton("Confirm") { _, _ ->
                                homeViewModel.addGlucoseEntry(currentTime, number)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { warnDialog, _ -> warnDialog.dismiss() }
                            .show()
                    }
                    else -> {
                        homeViewModel.addGlucoseEntry(currentTime, number)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun getCurrentTime(): Float {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toFloat()
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE) / 60f
        return currentHour + currentMinute
    }

    private fun loadChartData(entries: List<Entry>) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        val roundedCurrentHour = if (currentMinute > 0) currentHour + 1 else currentHour
        val startHour = (roundedCurrentHour - 5).let { if (it < 0) it + 24 else it }

        val filteredEntries = entries.filter { it.x in startHour.toFloat()..roundedCurrentHour.toFloat() }
        val exactOrCloseMatch = filteredEntries.any { Math.abs(it.x - startHour) <= 0.05 }

        var lastBeforeStart: Entry? = null
        for (entry in entries) {
            if (entry.x < startHour) {
                lastBeforeStart = entry
            } else {
                break
            }
        }

        val adjustedEntries = mutableListOf<Entry>()

        if (!exactOrCloseMatch && lastBeforeStart != null && (startHour - lastBeforeStart.x) <= 0.33) {
            adjustedEntries.add(Entry(0f, lastBeforeStart.y))
        }

        adjustedEntries.addAll(filteredEntries.map { Entry(it.x - startHour, if (it.y > 20f) 20f else it.y) })

        val dataSet = LineDataSet(adjustedEntries, "Glucose Levels")
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 2f
        dataSet.color = Color.BLACK
        dataSet.mode = LineDataSet.Mode.HORIZONTAL_BEZIER

        val circleColors = adjustedEntries.map {
            when {
                it.y <= 3.9 -> Color.RED    // 🔴 Low Blood Sugar
                it.y >= 10 -> Color.YELLOW  // 🟡 High Blood Sugar
                else -> Color.WHITE         // ⚪ Normal
            }
        }
        dataSet.circleColors = circleColors

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // Refresh chart
    }

}
