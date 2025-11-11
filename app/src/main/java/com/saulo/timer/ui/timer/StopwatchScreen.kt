package com.saulo.timer.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saulo.timer.ui.components.CircularTimer
import com.saulo.timer.ui.components.WorkoutControlButton
import com.saulo.timer.util.ImmersiveMode
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel,
    onStop: (duration: Long) -> Unit
) {
    val time by viewModel.time.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = Unit) {
        viewModel.startTimer()
    }

    ImmersiveMode {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Top section for description
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = "Cronômetro crescente, sem tempo definido",
                    style = TextStyle(fontSize = 16.sp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            // Main timer using a Box to overlay text on the circular progress
            Box(
                modifier = Modifier.weight(3f),
                contentAlignment = Alignment.Center
            ) {
                CircularTimer(
                    timeInMillis = time % 60000L, // Progress for the current minute
                    totalTime = 60000L, // Total time is 60 seconds for the ring
                    showTime = false // We show our own formatted time
                )
                Text(
                    text = formatStopwatchTime(time),
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WorkoutControlButton(
                    icon = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isRunning) "Pausar" else "Continuar",
                    onClick = { 
                        scope.launch {
                            if (isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                        }
                    }
                )
                WorkoutControlButton(
                    icon = Icons.Filled.Stop,
                    contentDescription = "Parar",
                    onClick = {
                        scope.launch {
                            viewModel.stopTimer()
                            onStop(viewModel.getFinalDuration())
                        }
                    }
                )
            }
        }
    }
}

private fun formatStopwatchTime(timeInMillis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeInMillis) % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview(showBackground = true)
@Composable
fun StopwatchScreenPreview() {
    val viewModel: StopwatchViewModel = viewModel()
    StopwatchScreen(viewModel = viewModel, onStop = {})
}
