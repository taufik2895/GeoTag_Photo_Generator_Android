package com.geotagphotogenerator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val jakartaTimeZoneId = "Asia/Jakarta"
private const val minutesPerHour = 60

internal fun initialDatePickerMillis(): Long {
    val jakartaCalendar = Calendar.getInstance(TimeZone.getTimeZone(jakartaTimeZoneId))
    val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            jakartaCalendar.get(Calendar.YEAR),
            jakartaCalendar.get(Calendar.MONTH),
            jakartaCalendar.get(Calendar.DAY_OF_MONTH),
        )
    }
    return utcCalendar.timeInMillis
}

internal fun initialTimeMinutes(): Int {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone(jakartaTimeZoneId))
    return calendar.get(Calendar.HOUR_OF_DAY) * minutesPerHour +
        calendar.get(Calendar.MINUTE)
}

private fun formatSelectedDate(dateMillis: Long): String {
    val formatter = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return formatter.format(Date(dateMillis))
}

private fun formatSelectedTime(minutes: Int): String =
    "%02d:%02d".format(Locale.ROOT, minutes / minutesPerHour, minutes % minutesPerHour)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ManualDateTimeCard(
    selectedDateMillis: Long,
    selectedTimeMinutes: Int,
    onDateSelected: (Long) -> Unit,
    onTimeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Date and time",
                style = MaterialTheme.typography.titleLarge,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("Date", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatSelectedDate(selectedDateMillis),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Select date")
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("Time (WIB)", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = formatSelectedTime(selectedTimeMinutes),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Button(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Select time")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let(onDateSelected)
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null,
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false,
            )
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTimeMinutes / minutesPerHour,
            initialMinute = selectedTimeMinutes % minutesPerHour,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(
                            timePickerState.hour * minutesPerHour + timePickerState.minute,
                        )
                        showTimePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
