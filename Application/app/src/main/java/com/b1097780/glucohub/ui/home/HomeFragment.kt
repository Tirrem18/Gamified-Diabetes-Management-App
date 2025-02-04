package com.b1097780.glucohub.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.b1097780.glucohub.R
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
        setupButtonPopup() // Setup buttons to show popup

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

    private fun setupButtonPopup() {
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

    private fun setupGlucoseChart() {
        val lineChart: LineChart = binding.lineChart

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)
        val roundedCurrentHour = if (currentMinute > 0) currentHour + 1 else currentHour
        val startHour = (roundedCurrentHour - 5).let { if (it < 0) it + 24 else it }

        configureLineChart(lineChart, startHour, roundedCurrentHour)
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
        xAxis.labelCount = 6
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
                return String.format("%02d:00", hour)
            }
        }
    }

    private fun showNumberInputPopup() {
        val editText = EditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("Enter a Value")
            .setMessage("Please enter a number between 0 and 20:")
            .setView(editText)
            .setPositiveButton("OK", null) // Set null for now to validate input manually
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.setOnShowListener {
            val okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            okButton.setOnClickListener {
                val inputText = editText.text.toString()
                val number = inputText.toIntOrNull()

                if (number != null && number in 0..20) {
                    Toast.makeText(requireContext(), "You entered: $number", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    editText.error = "Enter a valid number (0-20)"
                }
            }
        }

        dialog.show()
    }


    private fun loadChartData(lineChart: LineChart, startHour: Int, endHour: Int) {
        val entries = ArrayList<Entry>()

        val sampleData = listOf(
            Pair(1.7f, 5.5f),
            Pair(2.5f, 6.0f),
            Pair(3.0f, 5.8f),
            Pair(4.25f, 6.3f),
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
            Pair(11.99f, 6f),
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

        var lastBeforeStart: Pair<Float, Float>? = null
        for (data in sampleData) {
            if (data.first < startHour) {
                lastBeforeStart = data
            } else {
                break
            }
        }

        lastBeforeStart?.let { (time, value) ->
            val xValue = 0f
            entries.add(Entry(xValue, value))
        }

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
        dataSet.color = android.graphics.Color.BLACK
        dataSet.setCircleColor(android.graphics.Color.WHITE)
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        lineChart.invalidate()
    }
}
