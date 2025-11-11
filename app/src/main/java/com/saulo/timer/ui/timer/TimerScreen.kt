package com.saulo.timer.ui.timer

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulo.timer.ui.theme.TimerTheme
import com.saulo.timer.util.ImmersiveMode
import com.saulo.timer.util.formatTime
import androidx.compose.foundation.layout.safeDrawingPadding
import kotlin.math.ceil

/**
 * A tela principal do timer, que gerencia o estado e exibe a interface do usuário.
 * Esta tela decide condicionalmente se deve exibir a sobreposição de preparação
 * ou o conteúdo principal do timer com base no estado do ViewModel.
 *
 * @param viewModel O [TimerViewModel] que gerencia a lógica e o estado do timer.
 * @param workoutType O tipo de treino a ser exibido na tela (ex: "AMRAP").
 * @param timeInMillis O tempo total do timer em milissegundos.
 * @param onStop Uma função de callback que é chamada quando o timer é parado pelo usuário.
 * @param onFinish Uma função de callback que é chamada quando o timer chega ao fim.
 */
@Composable
fun TimerScreen(
    viewModel: TimerViewModel,
    workoutType: String,
    timeInMillis: Long,
    onStop: (elapsedTime: Long) -> Unit,
    onFinish: (elapsedTime: Long) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = timeInMillis, key2 = workoutType) {
        viewModel.setup(workoutType, timeInMillis)
    }

    LaunchedEffect(key1 = state) {
        if (state is TimerState.Finished) {
            onFinish(timeInMillis)
        }
    }

    ImmersiveMode {
        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val remainingTime = when (val s = state) {
                    is TimerState.Running -> s.remainingTime
                    is TimerState.Paused -> s.remainingTime
                    else -> 0L
                }

                if (viewModel.isPreparing) {
                    PreparationOverlay(
                        workoutType = workoutType,
                        workoutDescription = getWorkoutDescription(workoutType),
                        remainingTime = remainingTime,
                        onStart = { viewModel.startTimer() },
                        onStop = {
                            viewModel.stopTimer()
                            onStop(0)
                        }
                    )
                } else {
                    TimerContent(
                        workoutType = workoutType,
                        remainingTime = remainingTime,
                        totalTime = timeInMillis,
                        isRunning = viewModel.isRunning,
                        onToggle = {
                            if (viewModel.isRunning) viewModel.pauseTimer() else viewModel.startTimer()
                        },
                        onStop = {
                            viewModel.stopTimer()
                            onStop(viewModel.getElapsedTime())
                        }
                    )
                }
            }
        }
    }
}

/**
 * Exibe o conteúdo principal do timer, incluindo o tipo de treino, o progresso circular e os botões de controle.
 *
 * @param workoutType O tipo de treino a ser exibido.
 * @param remainingTime O tempo restante do timer em milissegundos.
 * @param totalTime O tempo total do timer em milissegundos.
 * @param isRunning Um booleano que indica se o timer está atualmente em execução.
 * @param onToggle Uma função de callback para alternar entre os estados de play e pause do timer.
 * @param onStop Uma função de callback para parar o timer.
 */
@Composable
fun TimerContent(
    workoutType: String,
    remainingTime: Long,
    totalTime: Long,
    isRunning: Boolean,
    onToggle: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = workoutType,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = getWorkoutDescription(workoutType),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
        }

        CircularProgress(
            time = formatTime(remainingTime),
            progress = (remainingTime.toFloat() / totalTime.toFloat()),
            modifier = Modifier.size(256.dp),
            strokeWidth = 10.dp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onToggle,
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isRunning) "Pausar" else "Continuar",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Button(
                    onClick = onStop,
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
            }
        }
    }
}

/**
 * Exibe uma sobreposição de preparação antes do início do timer principal,
 * alinhada com o design do protótipo.
 *
 * @param workoutType O tipo de treino a ser exibido.
 * @param workoutDescription A descrição do tipo de treino.
 * @param remainingTime O tempo restante da contagem regressiva de preparação em milissegundos.
 * @param onStart Uma função de callback para iniciar o timer imediatamente.
 * @param onStop Uma função de callback para cancelar a preparação e parar o timer.
 */
@Composable
fun PreparationOverlay(
    workoutType: String,
    workoutDescription: String,
    remainingTime: Long,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = workoutType,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = workoutDescription,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp, start = 16.dp, end = 16.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Prepare-se",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            CircularProgress(
                time = ceil(remainingTime / 1000.0).toLong().toString(),
                progress = 1f - (remainingTime.toFloat() / 10000f),
                modifier = Modifier.size(256.dp),
                strokeWidth = 10.dp
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onStart,
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = "Iniciar",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Button(
                    onClick = onStop,
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
            }
        }
    }
}

/**
 * Um Composable que exibe um progresso circular animado com um texto de tempo no centro.
 * O texto é garantido para ser desenhado sobre os arcos do progresso.
 *
 * @param time O texto do tempo a ser exibido no centro do círculo.
 * @param progress O progresso atual a ser exibido pelo círculo (de 0.0f a 1.0f).
 * @param modifier O [Modifier] a ser aplicado a este Composable.
 * @param strokeWidth A espessura do traço do círculo de progresso, em Dp.
 */
@Composable
fun CircularProgress(
    time: String,
    progress: Float,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 12.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val animatedProgress by animateFloatAsState(targetValue = progress, label = "ProgressAnimation")
        val primaryColor = MaterialTheme.colorScheme.primary
        val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()

            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidthPx)
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = 360 * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )
        }

        Text(
            modifier = Modifier.zIndex(1f),
            text = time,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
            fontSize = if (time.length > 5) 60.sp else 80.sp,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Retorna a descrição para um determinado tipo de treino.
 *
 * @param workoutType O tipo de treino (ex: "AMRAP").
 * @return A descrição correspondente ao tipo de treino.
 */
private fun getWorkoutDescription(workoutType: String): String {
    return when (workoutType) {
        "AMRAP" -> "Realize o máximo de rounds possíveis no tempo determinado."
        "EMOM" -> "Execute um exercício a cada minuto."
        "TABATA" -> "Faça 8 rounds de 20 segundos de exercício intenso, seguidos por 10 segundos de descanso."
        "FOR TIME" -> "Complete a tarefa o mais rápido possível."
        else -> "Prepare-se para o seu treino."
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun TimerScreenPreview() {
    TimerTheme(darkTheme = true) {
        TimerContent(
            workoutType = "AMRAP",
            remainingTime = 1199000, // 19:59
            totalTime = 1200000, // 20:00
            isRunning = false,
            onToggle = {},
            onStop = {}
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreparationOverlayPreview() {
    TimerTheme(darkTheme = true) {
        PreparationOverlay(
            workoutType = "AMRAP",
            workoutDescription = getWorkoutDescription("AMRAP"),
            remainingTime = 8000,
            onStart = {},
            onStop = {}
        )
    }
}
