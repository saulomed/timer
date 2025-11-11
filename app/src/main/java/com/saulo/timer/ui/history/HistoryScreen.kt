package com.saulo.timer.ui.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saulo.timer.model.Circuit
import com.saulo.timer.model.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    circuits: List<Circuit>,
    onStartClick: (Circuit) -> Unit,
    onEditClick: (Circuit) -> Unit,
    onDeleteClick: (Circuit) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Circuitos Salvos") })
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp)
        ) {
            items(circuits) { circuit ->
                CircuitHistoryItem(
                    circuit = circuit,
                    onStart = { onStartClick(circuit) },
                    onEdit = { onEditClick(circuit) },
                    onDelete = { onDeleteClick(circuit) }
                )
            }
        }
    }
}

@Composable
fun CircuitHistoryItem(
    circuit: Circuit,
    onStart: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = circuit.name, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${circuit.rounds} rounds, ${circuit.exercises.size} exercícios",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row {
                IconButton(onClick = onStart) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Iniciar Circuito")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar Circuito")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Deletar Circuito")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    val previewCircuits = listOf(
        Circuit(1, "Treino A", listOf(Exercise("Flexão", 30, null, 15)), 3, 60),
        Circuit(2, "Treino B", listOf(Exercise("Agachamento", 45, null, 20)), 5, 90)
    )
    HistoryScreen(circuits = previewCircuits, onStartClick = {}, onEditClick = {}, onDeleteClick = {})
}
