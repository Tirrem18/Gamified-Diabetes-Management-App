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
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        val roundedCurrentHour = if (currentMinute > 0) currentHour + 1 else currentHour
        val startHour = (roundedCurrentHour - 5).let { if (it < 0) it + 24 else it }

        configureLineChart(lineChart, startHour, roundedCurrentHour)
        lineChart.invalidate() // Refresh chart with new time range
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
        dataSet.circleRadius = 4.5f
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 1.5f
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
