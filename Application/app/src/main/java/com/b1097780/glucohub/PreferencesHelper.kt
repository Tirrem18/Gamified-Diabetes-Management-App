package com.b1097780.glucohub

import android.content.Context
import android.content.SharedPreferences
import com.b1097780.glucohub.ui.home.ActivityLog.ActivityLogEntry
import com.google.gson.Gson


object PreferencesHelper {
    private const val PREFS_NAME = "GlucoHubPrefs"

    // Keys for different stored values
    private const val KEY_CARB_RATIO = "carb_ratio"
    private const val KEY_NIGHT_UNITS = "night_time_units"
    private const val KEY_ACTIVITY_LOG = "activity_log_entries"
    private const val KEY_GLUCOSE_ENTRIES = "glucose_entries"

    // Get SharedPreferences instance
    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
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
    // ✅ ACTIVITY LOG ENTRIES
    // -------------------------
    fun getActivityLogEntries(context: Context): List<ActivityLogEntry> {
        val json = getPrefs(context).getString(KEY_ACTIVITY_LOG, "") ?: return emptyList()
        return Gson().fromJson(json, object : com.google.gson.reflect.TypeToken<List<ActivityLogEntry>>() {}.type) ?: emptyList()
    }

    fun setActivityLogEntries(context: Context, entries: List<ActivityLogEntry>) {
        val json = Gson().toJson(entries)
        getPrefs(context).edit().putString(KEY_ACTIVITY_LOG, json).apply()
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
}
