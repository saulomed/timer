package com.saulo.timer.ui.tabata

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saulo.timer.ui.components.CircularTimer
import com.saulo.timer.ui.components.PreparationScreen
import com.saulo.timer.ui.theme.restColor
import com.saulo.timer.ui.theme.workColor
import com.saulo.timer.util.ImmersiveMode

private data class TabataDisplayState(
    val stateText: String,
    val stateColor: Color,
    val remainingTime: Long,
    val totalIntervalTime: Long
)

@Composable
fun TabataWorkoutScreen(
    viewModel: TabataWorkoutViewModel,
    rounds: Int,
    workTime: Long,
    restTime: Long,
    onStop: (elapsedTime: Long) -> Unit,
    onFinish: (elapsedTime: Long) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val currentRound by viewModel.currentRound.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()

    LaunchedEffect(key1 = Unit) {
        viewModel.setup(rounds, workTime, restTime)
    }

    LaunchedEffect(key1 = state) {
        if (state is TabataState.Finished) {
            val elapsedTime = viewModel.getFinalElapsedTime()
            onFinish(elapsedTime)
        }
    }

    ImmersiveMode {
        val currentState = state
        if (currentState is TabataState.Prepare) {
            PreparationScreen(
                workoutType = "TABATA",
                workoutDescription = "Faça 8 rounds de 20 segundos de exercício intenso, seguidos por 10 segundos de descanso.",
                remainingTime = currentState.remainingTime,
                onStart = { viewModel.startWorkout() },
                onStop = {
                    val elapsedTime = viewModel.stopWorkoutAndGetElapsedTime()
                    onStop(elapsedTime)
                }
            )
        } else {
            val displayState = when (val s = state) {
                is TabataState.Work -> TabataDisplayState("TRABALHO", MaterialTheme.workColor, s.remainingTime, workTime)
                is TabataState.Rest -> TabataDisplayState("DESCANSO", MaterialTheme.restColor, s.remainingTime, restTime)
                is TabataState.Finished -> TabataDisplayState("CONCLUÍDO", Color.Gray, 0L, 1L) // totalTime can't be 0
                else -> null // Should not happen
            }

            if (displayState != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    // Top section for state and round
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = displayState.stateText,
                            style = TextStyle(fontSize = 40.sp, color = displayState.stateColor, fontWeight = FontWeight.Bold)
                        )
                        if (state !is TabataState.Finished) {
                            Text(
                                text = "Round: $currentRound / $rounds",
                                style = TextStyle(fontSize = 24.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Main timer
                    CircularTimer(
                        timeInMillis = displayState.remainingTime,
                        totalTime = displayState.totalIntervalTime,
                        progressColor = displayState.stateColor,
                        strokeWidth = 10.dp
                    )

                    // Control buttons
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                                enabled = state !is TabataState.Finished
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
                                enabled = state !is TabataState.Finished
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
}

@Preview(showBackground = true)
@Composable
fun TabataWorkoutScreenPreview() {
    val viewModel: TabataWorkoutViewModel = viewModel()
    TabataWorkoutScreen(viewModel = viewModel, rounds = 8, workTime = 20000, restTime = 10000, onStop = {}, onFinish = {})
}
