import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.b1097780.glucohub.PreferencesHelper
import com.b1097780.glucohub.R
import com.b1097780.glucohub.ui.dailylogs.DailyLogsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CalendarFragment : Fragment() {

    private lateinit var calendarContainer: LinearLayout
    private lateinit var calendar: Calendar
    private lateinit var monthYearTextView: TextView
    private var selectedButton: Button? = null // Track the selected button
    private val viewModel: DailyLogsViewModel by activityViewModels()


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
        // Use ConstraintLayout for better positioning
        val navLayout = ConstraintLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

// Create Previous Month Button (Fixed Left)
        val prevButton = Button(requireContext()).apply {
            text = "<"
            textSize = 20f
            setPadding(6, 6, 6, 6)  // Reduce padding for smaller button
            id = View.generateViewId() // Unique ID for ConstraintLayout
            setBackgroundColor(getThemeColor(requireContext(), android.R.attr.colorPrimary))
            setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))

            setOnClickListener {
                val minCalendar = Calendar.getInstance().apply { set(2024, Calendar.FEBRUARY, 1) } // Set minimum to Jan 2024

                if (calendar.after(minCalendar)) { // Only go back if it's after Jan 2024
                    it.isEnabled = false
                    it.postDelayed({
                        calendar.add(Calendar.MONTH, -1)
                        populateCalendar()
                        it.isEnabled = true
                    }, 100)
                } else {
                    Toast.makeText(context, "Cannot go before January 2024", Toast.LENGTH_SHORT).show()
                }
            }


        }

// Create Next Month Button (Fixed Right)
        val nextButton = Button(requireContext()).apply {
            text = ">"
            textSize = 20f
            setPadding(6, 6, 6, 6)  // Reduce padding for smaller button
            id = View.generateViewId() // Unique ID for ConstraintLayout
            setBackgroundColor(getThemeColor(requireContext(), android.R.attr.colorPrimary))
            setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))


            setOnClickListener {
                val maxCalendar = Calendar.getInstance().apply { add(Calendar.MONTH, 1) } // Set max to 1 year from today

                if (calendar.before(maxCalendar)) { // Only go forward if within the limit
                    it.isEnabled = false
                    it.postDelayed({
                        calendar.add(Calendar.MONTH, 1)
                        populateCalendar()
                        it.isEnabled = true
                    }, 100)
                } else {
                    Toast.makeText(context, "Cannot go beyond this point", Toast.LENGTH_SHORT).show()
                }
            }


        }

// Create Month-Year Title (Centered)
        monthYearTextView = TextView(requireContext()).apply {
            textSize = 20f
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(12, 8, 12, 8)
            setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))
            id = View.generateViewId() // Unique ID for ConstraintLayout
        }

// Add Views to ConstraintLayout
        navLayout.addView(prevButton)
        navLayout.addView(monthYearTextView)
        navLayout.addView(nextButton)

// Set Constraints for Positioning
        val constraintSet = ConstraintSet()
        constraintSet.clone(navLayout)

        constraintSet.connect(prevButton.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        constraintSet.connect(prevButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
        constraintSet.constrainWidth(prevButton.id, 120) // Force fixed width (Change if needed)
        constraintSet.constrainHeight(prevButton.id, 95) // Keep height small

        constraintSet.connect(nextButton.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)
        constraintSet.connect(nextButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)

        constraintSet.constrainWidth(nextButton.id, 120)
        constraintSet.constrainHeight(nextButton.id, 95)

        constraintSet.connect(monthYearTextView.id, ConstraintSet.START, prevButton.id, ConstraintSet.END, 32)
        constraintSet.connect(monthYearTextView.id, ConstraintSet.END, nextButton.id, ConstraintSet.START, 32)
        constraintSet.connect(monthYearTextView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
        constraintSet.constrainWidth(monthYearTextView.id, ConstraintSet.WRAP_CONTENT)

        constraintSet.applyTo(navLayout)

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
            setPadding(2, 35, 2, 2) // Space for the border
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
                setBackgroundColor(getThemeColor(requireContext(), R.attr.colorPrimaryVariant))
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
            val dateKey = String.format("%04d%02d%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, day)


            val hasData = hasDataForDate(requireContext(), dateKey) // Check SharedPreferences



            val dayButton = Button(requireContext()).apply {
                text = day.toString()
                textSize = 10f
                setTypeface(null, Typeface.BOLD)
                setPadding(6, 6, 6, 6)
                setTextColor(getThemeColor(requireContext(), android.R.attr.textColorPrimary))

                // Color based on data presence
                setBackgroundColor(
                    if (hasData) getThemeColor(requireContext(), android.R.attr.colorPrimary)
                    else getThemeColor(requireContext(), R.attr.colorPrimaryVariant)
                )

                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = 60
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(1, 1, 1, 1)
                }

                // On click, update ViewModel with selected date
                setOnClickListener {
                    Log.d("CalendarFragment", "Clicked on date: $day/${calendar.get(Calendar.MONTH) + 1}/${calendar.get(Calendar.YEAR)}")

                    // Start the shrink animation
                    val shrinkAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.shrink_button)
                    it.startAnimation(shrinkAnimation)

                    // Slight delay to allow animation to play before updating ViewModel
                    Handler(Looper.getMainLooper()).postDelayed({
                        viewModel.updateSelectedDate(
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            day,
                            hasData
                        )
                    }, 150) // Matches animation duration
                }




            }
            gridLayout.addView(dayButton)
        }



        // Add GridLayout to container
        calendarContainer.addView(gridLayout)
    }

    private fun hasDataForDate(context: Context, dateKey: String): Boolean {
        val glucoseEntries = PreferencesHelper.getGlucoseEntriesForDate(context, dateKey)
        val activityEntries = PreferencesHelper.getActivityEntriesForDate(context, dateKey)

        val sharedPrefs = context.getSharedPreferences("GlucoHubPrefs", Context.MODE_PRIVATE)
        val allKeys = sharedPrefs.all.keys

        Log.d("SharedPrefsDebug", "Stored Keys: $allKeys")
        Log.d("SharedPrefsDebug", "Checking $dateKey: Glucose=${glucoseEntries.size}, Activity=${activityEntries.size}")

        return glucoseEntries.isNotEmpty() || activityEntries.isNotEmpty()
    }







    private fun getThemeColor(context: Context, attr: Int): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }
}
