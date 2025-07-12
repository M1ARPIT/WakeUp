package com.example.wakeup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.wakeup.utils.PreferenceHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    val options = listOf("15 min", "30 min", "1 hour", "2 hours", "4 hours")
    val intervalMap = mapOf(
        "15 min" to 15 * 60 * 1000L,
        "30 min" to 30 * 60 * 1000L,
        "1 hour" to 60 * 60 * 1000L,
        "2 hours" to 2 * 60 * 60 * 1000L,
        "4 hours" to 4 * 60 * 60 * 1000L
    )

    var expanded by remember { mutableStateOf(false) }

    // ✅ Load saved interval on start
    var selectedOption by remember {
        mutableStateOf(
            intervalMap.entries.find { it.value == PreferenceHelper.getReminderInterval(context) }?.key
                ?: "2 hours"
        )
    }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text("🕒 Reminder Interval")
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            TextField(
                readOnly = true,
                value = selectedOption,
                onValueChange = {},
                label = { Text("Select interval") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedOption = option
                            expanded = false
                            val millis = intervalMap[option] ?: 2 * 60 * 60 * 1000L
                            PreferenceHelper.saveReminderInterval(context, millis)
                            WorkScheduler.scheduleReminderWork(context, millis)
                            Toast.makeText(context, "Reminder set every $option", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
