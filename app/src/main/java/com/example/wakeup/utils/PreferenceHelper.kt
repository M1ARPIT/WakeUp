package com.example.wakeup.utils

import android.content.Context
import android.content.SharedPreferences

object PreferenceHelper {
    private const val PREF_NAME = "WakeUpPrefs"
    private const val KEY_FIRST_RUN = "first_run"
    private const val KEY_REMINDER_INTERVAL = "reminder_interval"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isFirstRun(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FIRST_RUN, true)
    }

    fun setFirstRunCompleted(context: Context) {
        getPrefs(context).edit().putBoolean(KEY_FIRST_RUN, false).apply()
    }

    fun saveReminderInterval(context: Context, millis: Long) {
        getPrefs(context).edit().putLong(KEY_REMINDER_INTERVAL, millis).apply()
    }

    fun getReminderInterval(context: Context): Long {
        return getPrefs(context).getLong(KEY_REMINDER_INTERVAL, 2 * 60 * 60 * 1000L) // Default 2hr
    }
}
