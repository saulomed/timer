package com.saulo.timer.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.saulo.timer.util.TimerViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    factory: TimerViewModelFactory
) {
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val preparationTime by viewModel.preparationTime.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Configurações") })
        }
    ) {
        Column(
            modifier = Modifier.padding(it).padding(16.dp)
        ) {
            Text(
                text = "Tempo de Preparação",
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Slider(
                    value = preparationTime.toFloat(),
                    onValueChange = { newValue ->
                        viewModel.setPreparationTime(newValue.toInt())
                    },
                    valueRange = 0f..30f,
                    steps = 29,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$preparationTime s",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
