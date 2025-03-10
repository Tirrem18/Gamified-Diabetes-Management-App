import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarFragment : Fragment() {

    private lateinit var calendarContainer: LinearLayout
    private lateinit var calendar: Calendar
    private lateinit var monthYearTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize the root layout
        val rootLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(32, 32, 32, 32)
        }

        // Initialize the calendar instance
        calendar = Calendar.getInstance()

        // Layout for navigation buttons
        val navLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // Small Previous Month Button
        val prevButton = Button(requireContext()).apply {
            text = "<"
            textSize = 16f  // Smaller text
            setPadding(6, 6, 6, 18)  // Minimal padding
            setBackgroundColor(getThemeColor(requireContext(), android.R.attr.colorPrimary))
            setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
            layoutParams = LinearLayout.LayoutParams(90, 90)  // Small button size
            setOnClickListener {
                calendar.add(Calendar.MONTH, -1)
                populateCalendar()
            }
        }

        // Small Next Month Button
        val nextButton = Button(requireContext()).apply {
            text = ">"
            textSize = 16f
            setPadding(6, 6, 6, 18)
            setBackgroundColor(getThemeColor(requireContext(), android.R.attr.colorPrimary))
            setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
            layoutParams = LinearLayout.LayoutParams(90, 90)
            setOnClickListener {
                calendar.add(Calendar.MONTH, 1)
                populateCalendar()
            }
        }

        // Month-Year Title (Centered)
        monthYearTextView = TextView(requireContext()).apply {
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(12, 8, 12, 8)
            setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
        }

        // Add buttons and title to navigation layout
        navLayout.addView(prevButton)
        navLayout.addView(monthYearTextView)
        navLayout.addView(nextButton)

        // Add navigation layout to root
        rootLayout.addView(navLayout)

        // Create and add the calendar container
        calendarContainer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        rootLayout.addView(calendarContainer)

        // Populate the calendar initially
        populateCalendar()

        return rootLayout
    }



    private fun populateCalendar() {
        // Clear previous views
        calendarContainer.removeAllViews()

        // Update Month-Year
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        monthYearTextView.text = dateFormat.format(calendar.time)

        // Get month details
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfMonth = calendar.clone() as Calendar
        firstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1)
        val startDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK) - 1 // Convert to zero-based index

        // Create GridLayout with a border
        val gridLayout = GridLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            rowCount = 7
            columnCount = 7
            setPadding(2, 25, 2, 2) // Space for the border
            setBackgroundColor(Color.TRANSPARENT) // Border around the entire grid
        }

        // Add weekday headers (Sun, Mon, Tue, ...)
        val daysOfWeek = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        for (day in daysOfWeek) {
            val dayLabel = TextView(requireContext()).apply {
                text = day
                textSize = 15f
                gravity = Gravity.CENTER
                setPadding(8, 8, 8, 0)
                setTypeface(null, Typeface.BOLD)
                setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
                setBackgroundColor(getThemeColor(requireContext(), android.R.attr.colorPrimary))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = ViewGroup.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(1, 1, 1, 1) // Thin black outline around each cell
                }
            }
            gridLayout.addView(dayLabel)
        }

        // Add empty placeholders before the first day of the month
        for (i in 0 until startDayOfWeek) {
            val emptyView = TextView(requireContext()).apply {
                text = ""
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 50
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(1, 1, 1, 1) // Thin black outline
                }
            }
            gridLayout.addView(emptyView)
        }

        // Add buttons for each day of the month
        for (day in 1..daysInMonth) {
            val dayButton = Button(requireContext()).apply {
                text = day.toString()
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setPadding(6, 6, 6, 6) // Reduced padding
                setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
                setBackgroundColor(getThemeColor(requireContext(), android.R.attr.colorPrimary))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 60
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(1, 1, 1, 1) // Add black outline around each day box
                }
                setOnClickListener {
                    Toast.makeText(context, "Selected: $day", Toast.LENGTH_SHORT).show()
                }
            }
            gridLayout.addView(dayButton)
        }

        // Add GridLayout to container
        calendarContainer.addView(gridLayout)
    }



    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
