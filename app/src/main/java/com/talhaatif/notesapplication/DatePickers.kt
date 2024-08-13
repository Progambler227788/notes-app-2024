package com.talhaatif.notesapplication

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class DatePickers(
    private val context: Context,
    private val applyDateRangeFilter: (Date?, Date?) -> Unit
) {

    fun showDatePickerDialog() {
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Select a date range")
            .build()

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDateMillis = selection.first
            val endDateMillis = selection.second

            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC") // Set timezone to UTC for consistency
            val startDate = startDateMillis?.let { Date(it) }
            val endDate = endDateMillis?.let { Date(it) }

            Log.d("DatePickers", "Start Date Selected: ${startDate?.let { formatDate(it) }}")
            Log.d("DatePickers", "End Date Selected: ${endDate?.let { formatDate(it) }}")

            applyDateRangeFilter(startDate, endDate)
        }

        dateRangePicker.show((context as AppCompatActivity).supportFragmentManager, "DATE_PICKER")
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
        return sdf.format(date)
    }
}
