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

        // Expanded Glucose Test Data
        val testEntries = listOf(
            "$today,0.0,8.0", "$today,0.15,7.2", "$today,0.2,6.5", "$today,0.35,9.1", "$today,0.5,10.0"
        )
        editor.putString(KEY_GLUCOSE_ENTRIES, testEntries.joinToString(";"))

        // Expanded Activity Log Test Data
        val testActivities = listOf(
            "$today,Study,00:30,00:50,Reading notes",
            "$today,Coding,01:10,01:35,Fixing bugs",
            "$today,Snack,02:30,02:45,Eating fruit"
        )

        editor.putString(KEY_ACTIVITY_LOG, testActivities.joinToString(";"))
        editor.apply()
    }
}
