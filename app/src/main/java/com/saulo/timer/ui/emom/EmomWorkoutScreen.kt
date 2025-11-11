package com.saulo.timer.ui.emom

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.saulo.timer.ui.components.WorkoutControlButton
import com.saulo.timer.ui.theme.workColor
import com.saulo.timer.util.ImmersiveMode

private data class EmomDisplayState(
    val titleText: String,
    val titleColor: Color,
    val remainingTime: Long,
    val totalIntervalTime: Long,
    val progressColor: Color
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
                is EmomState.Work -> EmomDisplayState("Minuto: ${s.currentMinute} / $minutes", MaterialTheme.colorScheme.onSurface, s.remainingTime, 60000L, MaterialTheme.workColor)
                is EmomState.Finished -> EmomDisplayState("CONCLUÍDO", Color.Gray, 0L, 1L, Color.Gray)
                else -> null // Should not happen
            }

            if (displayState != null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    // Top section for minute counter
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayState.titleText,
                            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                            color = displayState.titleColor
                        )
                    }

                    // Main timer
                    CircularTimer(
                        timeInMillis = displayState.remainingTime,
                        totalTime = displayState.totalIntervalTime,
                        progressColor = displayState.progressColor
                    )

                    // Control buttons
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            WorkoutControlButton(
                                icon = if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isRunning) "Pausar" else "Continuar",
                                onClick = {
                                    if (isRunning) viewModel.pauseWorkout() else viewModel.startWorkout()
                                },
                                enabled = state !is EmomState.Finished
                            )
                            WorkoutControlButton(
                                icon = Icons.Filled.Stop,
                                contentDescription = "Parar",
                                onClick = {
                                    val elapsedTime = viewModel.stopWorkoutAndGetElapsedTime()
                                    onStop(elapsedTime)
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        WorkoutControlButton(
                            icon = Icons.Filled.FastForward,
                            contentDescription = "Pular",
                            onClick = { viewModel.skip() },
                            enabled = state !is EmomState.Finished
                        )
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
