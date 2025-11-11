package com.saulo.timer.ui.circuit

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saulo.timer.model.Exercise
import com.saulo.timer.ui.theme.TimerTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircuitScreen(
    viewModel: CircuitViewModel,
    onStartClick: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Configuração Circuito", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp)) // To balance the title
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f))
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = onStartClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Text(
                        text = "Iniciar Treino",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Crie seu treino de circuito personalizado definindo nome, rounds, descansos e exercícios.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextField(
                        value = viewModel.circuitName,
                        onValueChange = { viewModel.circuitName = it },
                        label = { Text("Nome do Circuito") },
                        placeholder = { Text("Ex: Treino de Segunda") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                MaterialTheme.shapes.medium
                            )
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Quantidade de Rounds", modifier = Modifier.weight(1f))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { 
                                    val currentRounds = viewModel.rounds.toIntOrNull() ?: 0
                                    if (currentRounds > 1) viewModel.rounds = (currentRounds - 1).toString()
                                },
                                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Text("-")
                            }
                            Text(viewModel.rounds, fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { 
                                    val currentRounds = viewModel.rounds.toIntOrNull() ?: 0
                                    viewModel.rounds = (currentRounds + 1).toString()
                                 },
                                modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Text("+")
                            }
                        }
                    }
                    TextField(
                        value = viewModel.restBetweenRounds,
                        onValueChange = { viewModel.restBetweenRounds = it },
                        label = { Text("Tempo de descanso entre Rounds") },
                        trailingIcon = { Text("s") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("Exercícios", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
            }

            itemsIndexed(viewModel.exercises) { index, exercise ->
                ExerciseItem(
                    exercise = exercise,
                    isLastExercise = index == viewModel.exercises.lastIndex,
                    onExerciseChange = { updatedExercise ->
                        viewModel.updateExercise(index, updatedExercise)
                    },
                    onRemoveClick = { viewModel.removeExercise(exercise) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                OutlinedButton(
                    onClick = { viewModel.addExercise() },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar Exercício", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Adicionar Exercício", color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { 
                    scope.launch {
                        viewModel.saveCircuit()
                        Toast.makeText(context, "Circuito salvo!", Toast.LENGTH_SHORT).show()
                        onNavigateBack()
                    }
                }) {
                    Text("Salvar")
                }
            }
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: Exercise,
    isLastExercise: Boolean,
    onExerciseChange: (Exercise) -> Unit,
    onRemoveClick: () -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), MaterialTheme.shapes.extraLarge)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextField(
                value = exercise.name,
                onValueChange = { onExerciseChange(exercise.copy(name = it)) },
                placeholder = { Text("Nome do Exercício") },
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            )
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Delete, contentDescription = "Remover Exercício", tint = Color.Red)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextField(
                value = exercise.workTime1.toString(),
                onValueChange = { onExerciseChange(exercise.copy(workTime1 = it.toLongOrNull() ?: 0)) },
                label = { Text("Tempo de trabalho") },
                trailingIcon = { Text("s") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                 colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                )
            )
            TextField(
                value = exercise.workTime2?.toString() ?: "",
                onValueChange = { onExerciseChange(exercise.copy(workTime2 = it.toLongOrNull())) },
                label = { Text("Tempo de trabalho 2") },
                trailingIcon = { Text("s") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                enabled = true,
                 colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                )
            )
        }
        if ((exercise.workTime2 ?: 0) > 0) {
            Box {
                TextField(
                    value = "${exercise.restBetweenWork ?: 5} s",
                    onValueChange = {}, // Not editable
                    label = { Text("Desc. entre Trabalhos") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                     colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    )
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { isDropdownExpanded = true }
                )
                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    (0..10).forEach { time ->
                        DropdownMenuItem(
                            text = { Text("$time s") },
                            onClick = {
                                onExerciseChange(exercise.copy(restBetweenWork = time))
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (!isLastExercise) {
            TextField(
                value = exercise.restTime.toString(),
                onValueChange = { onExerciseChange(exercise.copy(restTime = it.toLongOrNull() ?: 0)) },
                label = { Text("Descanso entre exercícios") },
                trailingIcon = { Text("s") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                 colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                )
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun CircuitScreenPreview() {
    TimerTheme(darkTheme = true) {
        // Preview is limited as it cannot instantiate the ViewModel or navigation
        // You can create a dummy ViewModel for preview purposes if needed
    }
}
