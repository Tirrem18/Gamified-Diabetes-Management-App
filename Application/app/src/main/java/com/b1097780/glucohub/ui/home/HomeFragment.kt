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
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupObservers(homeViewModel)
        setupGlucoseChart()



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers(homeViewModel: HomeViewModel) {
        homeViewModel.glucoseText.observe(viewLifecycleOwner) {
            binding.textGlucose.text = it
        }
        homeViewModel.plannerText.observe(viewLifecycleOwner) {
            binding.textPlanner.text = it
        }

    }

    private fun setupGlucoseChart() {
        val lineChart: LineChart = binding.lineChart

        // Get current time and calculate the starting hour for the X-axis
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        val roundedCurrentHour = if (currentMinute > 0) currentHour + 1 else currentHour
        val startHour = (roundedCurrentHour - 5).let { if (it < 0) it + 24 else it }

        // Configure chart with startHour
        configureLineChart(lineChart, startHour, roundedCurrentHour)

        // Load data into the chart using startHour
        loadChartData(lineChart, startHour, roundedCurrentHour)
    }

    private fun configureLineChart(lineChart: LineChart, startHour: Int, endHour: Int) {
        lineChart.description.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.setScaleEnabled(false)
        lineChart.setPinchZoom(false)
        lineChart.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        val xAxis: XAxis = lineChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = 6 // Ensures only 6 labels show
        xAxis.axisMinimum = 0f
        xAxis.axisMaximum = 5f
        xAxis.valueFormatter = getTimeValueFormatter(startHour)
        xAxis.textColor = android.graphics.Color.BLACK

        val leftAxis: YAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 20f
        leftAxis.granularity = 4f
        leftAxis.setDrawGridLines(true)
        leftAxis.textColor = android.graphics.Color.BLACK

        val rightAxis: YAxis = lineChart.axisRight
        rightAxis.isEnabled = false
    }

    private fun getTimeValueFormatter(startHour: Int): ValueFormatter {
        return object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val hour = (startHour + value.toInt()) % 24
                return String.format("%02d:00", hour) // Ensures labels like 07:00, 08:00, 09:00
            }
        }
    }

    private fun loadChartData(lineChart: LineChart, startHour: Int, endHour: Int) {
        val entries = ArrayList<Entry>()

        // Sample glucose data (Actual time in hours.fraction, Glucose level)
        val sampleData = listOf(
            Pair(1.7f, 5.5f),
            Pair(2.5f, 6.0f),
            Pair(3.0f, 5.8f),
            Pair(4.25f, 6.3f), // Last known point before 5:00
            Pair(5.5f, 5.9f),
            Pair(5.7f, 5.9f),
            Pair(5.95f, 6.5f),
            Pair(6.25f, 7.9f),
            Pair(6.75f, 11f),
            Pair(7.5f, 14f),
            Pair(7.75f, 12.5f),
            Pair(8.0f, 5.8f),
            Pair(8.50f, 4f),
            Pair(9.25f, 6.3f),
            Pair(9.85f, 7.4f),
            Pair(10.01f, 7.9f),
            Pair(10.45f, 7.4f),
            Pair(10.95f, 7.4f),
            Pair(11.5f, 5.9f),
            Pair(11.9f, 4.9f),
            Pair(12.5f, 6.0f),
            Pair(13.0f, 5.8f),
            Pair(14.25f, 6.3f),
            Pair(15.5f, 5.9f),
            Pair(15.7f, 5.9f),
            Pair(15.95f, 6.5f),
            Pair(16.25f, 7.9f),
            Pair(16.75f, 11f),
            Pair(17.5f, 14f),
            Pair(17.75f, 12.5f),
            Pair(18.0f, 5.8f),
            Pair(18.50f, 4f),
            Pair(19.25f, 6.3f),
            Pair(19.85f, 7.4f),
            Pair(21.5f, 5.9f)
        )

        // Find the last data point before startHour (5:00)
        var lastBeforeStart: Pair<Float, Float>? = null
        for (data in sampleData) {
            if (data.first < startHour) {
                lastBeforeStart = data
            } else {
                break
            }
        }

        // If there's a valid previous point, interpolate a point at startHour (5:00)
        lastBeforeStart?.let { (time, value) ->
            val interpolatedValue = value // Using the last known value directly
            val xValue = 0f // Start of the graph
            entries.add(Entry(xValue, interpolatedValue))
        }

        // Convert to X-Axis values (0-5 range)
        for ((time, value) in sampleData) {
            if (time >= startHour && time <= endHour) {
                val xValue = ((time - startHour) / (endHour - startHour)) * 5f
                entries.add(Entry(xValue, value))
            }
        }

        val dataSet = LineDataSet(entries, "Glucose Levels")
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 4f
        dataSet.setDrawValues(false)
        dataSet.lineWidth = 2f
        dataSet.color = android.graphics.Color.BLACK // Black Line
        dataSet.setCircleColor(android.graphics.Color.WHITE) // White Dots

        // Enable **Curved Lines**
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate() // Refresh
    }
}
