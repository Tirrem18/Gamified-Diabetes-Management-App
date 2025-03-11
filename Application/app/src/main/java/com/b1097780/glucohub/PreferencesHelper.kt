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

    fun getGlucoseStatsForDate(context: Context, date: String): Map<String, Any> {
        val glucoseEntries = getGlucoseEntriesForDate(context, date)

        if (glucoseEntries.isEmpty()) {
            return mapOf(
                "totalEntries" to 0,
                "averageGlucose" to 0.0,
                "timeInRange" to "0%",
                "highestGlucose" to 0.0,
                "lowestGlucose" to 0.0,
                "highestTime" to "--.--", // ✅ Added placeholder
                "lowestTime" to "--.--"  // ✅ Added placeholder
            )
        }

        val totalEntries = glucoseEntries.size
        val glucoseValues = glucoseEntries.map { it.second }
        val avgGlucose = glucoseValues.average()

        val highestEntry = glucoseEntries.maxByOrNull { it.second }
        val lowestEntry = glucoseEntries.minByOrNull { it.second }

        val highestGlucose = highestEntry?.second ?: 0.0
        val lowestGlucose = lowestEntry?.second ?: 0.0

        val highestTime = highestEntry?.first ?: "--.--"
        val lowestTime = lowestEntry?.first ?: "--.--"

        // Calculate Time in Range (Assuming 4-10 mmol/L as the target range)
        val inRangeCount = glucoseValues.count { it in 4.0..10.0 }
        val timeInRangePercentage = if (totalEntries > 0) {
            (inRangeCount.toDouble() / totalEntries * 100).toInt()
        } else {
            0
        }

        Log.d("PreferencesHelper", "Highest Glucose: $highestGlucose at $highestTime")
        Log.d("PreferencesHelper", "Lowest Glucose: $lowestGlucose at $lowestTime")

        return mapOf(
            "totalEntries" to totalEntries,
            "averageGlucose" to String.format("%.1f", avgGlucose),
            "timeInRange" to "$timeInRangePercentage%",
            "highestGlucose" to String.format("%.1f", highestGlucose),
            "lowestGlucose" to String.format("%.1f", lowestGlucose),
            "highestTime" to highestTime, // ✅ Now returning actual time
            "lowestTime" to lowestTime    // ✅ Now returning actual time
        )
    }



    // -------------------------
    // ✅ POPULATE TEST DATA (WITH COMPRESSION)
    // -------------------------
    fun populateTestData(context: Context) {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        val today = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.add(Calendar.MINUTE, -20) // Move back 20 minutes from current time
        val currentTimeMinus20 = timeFormat.format(calendar.time)

        val random = Random(System.currentTimeMillis())

        // --- Generate Data for the Last 12 Months, Including Today ---
        for (monthOffset in 0 until 12) {
            val tempCalendar = Calendar.getInstance()
            tempCalendar.add(Calendar.MONTH, -monthOffset) // Move backwards by monthOffset months

            val adjustedYear = tempCalendar.get(Calendar.YEAR)
            val adjustedMonth = tempCalendar.get(Calendar.MONTH)

            tempCalendar.set(adjustedYear, adjustedMonth, 1) // Set to first day of the adjusted month

            val maxDay = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val lastDay = if (monthOffset == 0) today else maxDay

            // 🔹 Pick at least one guaranteed random day per month
            val guaranteedDay = if (monthOffset == 0) today else (1..maxDay).random()

            for (day in 1..lastDay) {
                // 🔹 Lower skip rate (e.g., skip only 20% instead of 50%)
                if (day != today && day != guaranteedDay && random.nextInt(100) < 20) continue

                tempCalendar.set(adjustedYear, adjustedMonth, day)
                val date = dateFormat.format(tempCalendar.time)

                val isToday = (day == today && adjustedMonth == currentMonth && adjustedYear == currentYear)

                val glucoseEntries = getRandomGlucoseDataSet(date)
                    .map { entry ->
                        val parts = entry.split(",")
                        Pair(parts[1], parts[2].toFloat())
                    }
                    .filter { !isToday || it.first <= currentTimeMinus20 }

                val activityEntries = generateActivityLog(date)
                    .map { entry ->
                        val parts = entry.split(",")
                        ActivityLogEntry(parts[1], parts[2], parts.getOrNull(3)?.takeIf { it != "null" }, parts.getOrNull(4)?.takeIf { it != "null" })
                    }
                    .filter { !isToday || it.startTime <= currentTimeMinus20 }

                saveGlucoseEntries(context, date, glucoseEntries)
                saveActivityEntries(context, date, activityEntries)
            }
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


    // Generates Activity Data with Meals, Insulin, and General Activities
    // Generates Activity Data with More Variety
    fun generateActivityLog(date: String): List<String> {
        val random = Random(System.currentTimeMillis())

        // Define time slots every 2 hours
        val timeSlots = listOf(
            "00:00", "02:00", "04:00", "06:00", "08:00", "10:00",
            "12:00", "14:00", "16:00", "18:00", "20:00", "22:00"
        )

        val meals = listOf(
            "$date,Breakfst,07:15,07:35,Egg Toast - 35g",
            "$date,Breakfst,07:45,08:00,Pancakes - 50g",
            "$date,Lunch,12:00,12:30,Grilled Chicken - 45g",
            "$date,Lunch,12:10,12:40,Salmon Salad - 30g",
            "$date,Dinner,18:45,19:15,Steak & Rice - 55g",
            "$date,Dinner,19:30,20:00,Spaghetti - 60g",
            "$date,Snack,10:30,null,Granola Bar - 22g",
            "$date,Snack,15:00,null,Yogurt & Berries - 18g",
            "$date,Snack,22:00,null,Protein Shake - 30g"
        )

        val insulin = listOf(
            // Morning Fast-Acting (After Breakfast)
            "$date,Insulin,07:20,null,Fast-Acting: 5u",
            "$date,Insulin,07:40,null,Fast-Acting: 6u",
            "$date,Insulin,08:00,null,Fast-Acting: 7u",
            "$date,Insulin,08:15,null,Fast-Acting: 8u",

            // Mid-Morning Corrections (Random)
            "$date,Insulin,09:30,null,Fast-Acting: 3u",
            "$date,Insulin,10:15,null,Fast-Acting: 4u",

            // Lunch Fast-Acting
            "$date,Insulin,12:30,null,Fast-Acting: 6u",
            "$date,Insulin,12:45,null,Fast-Acting: 7u",
            "$date,Insulin,13:00,null,Fast-Acting: 8u",
            "$date,Insulin,13:15,null,Fast-Acting: 9u",

            // Afternoon Corrections (Random)
            "$date,Insulin,14:30,null,Fast-Acting: 4u",
            "$date,Insulin,15:45,null,Fast-Acting: 5u",

            // Evening Fast-Acting (After Dinner)
            "$date,Insulin,18:50,null,Fast-Acting: 7u",
            "$date,Insulin,19:10,null,Fast-Acting: 8u",
            "$date,Insulin,19:30,null,Fast-Acting: 9u",
            "$date,Insulin,19:45,null,Fast-Acting: 10u",

            // Nighttime Corrections (Random)
            "$date,Insulin,20:30,null,Fast-Acting: 6u",
            "$date,Insulin,21:15,null,Fast-Acting: 5u",

            // Pre-Snack Insulin (Sometimes before late-night snacks)
            "$date,Insulin,21:45,null,Fast-Acting: 4u",
            "$date,Insulin,22:00,null,Fast-Acting: 5u",

            // Bedtime Long-Acting (ALWAYS)
            "$date,Insulin,22:30,null,Long-Acting: 35u",
            "$date,Insulin,22:45,null,Long-Acting: 35u",
            "$date,Insulin,23:00,null,Long-Acting: 35u"
        )


        val activities = listOf(
            // Gym & Sports
            "$date,Gym,06:15,07:00,Strength Training",
            "$date,Gym,18:30,19:20,Cardio & Weights",
            "$date,Run,07:00,07:40,Morning Jog",
            "$date,Cycling,16:00,16:45,Outdoor Ride",
            "$date,Yoga,08:00,08:45,Morning Stretch",
            "$date,Swim,17:00,17:50,Laps at Pool",

            // Work & Study
            "$date,Work,09:00,12:00,Coding Session",
            "$date,Work,13:00,17:00,Meetings & Reports",
            "$date,Study,14:30,15:45,Math Exercises",
            "$date,Study,16:00,17:30,Exam Prep",

            // Leisure & Relaxation
            "$date,Read,21:15,21:45,Science Fiction",
            "$date,TV,20:00,21:00,New Series",
            "$date,Music,19:30,null,Guitar Practice",
            "$date,Game,22:15,null,Online Chess",
            "$date,Meditate,06:00,06:20,Morning Calm",

            // Chores & Miscellaneous
            "$date,Shop,11:00,11:45,Grocery Run",
            "$date,Cook,18:00,18:45,Meal Prep",
            "$date,Walk,08:20,08:40,Park Walk",
            "$date,Call,17:15,17:45,Family Chat",
            "$date,Clean,10:00,10:30,House Cleaning",

            // One-Time Events
            "$date,Doctor,10:45,null,Check-up Visit",
            "$date,Friend,15:30,null,Coffee Meetup",
            "$date,Repair,11:20,null,Fix Bicycle",
            "$date,Haircut,16:10,null,New Style",
            "$date,Concert,19:00,22:00,Live Music",
            "$date,Trip,08:00,18:00,City Day Trip"
        )

        val allEvents = (meals + insulin + activities).shuffled()
        val eventMap = mutableMapOf<String, String>()
        val usedEvents = mutableSetOf<String>() // Track already assigned events

        for (slot in timeSlots) {
            // Filter available events that match the time slot and have NOT been used
            val availableEvents = allEvents.filter {
                it.contains(slot.substring(0, 2)) && it !in usedEvents
            }

            if (availableEvents.isNotEmpty()) {
                val chosenEvent = availableEvents.random()
                eventMap[slot] = chosenEvent
                usedEvents.add(chosenEvent) // Mark as used
            } else {
                // If no direct match, pick a random event that hasn't been used yet
                val fallbackEvents = allEvents.filter { it !in usedEvents }
                if (fallbackEvents.isNotEmpty()) {
                    val chosenFallback = fallbackEvents.random()
                    eventMap[slot] = chosenFallback
                    usedEvents.add(chosenFallback) // Mark as used
                }
            }
        }

        return eventMap.values.toList()
    }


    }

