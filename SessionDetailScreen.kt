package com.example.bluetooth.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetooth.model.data.MeasurementSession
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SessionDetailScreen(
    sessions: List<MeasurementSession>,
    onItemClick: (MeasurementSession) -> Unit,
    onExportClick: (MeasurementSession) -> Unit,
    onDeleteClick: (MeasurementSession) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sessions) { session ->
            SessionItem(
                session = session,
                onItemClick = { onItemClick(session) },
                onExportClick = { onExportClick(session) },
                onDeleteClick = { onDeleteClick(session) }
            )
        }
    }
}

@Composable
private fun SessionItem(
    session: MeasurementSession,
    onItemClick: () -> Unit,
    onExportClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date
            Text(
                text = formatDate(session.startTime),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Sensor Type
            Text(
                text = "Sensor: ${session.sensorType.name}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Duration
            Text(
                text = "Duration: ${session.duration / 1000}s",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onExportClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export")
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete"
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}