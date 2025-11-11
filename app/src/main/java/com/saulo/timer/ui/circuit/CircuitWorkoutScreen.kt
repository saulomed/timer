package com.saulo.timer.ui.circuit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulo.timer.ui.components.CircularTimer
import com.saulo.timer.ui.components.PreparationScreen
import com.saulo.timer.ui.theme.TimerTheme
import com.saulo.timer.ui.theme.restBetweenRoundsColor
import com.saulo.timer.ui.theme.restColor
import com.saulo.timer.ui.theme.workColor
import com.saulo.timer.util.ImmersiveMode
import com.saulo.timer.util.formatTime

private data class TimerDisplayState(
    val stateText: String,
    val stateColor: Color,
    val remainingTime: Long,
    val totalIntervalTime: Long,
    val nextUpText: String,
    val showRoundInfo: Boolean = false,
    val currentRound: Int = 0,
    val totalRounds: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircuitWorkoutScreen(
    viewModel: CircuitWorkoutViewModel,
    onStop: () -> Unit,
    onFinish: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val isSecondWorkInterval by viewModel.isSecondWorkInterval.collectAsState()

    LaunchedEffect(key1 = state) {
        if (state is CircuitWorkoutState.Finished) {
            onFinish()
        }
    }

    ImmersiveMode {
        val currentState = state

        if (currentState is CircuitWorkoutState.Prepare) {
            PreparationScreen(
                workoutType = viewModel.circuit.name,
                workoutDescription = "Prepare-se para o seu treino.",
                remainingTime = currentState.remainingTime,
                onStart = { viewModel.startWorkout() },
                onStop = { viewModel.stopWorkout(); onStop() }
            )
        } else {
            Scaffold(
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = viewModel.circuit.name,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onStop) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                            }
                        },
                        actions = {
                            Spacer(modifier = Modifier.width(48.dp)) // To balance the title
                        }
                    )
                }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(it).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (currentState != null && viewModel.isCircuitInitialized) {
                        val displayState = when (currentState) {
                            is CircuitWorkoutState.Work -> {
                                val workTime = if (isSecondWorkInterval) currentState.exercise.workTime2!! * 1000 else currentState.exercise.workTime1 * 1000
                                val title = if (isSecondWorkInterval) currentState.exercise.name else currentState.exercise.name
                                TimerDisplayState(title, MaterialTheme.workColor, currentState.remainingTime, workTime, "Próximo: Descanso", true, viewModel.currentRound.value, viewModel.circuit.rounds)
                            }
                            is CircuitWorkoutState.RestBetweenWork -> {
                                val restTime = currentState.remainingTime
                                TimerDisplayState("Descanso", MaterialTheme.colorScheme.tertiary, restTime, viewModel.circuit.exercises[viewModel.currentExerciseIndex].restBetweenWork!! * 1000L, "Próximo: Trabalho 2", true, viewModel.currentRound.value, viewModel.circuit.rounds)
                            }
                            is CircuitWorkoutState.Rest -> TimerDisplayState("Descanso", MaterialTheme.restColor, currentState.remainingTime, viewModel.circuit.exercises.getOrNull(viewModel.circuit.exercises.indexOf(currentState.nextExercise) -1)?.restTime?.times(1000) ?: 1000L, "Próximo: ${currentState.nextExercise.name}", true, viewModel.currentRound.value, viewModel.circuit.rounds)
                            is CircuitWorkoutState.RestBetweenRounds -> TimerDisplayState("Descanso entre Rounds", MaterialTheme.restBetweenRoundsColor, currentState.remainingTime, viewModel.circuit.restBetweenRounds * 1000, "Próximo Round", true, viewModel.currentRound.value, viewModel.circuit.rounds)
                            is CircuitWorkoutState.Finished -> TimerDisplayState("Concluído", Color.Gray, 0L, 1L, "")
                            else -> null // Should not happen for Prepare
                        }

                        if (displayState != null) {
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = displayState.stateText,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                if(displayState.showRoundInfo) {
                                    Text(text = "Round: ${displayState.currentRound} / ${displayState.totalRounds}", fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(contentAlignment = Alignment.Center) {
                                    CircularTimer(
                                        timeInMillis = displayState.remainingTime,
                                        totalTime = displayState.totalIntervalTime,
                                        progressColor = displayState.stateColor,
                                        modifier = Modifier.size(300.dp)
                                    )
                                    Text(
                                        text = formatTime(displayState.remainingTime),
                                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            // Controls and Next Exercise
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { if (isRunning) viewModel.pauseWorkout() else viewModel.startWorkout() },
                                        modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                                        enabled = currentState !is CircuitWorkoutState.Finished
                                    ) {
                                        Icon(if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow, contentDescription = if (isRunning) "Pausar" else "Continuar", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(48.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(
                                        onClick = { viewModel.stopWorkout(); onStop() },
                                        modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(Icons.Filled.Stop, contentDescription = "Parar", tint = Color.Red, modifier = Modifier.size(36.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    IconButton(
                                        onClick = { viewModel.skip() },
                                        modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), CircleShape),
                                        enabled = currentState !is CircuitWorkoutState.Finished
                                    ) {
                                        Icon(Icons.Filled.SkipNext, contentDescription = "Pular", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(36.dp))
                                    }
                                }
                                if (displayState.nextUpText.isNotBlank()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), MaterialTheme.shapes.large)
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Próximo Exercício",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = displayState.nextUpText,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                    } else {
                        Text(
                            text = "Carregando circuito...",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun CircuitWorkoutScreenPreview() {
    TimerTheme(darkTheme = true) {
        // Preview is limited
    }
}
