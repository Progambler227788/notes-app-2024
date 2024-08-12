package com.talhaatif.notesapplication.firebase

import android.content.Context
import android.content.SharedPreferences

class Util {
    fun saveLocalData(activity: Context, key: String, value: String) {
        val sharedPreferences: SharedPreferences = activity.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply() // apply changes
    }

    fun getLocalData(activity: Context, key: String): String {
        val sharedPreferences: SharedPreferences = activity.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString(key, "") ?: ""
    }
}

