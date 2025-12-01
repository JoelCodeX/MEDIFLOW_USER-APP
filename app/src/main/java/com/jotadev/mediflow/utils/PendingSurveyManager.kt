package com.jotadev.mediflow.utils

import android.content.Context

object PendingSurveyManager {
    private const val PREF_NAME = "mediflow_prefs"
    private const val KEY_PENDING = "pending_survey"
    private const val KEY_PENDING_ID = "pending_survey_id"

    fun setPending(context: Context, pending: Boolean, id: String? = null) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(KEY_PENDING, pending)
        if (id != null) {
            editor.putString(KEY_PENDING_ID, id)
        } else {
            editor.remove(KEY_PENDING_ID)
        }
        editor.apply()
    }

    fun isPending(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PENDING, false)
    }

    fun getPendingId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PENDING_ID, null)
    }

    fun clear(context: Context) {
        setPending(context, false, null)
    }
}