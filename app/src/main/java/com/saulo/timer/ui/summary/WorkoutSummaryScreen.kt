package com.saulo.timer.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.concurrent.TimeUnit

@Composable
fun WorkoutSummaryScreen(
    workoutType: String,
    durationInMillis: Long,
    roundsCompleted: Int?,
    onDoneClick: () -> Unit
) {
    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Treino Concluído!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            SummaryDetail(label = "Tipo de Treino", value = workoutType)
            SummaryDetail(label = "Duração Total", value = formatDuration(durationInMillis))
            roundsCompleted?.let {
                SummaryDetail(label = "Rounds Completados", value = it.toString())
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(onClick = onDoneClick) {
                Text(text = "Voltar ao Início", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun SummaryDetail(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 20.sp
        )
    }
}

private fun formatDuration(timeInMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60

    return if (hours > 0) {
        String.format("%d h %02d min %02d s", hours, minutes, seconds)
    } else {
        String.format("%02d min %02d s", minutes, seconds)
    }
}
