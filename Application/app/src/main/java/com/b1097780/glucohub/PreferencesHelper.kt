package com.b1097780.glucohub

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.b1097780.glucohub.ui.home.ActivityLog.ActivityLogEntry
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import kotlin.random.Random

object PreferencesHelper {
    private const val PREFS_NAME = "GlucoHubPrefs"

    // Keys for stored values
    private const val KEY_CARB_RATIO = "carb_ratio"
    private const val KEY_NIGHT_UNITS = "night_time_units"
    private const val KEY_USER_COINS = "userCoins"
    private const val KEY_LAST_ENTRY_TIME = "lastEntryTime"
    private const val KEY_USER_STREAK = "userStreak"
    private const val KEY_LAST_STREAK_DATE = "lastStreakDate"
    private const val KEY_HIGHEST_STREAK = "highestStreak"
    private const val KEY_COIN_MULTIPLIER = "coinMultiplier"
    private const val KEY_MILESTONE_PREFIX = "milestone_"

    // Get SharedPreferences instance
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
        // Get the current streak (default 0)
        fun getUserStreak(context: Context): Int {
            return getPrefs(context).getInt(KEY_USER_STREAK, 0)
        }

        // Set the new streak count
        fun setUserStreak(context: Context, value: Int) {
            getPrefs(context).edit().putInt(KEY_USER_STREAK, value).apply()
        }

        // Get the last streak date (default to empty if not set)
        fun getLastStreakDate(context: Context): String {
            return getPrefs(context).getString(KEY_LAST_STREAK_DATE, "") ?: ""
        }

        // Set the last streak date
        fun setLastStreakDate(context: Context, date: String) {
            getPrefs(context).edit().putString(KEY_LAST_STREAK_DATE, date).apply()
        }

        // Get today's date in "yyyyMMdd" format
        fun getCurrentDate(): String {
            val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            return sdf.format(Date())
        }

    fun checkAndResetStreak(context: Context) {
        val lastStreakDate = getLastStreakDate(context)
        val todayDate = getCurrentDate()

        if (lastStreakDate.isEmpty()) return // Don't set anything if never used before

        val lastDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(lastStreakDate)
        val today = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).parse(todayDate)

        if (lastDate == null || today == null) return

        val diff = ((today.time - lastDate.time) / (1000 * 60 * 60 * 24)).toInt()

