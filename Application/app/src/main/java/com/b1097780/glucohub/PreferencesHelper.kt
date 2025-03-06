package com.b1097780.glucohub

import android.content.Context
import android.content.SharedPreferences
import com.b1097780.glucohub.ui.home.ActivityLog.ActivityLogEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

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
        val today = dateFormat.format(Date())

        // Fine-Tuned Diabetic Blood Sugar Data
        val testEntries = listOf(
            "$today,00.00f,6.5f", "$today,00.22f,6.3f", "$today,00.45f,6.2f",
            "$today,01.10f,6.1f", "$today,01.35f,5.9f", "$today,01.40f,7.5f", "$today,01.55f,8.1f", "$today,02.10f,7.3f", "$today,02.25f,6.8f",
            "$today,02.42f,6.2f", "$today,03.05f,5.9f", "$today,03.30f,5.6f", "$today,03.50f,5.4f",
            "$today,04.15f,5.2f", "$today,04.38f,5.1f", "$today,05.00f,5.0f", "$today,05.25f,4.9f", "$today,05.50f,4.8f",
            "$today,06.15f,5.3f", "$today,06.40f,6.0f", "$today,07.05f,7.2f", "$today,07.30f,8.5f", "$today,07.55f,9.8f",
            "$today,08.20f,8.7f", "$today,08.45f,7.5f", "$today,09.10f,6.9f", "$today,09.35f,6.3f", "$today,09.55f,6.1f",
            "$today,10.20f,5.8f", "$today,10.45f,5.6f", "$today,11.05f,5.3f", "$today,11.30f,5.0f", "$today,11.50f,4.8f",
            "$today,12.15f,4.6f", "$today,12.40f,4.4f", "$today,13.00f,5.2f", "$today,13.25f,6.3f", "$today,13.50f,7.8f",
            "$today,14.15f,8.9f", "$today,14.40f,7.5f", "$today,15.05f,6.8f", "$today,15.30f,6.2f", "$today,15.55f,5.9f",
            "$today,16.20f,5.6f", "$today,16.45f,5.3f", "$today,17.10f,5.0f", "$today,17.35f,4.8f", "$today,18.00f,6.5f",
            "$today,18.25f,8.0f", "$today,18.50f,9.5f", "$today,19.15f,8.2f", "$today,19.40f,7.0f", "$today,20.05f,6.5f",
            "$today,20.30f,6.2f", "$today,20.55f,5.9f", "$today,21.20f,5.5f", "$today,21.45f,5.2f", "$today,22.10f,4.9f",
            "$today,22.35f,4.7f", "$today,23.00f,4.6f", "$today,23.25f,4.5f", "$today,23.50f,4.4f"
        )
        editor.putString(KEY_GLUCOSE_ENTRIES, testEntries.joinToString(";"))

        // Expanded Activity Log with Natural Timing
        val testActivities = listOf(
            "$today,Read,00:00,00:25,Magazine", "$today,Coding,00:30,01:15,Fixing bugs",
            "$today,Snack,01:40,02:00,Midnight snack", "$today,Study,02:30,03:05,Exam prep",
            "$today,Gym,06:00,06:50,Weightlifting", "$today,Breakfast,07:10,07:30,Oats and eggs",
            "$today,Walk,08:00,08:25,Morning stroll", "$today,Coding,09:15,09:50,New feature",
            "$today,Snack,10:20,10:35,Coffee and fruit", "$today,Work,11:00,12:00,Writing docs",
            "$today,Lunch,13:00,13:40,Healthy meal", "$today,Study,14:15,14:50,Math exercises",
            "$today,Gym,16:00,16:45,Cardio session", "$today,Dinner,19:30,20:00,Steak and veggies",
            "$today,TV,21:30,22:00,Favourite show", "$today,Sleep,23:30,null,Going to bed"
        )
        editor.putString(KEY_ACTIVITY_LOG, testActivities.joinToString(";"))
        editor.apply()
    }

}
