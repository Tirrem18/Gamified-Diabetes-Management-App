package com.b1097780.glucohub

import android.content.Context
import android.content.SharedPreferences
import com.b1097780.glucohub.ui.home.ActivityLog.ActivityLogEntry
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

object PreferencesHelper {
    private const val PREFS_NAME = "GlucoHubPrefs"

    // Keys for different stored values
    private const val KEY_CARB_RATIO = "carb_ratio"
    private const val KEY_NIGHT_UNITS = "night_time_units"
    private const val KEY_ACTIVITY_LOG = "activity_log_entries"
    private const val KEY_GLUCOSE_ENTRIES = "glucose_entries"
    private const val KEY_USER_COINS = "userCoins"
    private const val KEY_LAST_ENTRY_TIME = "lastEntryTime"

    // Get SharedPreferences instance
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // -------------------------
    // ✅ CLEAR ALL DATA (Reset SharedPreferences)
    // -------------------------
    fun clearAllData(context: Context) {
        getPrefs(context).edit().clear().apply()
    }

    // -------------------------
    // ✅ CARB RATIO
    // -------------------------
    fun getCarbRatio(context: Context): Int {
        return getPrefs(context).getInt(KEY_CARB_RATIO, 9) // Default 9:1
    }

    fun setCarbRatio(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_CARB_RATIO, value).apply()
    }

    // -------------------------
    // ✅ NIGHT TIME UNITS
    // -------------------------
    fun getNightTimeUnits(context: Context): Int {
        return getPrefs(context).getInt(KEY_NIGHT_UNITS, 30) // Default 30 units
    }

    fun setNightTimeUnits(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_NIGHT_UNITS, value).apply()
    }

    // -------------------------
    // ✅ USER COINS
    // -------------------------
    fun getUserCoins(context: Context): Int {
        return getPrefs(context).getInt(KEY_USER_COINS, 10) // Default to 10 coins
    }

    fun addCoins(context: Context, amount: Int) {
        val sharedPrefs = getPrefs(context)
        val currentCoins = sharedPrefs.getInt("userCoins", 0)
        sharedPrefs.edit().putInt("userCoins", currentCoins + amount).apply()
    }



    fun setUserCoins(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_USER_COINS, value).apply()
    }

    // -------------------------
    // ✅ ACTIVITY LOG ENTRIES
    // -------------------------
    fun getActivityLogEntries(context: Context): List<ActivityLogEntry> {
        val sharedPrefs = getPrefs(context)
        val entryString = sharedPrefs.getString(KEY_ACTIVITY_LOG, "") ?: ""
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        return entryString.split(";").mapNotNull {
            val parts = it.split(",")
            if (parts.size >= 4 && parts[0] == today) {
                ActivityLogEntry(
                    name = parts[1],
                    startTime = parts[2],
                    endTime = parts[3].takeIf { it != "null" },
                    description = parts.getOrNull(4)?.takeIf { it != "null" }
                )
            } else {
                null
            }
        }
    }

    fun setActivityLogEntries(context: Context, entries: List<ActivityLogEntry>) {
        val sharedPrefs = getPrefs(context)
        val editor = sharedPrefs.edit()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val today = dateFormat.format(Date())

        val entryString = entries.joinToString(";") { entry ->
            "$today,${entry.name},${entry.startTime},${entry.endTime ?: "null"},${entry.description ?: "null"}"
        }

        editor.putString(KEY_ACTIVITY_LOG, entryString)
        editor.apply()
    }

    // -------------------------
    // ✅ RECENT GLUCOSE ENTRIES
    // -------------------------
    fun getRecentGlucoseEntries(context: Context): String {
        return getPrefs(context).getString(KEY_GLUCOSE_ENTRIES, "") ?: ""
    }

    fun setRecentGlucoseEntries(context: Context, value: String) {
        getPrefs(context).edit().putString(KEY_GLUCOSE_ENTRIES, value).apply()
    }

    // -------------------------
    // ✅ LAST ENTRY TIME
    // -------------------------
    fun getLastEntryTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_ENTRY_TIME, 0L)
    }

    fun setLastEntryTime(context: Context, time: Long) {
        getPrefs(context).edit().putLong(KEY_LAST_ENTRY_TIME, time).apply()
    }

    // -------------------------
    // ✅ POPULATE TEST DATA
    // -------------------------

    fun populateTestData(context: Context) {
        val editor = getPrefs(context).edit()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val today = calendar.get(Calendar.DAY_OF_MONTH)  // Current day of the month
        val currentMonth = calendar.get(Calendar.MONTH)  // Current month (0-based index)
        val currentYear = calendar.get(Calendar.YEAR)  // Current year

        val glucoseEntries = mutableListOf<String>()
        val activityLog = mutableListOf<String>()

        // --- Step 1: Get Last Month's Data ---
        calendar.set(currentYear, currentMonth - 1, 1) // Move to the first day of last month
        val lastMonth = calendar.get(Calendar.MONTH)
        val lastYear = calendar.get(Calendar.YEAR)
        val lastMonthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) // Get last month's total days

        // Loop through last month's entire range (1st to last day)
        for (day in 1..lastMonthMaxDay) {
            calendar.set(lastYear, lastMonth, day) // Set calendar to specific day
            val date = dateFormat.format(calendar.time) // Format as yyyyMMdd

            // Get a random glucose dataset for the day
            val glucoseDataSet = getRandomGlucoseDataSet(date)
            glucoseEntries.addAll(glucoseDataSet)

            // Add activity logs
            val testActivities = generateActivityLog(date)
            activityLog.addAll(testActivities)
        }

        // --- Step 2: Get This Month's Data (1st to Today) ---
        calendar.set(currentYear, currentMonth, 1) // Move to the first day of this month

        // Loop from the 1st of this month up to today
        for (day in 1..today) {
            calendar.set(currentYear, currentMonth, day) // Set calendar to specific day
            val date = dateFormat.format(calendar.time) // Format as yyyyMMdd

            // Get a random glucose dataset for the day
            val glucoseDataSet = getRandomGlucoseDataSet(date)
            glucoseEntries.addAll(glucoseDataSet)

            // Add activity logs
            val testActivities = generateActivityLog(date)
            activityLog.addAll(testActivities)
        }

        // Store data in shared preferences
        editor.putString(KEY_GLUCOSE_ENTRIES, glucoseEntries.joinToString(";"))
        editor.putString(KEY_ACTIVITY_LOG, activityLog.joinToString(";"))
        editor.apply()
    }


    // Function to randomly select one of 2 datasets with proper fluctuations
    fun getRandomGlucoseDataSet(date: String): List<String> {
        val goodDay = listOf(
            "$date,00.05f,${randomGlucose(3.9, 6.4)}f", "$date,00.45f,${randomGlucose(4.1, 6.6)}f",
            "$date,01.20f,${randomGlucose(3.8, 6.3)}f", "$date,01.55f,${randomGlucose(4.0, 6.5)}f",
            "$date,02.30f,${randomGlucose(3.6, 6.1)}f", "$date,03.15f,${randomGlucose(3.9, 6.4)}f",
            "$date,04.00f,${randomGlucose(3.5, 6.0)}f", "$date,04.45f,${randomGlucose(4.2, 6.7)}f",
            "$date,05.20f,${randomGlucose(3.9, 6.4)}f", "$date,06.10f,${randomGlucose(4.3, 6.7)}f", // Pre-exercise
            "$date,06.40f,${randomGlucose(3.5, 6.0)}f", "$date,07.10f,${randomGlucose(3.8, 6.3)}f", // Post-exercise drop
            "$date,07.45f,${randomGlucose(3.9, 6.4)}f", "$date,08.15f,${randomGlucose(5.0, 7.5)}f", // Breakfast spike
            "$date,08.50f,${randomGlucose(5.2, 7.7)}f", "$date,09.30f,${randomGlucose(5.0, 7.5)}f",
            "$date,10.05f,${randomGlucose(4.5, 7.0)}f", "$date,10.45f,${randomGlucose(4.2, 6.7)}f",
            "$date,11.20f,${randomGlucose(4.0, 6.5)}f", "$date,12.00f,${randomGlucose(5.5, 8.0)}f", // Lunch spike
            "$date,12.40f,${randomGlucose(5.2, 7.7)}f", "$date,13.15f,${randomGlucose(4.8, 7.3)}f",
            "$date,14.00f,${randomGlucose(4.2, 6.7)}f", "$date,14.45f,${randomGlucose(4.0, 6.5)}f",
            "$date,15.30f,${randomGlucose(3.9, 6.4)}f", "$date,16.10f,${randomGlucose(4.2, 6.7)}f",
            "$date,17.00f,${randomGlucose(3.5, 6.0)}f", "$date,17.45f,${randomGlucose(3.8, 6.3)}f",
            "$date,18.30f,${randomGlucose(5.0, 7.5)}f", "$date,19.10f,${randomGlucose(5.8, 8.3)}f", // Dinner spike
            "$date,19.55f,${randomGlucose(5.2, 7.7)}f", "$date,20.30f,${randomGlucose(4.8, 7.3)}f",
            "$date,21.15f,${randomGlucose(4.3, 6.8)}f", "$date,22.00f,${randomGlucose(3.9, 6.4)}f",
            "$date,22.40f,${randomGlucose(3.5, 6.0)}f", "$date,23.25f,${randomGlucose(3.8, 6.3)}f"
        )

        val badDay = listOf(
            "$date,00.10f,${randomGlucose(8.0, 10.5)}f", "$date,00.50f,${randomGlucose(8.5, 11.0)}f",
            "$date,01.30f,${randomGlucose(8.2, 10.7)}f", "$date,02.15f,${randomGlucose(8.0, 10.5)}f",
            "$date,03.00f,${randomGlucose(7.8, 13.3)}f", "$date,03.45f,${randomGlucose(7.6, 10.1)}f",
            "$date,04.30f,${randomGlucose(7.5, 13.0)}f", "$date,05.15f,${randomGlucose(7.8, 10.3)}f",
            "$date,06.00f,${randomGlucose(7.0, 9.5)}f", "$date,06.45f,${randomGlucose(6.8, 9.3)}f", // Exercise drop
            "$date,07.30f,${randomGlucose(6.5, 9.0)}f", "$date,08.10f,${randomGlucose(9.0, 11.5)}f", // Breakfast spike
            "$date,08.55f,${randomGlucose(9.5, 12.0)}f", "$date,09.40f,${randomGlucose(9.2, 11.7)}f",
            "$date,10.25f,${randomGlucose(8.8, 11.3)}f", "$date,11.10f,${randomGlucose(8.5, 11.0)}f",
            "$date,12.00f,${randomGlucose(9.5, 12.0)}f", "$date,12.50f,${randomGlucose(9.8, 12.3)}f",
            "$date,13.35f,${randomGlucose(9.2, 11.7)}f", "$date,14.20f,${randomGlucose(8.5, 11.0)}f",
            "$date,15.05f,${randomGlucose(8.0, 10.5)}f", "$date,15.50f,${randomGlucose(7.8, 10.3)}f",
            "$date,16.35f,${randomGlucose(7.5, 10.0)}f", "$date,17.20f,${randomGlucose(6.8, 9.3)}f", // Exercise drop
            "$date,18.10f,${randomGlucose(9.5, 12.0)}f", "$date,18.55f,${randomGlucose(10.2, 12.7)}f",
            "$date,19.40f,${randomGlucose(9.8, 12.3)}f", "$date,20.25f,${randomGlucose(9.2, 11.7)}f",
            "$date,21.10f,${randomGlucose(8.5, 11.0)}f", "$date,22.00f,${randomGlucose(7.8, 10.3)}f",
            "$date,22.45f,${randomGlucose(7.2, 9.7)}f", "$date,23.30f,${randomGlucose(6.8, 9.3)}f"
        )

        return if (Random.nextBoolean()) goodDay else badDay
    }

    // Function to generate random glucose values within a range
    fun randomGlucose(min: Double, max: Double): Double {
        return String.format("%.1f", Random.nextDouble(min, max)).toDouble()
    }



}

    // Function to generate an activity log
    fun generateActivityLog(date: String): List<String> {
        return listOf(
            "$date,Read,00:05,00:30,Magazine", "$date,Coding,00:40,01:25,Fixing bugs",
            "$date,Snack,01:50,02:10,Midnight snack", "$date,Study,02:35,03:15,Exam prep",
            "$date,Gym,06:10,07:00,Weightlifting", "$date,Breakfast,07:20,07:45,Oats and eggs",
            "$date,Walk,08:10,08:38,Morning stroll", "$date,Coding,09:25,10:00,New feature",
            "$date,Snack,10:30,10:50,Coffee and fruit", "$date,Work,11:10,12:15,Writing docs",
            "$date,Lunch,13:15,13:55,Healthy meal", "$date,Study,14:22,14:58,Math exercises",
            "$date,Gym,16:10,17:00,Cardio session", "$date,Dinner,19:40,20:10,Steak and veggies",
            "$date,TV,21:40,22:15,Favourite show", "$date,Sleep,23:40,null,Going to bed"
        )
    }