        if (diff >= 2) {
            setUserStreak(context, 0) // Reset streak if 2+ days have passed
        }

    }

    fun updateStreakOnEntry(context: Context) {
        val lastStreakDate = getLastStreakDate(context)
        val todayDate = getCurrentDate()

        if (lastStreakDate != todayDate) {
            val newStreak = getUserStreak(context) + 1
            setUserStreak(context, newStreak)
            setLastStreakDate(context, todayDate)

            // ✅ Update the UI in MainActivity
            (context as? MainActivity)?.updateStreakButton(newStreak)
        }
    }

    fun getHighestStreak(context: Context): Int {
        return getPrefs(context).getInt(KEY_HIGHEST_STREAK, 0)
    }

    // Set Highest Streak
    fun setHighestStreak(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_HIGHEST_STREAK, value).apply()
    }

    // Update Highest Streak if Needed
    fun updateHighestStreak(context: Context) {
        val currentStreak = getUserStreak(context)
        val highestStreak = getHighestStreak(context)

        if (currentStreak > highestStreak) {
            setHighestStreak(context, currentStreak)
        }
    }

    // Get Coin Multiplier
    fun getCoinMultiplier(context: Context): Int {
        return getPrefs(context).getInt(KEY_COIN_MULTIPLIER, 1) // Default to x1
    }

    // Update Coin Multiplier Based on Streak
    fun updateCoinMultiplier(context: Context) {
        val currentStreak = getUserStreak(context)
        val multiplier = when {
            currentStreak >= 100 -> 3
            currentStreak >= 50 -> 2
            else -> 1
        }
        getPrefs(context).edit().putInt(KEY_COIN_MULTIPLIER, multiplier).apply()
    }

    fun addCoins(context: Context, baseAmount: Int) {
        val sharedPrefs = getPrefs(context)
        val currentCoins = sharedPrefs.getInt(KEY_USER_COINS, 0) // Get current coins
        val multiplier = getCoinMultiplier(context) // Get stored multiplier
        val finalCoins = baseAmount * multiplier // Apply multiplier

        Log.d("DEBUG", "Adding $baseAmount coins with multiplier x$multiplier. Total added: $finalCoins")

        // ✅ Save new coin total
        sharedPrefs.edit().putInt(KEY_USER_COINS, currentCoins + finalCoins).apply()

        // ✅ Update UI in MainActivity
        (context as? MainActivity)?.updateCoinButton(currentCoins + finalCoins)
    }




    // Check if Milestone is Claimed
    fun isMilestoneClaimed(context: Context, days: Int): Boolean {
        return getPrefs(context).getBoolean("$KEY_MILESTONE_PREFIX$days", false)
    }

    // Mark Milestone as Claimed
    fun setMilestoneClaimed(context: Context, days: Int) {
        getPrefs(context).edit().putBoolean("$KEY_MILESTONE_PREFIX$days", true).apply()
    }







    // -------------------------
    // ✅ CLEAR ALL DATA
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
        return getPrefs(context).getInt(KEY_USER_COINS, 0) // Default to 10 coins
    }

    fun setUserCoins(context: Context, value: Int) {
        getPrefs(context).edit().putInt(KEY_USER_COINS, value).apply()
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
    // ✅ SAVE GLUCOSE & ACTIVITY LOG DATA (WITH COMPRESSION)
    // -------------------------

    fun saveGlucoseEntries(context: Context, date: String, glucoseEntries: List<Pair<String, Float>>) {
        val sharedPrefs = getPrefs(context)
        val editor = sharedPrefs.edit()

        val glucoseArray = JSONArray()
        for ((time, level) in glucoseEntries) {
            val entryObject = JSONObject().apply {
                put("time", time)
                put("glucose_level", level)
            }
            glucoseArray.put(entryObject)
        }

        editor.putString("${date}_glucose", compressJson(glucoseArray.toString()))
        editor.apply()
    }

    fun saveActivityEntries(context: Context, date: String, activityEntries: List<ActivityLogEntry>) {
        val sharedPrefs = getPrefs(context)
        val editor = sharedPrefs.edit()

        val activityArray = JSONArray()
        for (entry in activityEntries) {
            val entryObject = JSONObject().apply {
                put("name", entry.name)
                put("start_time", entry.startTime)
                put("end_time", entry.endTime ?: "null")
                put("description", entry.description ?: "null")
            }
            activityArray.put(entryObject)
        }

        editor.putString("${date}_activities", compressJson(activityArray.toString()))
        editor.apply()
    }

    // -------------------------
    // ✅ GET GLUCOSE & ACTIVITY ENTRIES (WITH DECOMPRESSION)
    // -------------------------

    fun getGlucoseEntriesForDate(context: Context, date: String): List<Pair<String, Float>> {
        val sharedPrefs = getPrefs(context)
        val jsonString = decompressJson(sharedPrefs.getString("${date}_glucose", "[]") ?: "[]")

        val resultGlucose = mutableListOf<Pair<String, Float>>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val entryObject = jsonArray.getJSONObject(i)
            val time = entryObject.getString("time")
            val level = entryObject.getDouble("glucose_level").toFloat()
            resultGlucose.add(Pair(time, level))
        }

        return resultGlucose
    }

    fun getActivityEntriesForDate(context: Context, date: String): List<ActivityLogEntry> {
        val sharedPrefs = getPrefs(context)
        val jsonString = decompressJson(sharedPrefs.getString("${date}_activities", "[]") ?: "[]")

        val resultActivities = mutableListOf<ActivityLogEntry>()
        val jsonArray = JSONArray(jsonString)

        for (i in 0 until jsonArray.length()) {
            val entryObject = jsonArray.getJSONObject(i)
            resultActivities.add(
                ActivityLogEntry(
                    name = entryObject.getString("name"),
                    startTime = entryObject.getString("start_time"),
                    endTime = entryObject.optString("end_time").takeIf { it != "null" },
                    description = entryObject.optString("description").takeIf { it != "null" }
                )
            )
        }

        return resultActivities
    }

    // -------------------------
    // ✅ JSON COMPRESSION & DECOMPRESSION
    // -------------------------

    private fun compressJson(json: String): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        GZIPOutputStream(byteArrayOutputStream).use { gzipOutput ->
            gzipOutput.write(json.toByteArray(Charsets.UTF_8))
        }
        return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray())
    }

    private fun decompressJson(compressed: String): String {
        return try {
            val bytes = Base64.getDecoder().decode(compressed)
            GZIPInputStream(ByteArrayInputStream(bytes)).bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            "[]" // Return empty JSON array if decompression fails
        }
    }

    // -------------------------
    // ✅ GET GLUCOSE ENTRIES IN A DATE RANGE
    // -------------------------

    fun getGlucoseEntriesBetweenDates(context: Context, startDate: String, endDate: String): Map<String, List<Pair<String, Float>>> {
        val result = mutableMapOf<String, List<Pair<String, Float>>>()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val start = dateFormat.parse(startDate) ?: return result
        val end = dateFormat.parse(endDate) ?: return result

        calendar.time = start
        while (!calendar.time.after(end)) {
            val dateKey = dateFormat.format(calendar.time)
            val entries = getGlucoseEntriesForDate(context, dateKey)
            if (entries.isNotEmpty()) result[dateKey] = entries
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return result
    }

    // -------------------------
    // ✅ GET ACTIVITY ENTRIES IN A DATE RANGE
    // -------------------------

    fun getActivityEntriesBetweenDates(context: Context, startDate: String, endDate: String): Map<String, List<ActivityLogEntry>> {
        val result = mutableMapOf<String, List<ActivityLogEntry>>()
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val start = dateFormat.parse(startDate) ?: return result
        val end = dateFormat.parse(endDate) ?: return result

        calendar.time = start
        while (!calendar.time.after(end)) {
            val dateKey = dateFormat.format(calendar.time)
            val entries = getActivityEntriesForDate(context, dateKey)
            if (entries.isNotEmpty()) result[dateKey] = entries
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return result
    }

    // -------------------------
    // ✅ POPULATE TEST DATA (WITH COMPRESSION)
    // -------------------------

    fun populateTestData(context: Context) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val today = calendar.get(Calendar.DAY_OF_MONTH)  // Current day of the month
        val currentMonth = calendar.get(Calendar.MONTH)  // Current month (0-based index)
        val currentYear = calendar.get(Calendar.YEAR)  // Current year

        // --- Step 1: Generate Last Month's Data ---
        calendar.set(currentYear, currentMonth - 1, 1) // Move to first day of last month
        val lastMonthMaxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH) // Get last month's total days

        for (day in 1..lastMonthMaxDay) {
            calendar.set(currentYear, currentMonth - 1, day)
            val date = dateFormat.format(calendar.time)

            // Generate test data
            val glucoseEntries = getRandomGlucoseDataSet(date).map {
                val parts = it.split(",")
                Pair(parts[1], parts[2].toFloat()) // Extract time & glucose level
            }

            val activityEntries = generateActivityLog(date).map { entry ->
                val parts = entry.split(",")
                ActivityLogEntry(parts[1], parts[2], parts.getOrNull(3)?.takeIf { it != "null" }, parts.getOrNull(4)?.takeIf { it != "null" })
            }

            // ✅ Save separately in SharedPreferences (WITH COMPRESSION)
            saveGlucoseEntries(context, date, glucoseEntries)
            saveActivityEntries(context, date, activityEntries)
        }

        // --- Step 2: Generate Data for Current Month Until Today ---
        calendar.set(currentYear, currentMonth, 1)

        for (day in 1..today) {
            calendar.set(currentYear, currentMonth, day)
            val date = dateFormat.format(calendar.time)

            // Generate test data
            val glucoseEntries = getRandomGlucoseDataSet(date).map {
                val parts = it.split(",")
                Pair(parts[1], parts[2].toFloat()) // Extract time & glucose level
            }

            val activityEntries = generateActivityLog(date).map { entry ->
                val parts = entry.split(",")
                ActivityLogEntry(parts[1], parts[2], parts.getOrNull(3)?.takeIf { it != "null" }, parts.getOrNull(4)?.takeIf { it != "null" })
            }

            // ✅ Save separately in SharedPreferences (WITH COMPRESSION)
            saveGlucoseEntries(context, date, glucoseEntries)
            saveActivityEntries(context, date, activityEntries)
        }
    }

    fun getRandomGlucoseDataSet(date: String): List<String> {
        val goodDay = listOf(
            "$date,00.05f,${randomGlucose(1.9, 4.4)}f", "$date,00.45f,${randomGlucose(1.1, 6.6)}f",
            "$date,01.20f,${randomGlucose(2.8, 6.3)}f", "$date,01.55f,${randomGlucose(4.0, 6.5)}f",
            "$date,02.30f,${randomGlucose(2.6, 6.1)}f", "$date,03.15f,${randomGlucose(3.9, 6.4)}f",
            "$date,04.00f,${randomGlucose(2.5, 6.0)}f", "$date,04.45f,${randomGlucose(4.2, 6.7)}f",
            "$date,05.20f,${randomGlucose(2.9, 6.4)}f", "$date,06.10f,${randomGlucose(4.3, 6.7)}f", // Pre-exercise
            "$date,06.40f,${randomGlucose(3.5, 6.0)}f", "$date,07.10f,${randomGlucose(3.8, 6.3)}f", // Post-exercise drop
            "$date,07.45f,${randomGlucose(1.9, 6.4)}f", "$date,08.15f,${randomGlucose(5.0, 7.5)}f", // Breakfast spike
            "$date,08.50f,${randomGlucose(3.2, 7.7)}f", "$date,09.30f,${randomGlucose(5.0, 7.5)}f",
            "$date,10.05f,${randomGlucose(1.5, 7.0)}f", "$date,10.45f,${randomGlucose(4.2, 6.7)}f",
            "$date,11.20f,${randomGlucose(1.0, 6.5)}f", "$date,12.00f,${randomGlucose(5.5, 8.0)}f", // Lunch spike
            "$date,12.40f,${randomGlucose(1.2, 7.7)}f", "$date,13.15f,${randomGlucose(4.8, 7.3)}f",
            "$date,14.00f,${randomGlucose(1.2, 6.7)}f", "$date,14.45f,${randomGlucose(4.0, 6.5)}f",
            "$date,15.30f,${randomGlucose(1.9, 6.4)}f", "$date,16.10f,${randomGlucose(4.2, 6.7)}f",
            "$date,17.00f,${randomGlucose(1.5, 6.0)}f", "$date,17.45f,${randomGlucose(3.8, 6.3)}f",
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
            "$date,15.05f,${randomGlucose(8.0, 10.5)}f", "$date,15.50f,${randomGlucose(7.8, 33.3)}f",
            "$date,16.35f,${randomGlucose(7.5, 10.0)}f", "$date,17.20f,${randomGlucose(6.8, 24.3)}f", // Exercise drop
            "$date,18.10f,${randomGlucose(9.5, 22.0)}f", "$date,18.55f,${randomGlucose(10.2, 12.7)}f",
            "$date,19.40f,${randomGlucose(9.8, 22.3)}f", "$date,20.25f,${randomGlucose(9.2, 21.7)}f",
            "$date,21.10f,${randomGlucose(8.5, 21.0)}f", "$date,22.00f,${randomGlucose(7.8, 10.3)}f",
            "$date,22.45f,${randomGlucose(7.2, 22.7)}f", "$date,23.30f,${randomGlucose(6.8, 9.3)}f"
        )

        return if (Random.nextBoolean()) goodDay else badDay
    }

    // Function to generate random glucose values within a range
    fun randomGlucose(min: Double, max: Double): Double {
        return String.format("%.1f", Random.nextDouble(min, max)).toDouble()
    }


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

}

