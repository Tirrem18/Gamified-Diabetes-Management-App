package com.b1097780.glucohub.ui.glucose

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.b1097780.glucohub.R
import java.text.SimpleDateFormat
import java.util.*

class GlucoseFragment : Fragment() {

    private lateinit var textAverageGlucose: TextView
    private lateinit var textSelectedRange: TextView
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

        // Initialize UI Elements
        textAverageGlucose = root.findViewById(R.id.text_average_glucose)
        textSelectedRange = root.findViewById(R.id.text_selected_range)
        btnDay = root.findViewById(R.id.btn_day)
        btnWeek = root.findViewById(R.id.btn_week)
        btnMonth = root.findViewById(R.id.btn_month)
        btnYear = root.findViewById(R.id.btn_year)

        buttons = listOf(btnDay, btnWeek, btnMonth, btnYear)

        // Apply default button styles
        buttons.forEach { styleButton(it) }

        // Set default selection to "Day"
        updateDateRange(1, btnDay)

        // Button Click Listeners
        btnDay.setOnClickListener { updateDateRange(1, btnDay) }
        btnWeek.setOnClickListener { updateDateRange(7, btnWeek) }
        btnMonth.setOnClickListener { updateDateRange(30, btnMonth) }
        btnYear.setOnClickListener { updateDateRange(365, btnYear) }

        return root
    }

    private fun updateDateRange(days: Int, selectedButton: Button) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val today = dateFormat.format(calendar.time)

        val rangeText = if (days == 1) {
            today // Show today's date
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, -days + 1)
            val startDate = dateFormat.format(calendar.time)
            "$startDate - $today"
        }

        textSelectedRange.text = rangeText

        // Update button styles
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
}
