package com.example.buildingfexfrontend.core.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import com.example.buildingfexfrontend.core.i18n.string
import com.example.buildingfexfrontend.core.util.Dates
import java.time.LocalDate

@Composable
private fun readOnlyColors() = OutlinedTextFieldDefaults.colors(
    disabledTextColor = MaterialTheme.colorScheme.onSurface,
    disabledBorderColor = MaterialTheme.colorScheme.outline,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
)

/** Read-only field that opens the standard Android date picker. Value stays `yyyy-MM-dd`. */
@Composable
fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier.clickable(enabled = enabled, role = Role.Button) {
            val date = Dates.ymdOrNull(value) ?: LocalDate.now()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    onValueChange("%04d-%02d-%02d".format(year, month + 1, dayOfMonth))
                },
                date.year,
                date.monthValue - 1,
                date.dayOfMonth,
            ).show()
        },
    ) {
        OutlinedTextField(
            value = Dates.displayDate(value),
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text(string("ui.datePlaceholder")) },
            readOnly = true,
            enabled = false,
            trailingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
            colors = readOnlyColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Read-only field that opens the standard Android time picker (24 h). Value stays `HH:mm`. */
@Composable
fun TimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier.clickable(enabled = enabled, role = Role.Button) {
            val hour = value.substringBefore(':').toIntOrNull()?.coerceIn(0, 23) ?: 9
            val minute = value.substringAfter(':', "").trim().toIntOrNull()?.coerceIn(0, 59) ?: 0
            TimePickerDialog(
                context,
                { _, selectedHour, selectedMinute ->
                    onValueChange("%02d:%02d".format(selectedHour, selectedMinute))
                },
                hour,
                minute,
                true,
            ).show()
        },
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text("HH:mm") },
            readOnly = true,
            enabled = false,
            trailingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
            colors = readOnlyColors(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
