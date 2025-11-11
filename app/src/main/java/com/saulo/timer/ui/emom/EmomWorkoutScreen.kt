package com.saulo.timer.ui.emom

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saulo.timer.ui.components.CircularTimer
import com.saulo.timer.ui.components.PreparationScreen
import com.saulo.timer.ui.theme.workColor
import com.saulo.timer.util.ImmersiveMode
import com.saulo.timer.util.formatTime

private data class EmomDisplayState(
    val remainingTime: Long,
    val totalIntervalTime: Long,
    val progressColor: Color,
    val currentMinute: Int
)

@Composable
fun EmomWorkoutScreen(
    viewModel: EmomWorkoutViewModel,
    minutes: Int,
    onStop: (elapsedTime: Long) -> Unit,
    onFinish: (elapsedTime: Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.setup(minutes)
    }

    LaunchedEffect(key1 = state) {
        if (state is EmomState.Finished) {
            val elapsedTime = viewModel.getFinalElapsedTime()
            onFinish(elapsedTime)
        }
    }

    ImmersiveMode {
        val currentState = state
        if (currentState is EmomState.Prepare) {
            PreparationScreen(
                workoutType = "EMOM",
                workoutDescription = "Execute um exercício a cada minuto.",
                remainingTime = currentState.remainingTime,
                onStart = { viewModel.startWorkout() },
                onStop = {
                    val elapsedTime = viewModel.stopWorkoutAndGetElapsedTime()
                    onStop(elapsedTime)
                }
            )
        } else {
            val displayState = when (val s = state) {
                is EmomState.Work -> EmomDisplayState(s.remainingTime, 60000L, MaterialTheme.workColor, s.currentMinute)
                is EmomState.Finished -> EmomDisplayState(0L, 1L, Color.Gray, minutes)
                else -> null // Should not happen
            }

            if (displayState != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "EMOM",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Execute um exercício a cada minuto.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
                        )
                    }

                    // Main timer
                    Box(contentAlignment = Alignment.Center) {
                        CircularTimer(
                            timeInMillis = displayState.remainingTime,
                            totalTime = displayState.totalIntervalTime,
                            progressColor = displayState.progressColor,
                            strokeWidth = 10.dp
                        )
                        Text(
                            text = formatTime(displayState.remainingTime),
                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Footer
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Minuto Atual", style = MaterialTheme.typography.labelMedium)
                        Text(text = "${displayState.currentMinute}/$minutes", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    }

                    // Control buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                val elapsedTime = viewModel.stopWorkoutAndGetElapsedTime()
                                onStop(elapsedTime)
                            },
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                Icons.Filled.Stop,
                                contentDescription = "Parar",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Button(
                            onClick = {
                                if (isRunning) viewModel.pauseWorkout() else viewModel.startWorkout()
                            },
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            enabled = state !is EmomState.Finished
                        ) {
                            Icon(
                                if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isRunning) "Pausar" else "Continuar",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Button(
                            onClick = { viewModel.skip() },
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                            enabled = state !is EmomState.Finished
                        ) {
                            Icon(
                                Icons.Filled.FastForward,
                                contentDescription = "Pular",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EmomWorkoutScreenPreview() {
    val viewModel: EmomWorkoutViewModel = viewModel()
    // EmomWorkoutScreen(viewModel = viewModel, minutes = 10, onStop = {}, onFinish = {})
}
