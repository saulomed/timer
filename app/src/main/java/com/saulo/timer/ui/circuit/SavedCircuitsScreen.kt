package com.saulo.timer.ui.circuit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saulo.timer.model.Circuit
import com.saulo.timer.model.Exercise
import com.saulo.timer.ui.theme.TimerTheme
import kotlin.math.roundToLong

/**
 * Composable function for the Saved Circuits screen.
 *
 * This screen displays a list of saved circuits. The user can start, edit, or delete a circuit.
 * If no circuits are saved, it shows an empty state with a button to create a new circuit.
 *
 * @param circuits The list of saved circuits to display.
 * @param onBack Callback to navigate back.
 * @param onStart Callback to start a circuit.
 * @param onEdit Callback to edit a circuit.
 * @param onDelete Callback to delete a circuit.
 * @param onCreateNew Callback to create a new circuit.
 * @param onCircuitClick Callback to handle circuit click.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedCircuitsScreen(
    circuits: List<Circuit>,
    onBack: () -> Unit,
    onStart: (Circuit) -> Unit,
    onEdit: (Circuit) -> Unit,
    onDelete: (Circuit) -> Unit,
    onCreateNew: () -> Unit,
    onCircuitClick: (Circuit) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Circuitos Salvos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            )
        }
    ) { paddingValues ->
        if (circuits.isEmpty()) {
            EmptyState(onCreateNew = onCreateNew, modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(circuits) { circuit ->
                    CircuitCard(
                        circuit = circuit,
                        onStart = { onStart(circuit) },
                        onEdit = { onEdit(circuit) },
                        onDelete = { onDelete(circuit) },
                        onClick = { onCircuitClick(circuit) }
                    )
                }
            }
        }
    }
}

/**
 * Composable for a single circuit card.
 *
 * @param circuit The circuit data to display.
 * @param onStart Callback for the start button.
 * @param onEdit Callback for the edit button.
 * @param onDelete Callback for the delete button.
 * @param onClick Callback for when the card is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CircuitCard(
    circuit: Circuit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = circuit.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            val details = if (circuit.rounds > 0) {
                "${circuit.rounds} rounds, ${circuit.exercises.size} exercícios"
            } else {
                val durationInSeconds = circuit.exercises.sumOf { (it.workTime1 + (it.workTime2 ?: 0)).toDouble() } / 1000
                "Duração: ${durationInSeconds.roundToLong()}s"
            }
            Text(
                text = details,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStart,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Iniciar Treino")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Iniciar Treino")
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Deletar", tint = Color.Red)
                    }
                }
            }
        }
    }
}

/**
 * Composable for the empty state of the Saved Circuits screen.
 *
 * @param onCreateNew Callback for the create new circuit button.
 * @param modifier Modifier for this composable.
 */
@Composable
fun EmptyState(onCreateNew: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Nenhum circuito encontrado",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Crie e salve seus treinos personalizados para acessá-los aqui.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCreateNew,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Criar Novo Circuito")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SavedCircuitsScreenPreview() {
    val sampleExercises = listOf(
        Exercise(name = "Ex1", workTime1 = 30000, restTime = 10000),
        Exercise(name = "Ex2", workTime1 = 20000, workTime2 = 10000, restTime = 10000)
    )
    val sampleCircuits = listOf(
        Circuit(id = 1, name = "Treino de Corpo Inteiro", rounds = 3, exercises = sampleExercises, restBetweenRounds = 60000L),
        Circuit(id = 2, name = "HIIT Rápido", rounds = 0, exercises = sampleExercises, restBetweenRounds = 60000L)
    )
    TimerTheme {
        SavedCircuitsScreen(
            circuits = sampleCircuits,
            onBack = {},
            onStart = {},
            onEdit = {},
            onDelete = {},
            onCreateNew = {},
            onCircuitClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptySavedCircuitsScreenPreview() {
    TimerTheme {
        SavedCircuitsScreen(
            circuits = emptyList(),
            onBack = {},
            onStart = {},
            onEdit = {},
            onDelete = {},
            onCreateNew = {},
            onCircuitClick = {}
        )
    }
}