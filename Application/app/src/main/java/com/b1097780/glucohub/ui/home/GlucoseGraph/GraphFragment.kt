package com.b1097780.glucohub.ui.home.GlucoseGraph

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.MainActivity
import com.b1097780.glucohub.databinding.FragmentGraphBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*

class GraphFragment : Fragment() {

    private var _binding: FragmentGraphBinding? = null
    private val binding get() = _binding!!
    private lateinit var graphViewModel: GraphViewModel
    private lateinit var lineChart: LineChart
    private val handler = Handler(Looper.getMainLooper()) // Handler for periodic updates
    private val updateInterval = 60 * 1000L // 60 seconds (1 minute)

    private val updateGraphRunnable = object : Runnable {
        override fun run() {
            updateGraphTimeRange()
            handler.postDelayed(this, updateInterval) // Schedule next update
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        graphViewModel = ViewModelProvider(requireActivity()).get(GraphViewModel::class.java)
        _binding = FragmentGraphBinding.inflate(inflater, container, false)
        val root: View = binding.root

        lineChart = binding.lineChart
        setupGlucoseChart()
        setupObservers()

        // Start auto-refresh every minute
        handler.post(updateGraphRunnable)

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateGraphRunnable) // Stop updates when fragment is destroyed
        _binding = null
    }

    private fun setupObservers() {
        graphViewModel.glucoseEntries.observe(viewLifecycleOwner) { glucoseData ->
            loadChartData(glucoseData)
        }
    }

    private fun setupGlucoseChart() {
        updateGraphTimeRange()
    }

    private fun updateGraphTimeRange() {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        val isEarlyMorning = currentHour < 5  // ✅ True if it's before 5 AM
        val startHour = if (isEarlyMorning) {
            0  // ✅ Always start at 00:00 before 5 AM
        } else {
            currentHour - 5 // ✅ After 5 AM, push forward dynamically
        }

        val roundedCurrentHour = if (currentMinute > 0) currentHour + 1 else currentHour

        configureLineChart(lineChart, startHour, roundedCurrentHour)

        // 🔥 Load data from ViewModel (ensures updates)
        (activity as? MainActivity)?.let { mainActivity ->
            graphViewModel.loadGlucoseEntries(mainActivity)
        }

        lineChart.invalidate() // Refresh chart
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

        // ✅ Ensure proper time display before and after 5 AM
        if (startHour == 0) {
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = 5f // ✅ Display 00:00 - 05:00 before 5 AM
        } else {
            xAxis.axisMinimum = 0f
            xAxis.axisMaximum = (endHour - startHour).toFloat() // ✅ Shift dynamically after 5 AM
        }

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

    private fun loadChartData(entries: List<Entry>) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val isEarlyMorning = currentHour < 5 // Before 5 AM
        val startHour = if (isEarlyMorning) 0 else currentHour - 5
        val endHour = currentHour + 1 // 🔥 Ensure next hour is included!

        // Filtering logic: Include the next hour
        val filteredEntries = if (isEarlyMorning) {
            entries // ✅ Keep all values from 00:00 before 5 AM
        } else {
            entries.filter { it.x in startHour.toFloat()..endHour.toFloat() }
        }

        val adjustedEntries = filteredEntries.map {
            val adjustedX = if (isEarlyMorning) it.x else it.x - startHour
            Entry(adjustedX, it.y)
        }.toMutableList()


        // Setup dataset
        val dataSet = LineDataSet(adjustedEntries, "Glucose Levels")
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 4.5f
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 1.5f
        dataSet.color = Color.BLACK
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curves

        // Color-code data points based on glucose levels
        val circleColors = adjustedEntries.map {
            when {
                it.y <= 3.9 -> Color.RED    // 🔴 Low Blood Sugar
                it.y >= 10 -> Color.YELLOW  // 🟡 High Blood Sugar
                else -> Color.WHITE         // ⚪ Normal Range
            }
        }
        dataSet.circleColors = circleColors

        // Set data and refresh chart
        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // Refresh chart
    }





}
