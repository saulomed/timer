package com.saulo.timer.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.saulo.timer.model.Circuit
import com.saulo.timer.ui.amrap.AmrapScreen
import com.saulo.timer.ui.circuit.CircuitScreen
import com.saulo.timer.ui.circuit.CircuitViewModel
import com.saulo.timer.ui.circuit.CircuitWorkoutScreen
import com.saulo.timer.ui.circuit.CircuitWorkoutViewModel
import com.saulo.timer.ui.circuit.SavedCircuitsScreen
import com.saulo.timer.ui.emom.EmomScreen
import com.saulo.timer.ui.emom.EmomWorkoutScreen
import com.saulo.timer.ui.emom.EmomWorkoutViewModel
import com.saulo.timer.ui.fortime.ForTimeScreen
import com.saulo.timer.ui.history.HistoryViewModel
import com.saulo.timer.ui.history.WorkoutLogScreen
import com.saulo.timer.ui.history.WorkoutLogViewModel
import com.saulo.timer.ui.home.HomeScreen
import com.saulo.timer.ui.settings.SettingsScreen
import com.saulo.timer.ui.summary.WorkoutSummaryScreen
import com.saulo.timer.ui.tabata.TabataScreen
import com.saulo.timer.ui.tabata.TabataWorkoutScreen
import com.saulo.timer.ui.tabata.TabataWorkoutViewModel
import com.saulo.timer.ui.timer.StopwatchScreen
import com.saulo.timer.ui.timer.StopwatchViewModel
import com.saulo.timer.ui.timer.TimerScreen
import com.saulo.timer.ui.timer.TimerViewModel
import com.saulo.timer.util.TimerViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val application = LocalContext.current.applicationContext as Application
    val viewModelFactory = TimerViewModelFactory(application)

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onAmrapClick = { navController.navigate("amrap") },
                onForTimeClick = { navController.navigate("fortime") },
                onTabataClick = { navController.navigate("tabata") },
                onEmomClick = { navController.navigate("emom") },
                onCircuitClick = { navController.navigate("circuit") },
                onSavedCircuitsClick = { navController.navigate("saved_circuits") },
                onWorkoutLogClick = { navController.navigate("workout_log") },
                onSettingsClick = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(factory = viewModelFactory)
        }
        composable(
            route = "workout_summary/{workoutType}/{durationInMillis}?roundsCompleted={roundsCompleted}",
            arguments = listOf(
                navArgument("workoutType") { type = NavType.StringType },
                navArgument("durationInMillis") { type = NavType.LongType },
                navArgument("roundsCompleted") { 
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val workoutType = backStackEntry.arguments?.getString("workoutType") ?: ""
            val durationInMillis = backStackEntry.arguments?.getLong("durationInMillis") ?: 0L
            val roundsCompleted = backStackEntry.arguments?.getInt("roundsCompleted")

            WorkoutSummaryScreen(
                workoutType = workoutType,
                durationInMillis = durationInMillis,
                roundsCompleted = if (roundsCompleted != -1) roundsCompleted else null,
                onDoneClick = { 
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        composable("saved_circuits") {
            val historyViewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
            val circuits by historyViewModel.allCircuits.collectAsState()
            SavedCircuitsScreen(
                circuits = circuits,
                onBack = { navController.popBackStack() },
                onStart = {
                    val circuitJson = Gson().toJson(it)
                    navController.navigate("circuit_workout/$circuitJson")
                },
                onEdit = { 
                    navController.navigate("circuit?circuitId=${it.id}")
                },
                onDelete = { historyViewModel.deleteCircuit(it) },
                onCreateNew = { navController.navigate("circuit") },
                onCircuitClick = {
                    val circuitJson = Gson().toJson(it)
                    navController.navigate("circuit_workout/$circuitJson")
                }
            )
        }
        composable("workout_log") {
            val workoutLogViewModel: WorkoutLogViewModel = viewModel(factory = viewModelFactory)
            WorkoutLogScreen(
                viewModel = workoutLogViewModel,
                onBackClick = { navController.popBackStack() },
                onStartWorkoutClick = { navController.navigate("home") }
            )
        }
        composable(
            route = "circuit?circuitId={circuitId}",
            arguments = listOf(navArgument("circuitId") { 
                type = NavType.IntType
                defaultValue = -1
            })
        ) { backStackEntry ->
            val circuitId = backStackEntry.arguments?.getInt("circuitId")
            val circuitViewModel: CircuitViewModel = viewModel(factory = viewModelFactory)
            val scope = rememberCoroutineScope()

            LaunchedEffect(circuitId) {
                if (circuitId != null && circuitId != -1) {
                    circuitViewModel.loadCircuit(circuitId)
                } else {
                    circuitViewModel.clear()
                }
            }

            CircuitScreen(
                viewModel = circuitViewModel,
                onStartClick = {
                    scope.launch {
                        val savedCircuit = circuitViewModel.saveCircuit()
                        val circuitJson = Gson().toJson(savedCircuit)
                        navController.navigate("circuit_workout/$circuitJson") {
                            popUpTo("home")
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("amrap") {
            AmrapScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartClick = { timeInMillis ->
                    navController.navigate("timer/AMRAP/$timeInMillis") {
                        popUpTo("home")
                    }
                }
            )
        }
        composable("fortime") {
            ForTimeScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartClick = { timeInMillis ->
                    if (timeInMillis != null) {
                        navController.navigate("timer/FOR TIME/$timeInMillis") {
                            popUpTo("home")
                        }
                    } else {
                        navController.navigate("stopwatch") {
                            popUpTo("home")
                        }
                    }
                }
            )
        }
        composable("tabata") {
            TabataScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartClick = { rounds, workTime, restTime ->
                    navController.navigate("tabata_workout/$rounds/$workTime/$restTime") {
                        popUpTo("home")
                    }
                }
            )
        }
        composable("emom") {
            EmomScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartClick = { minutes ->
                    navController.navigate("emom_workout/$minutes") {
                        popUpTo("home")
                    }
                }
            )
        }
        composable(
            route = "circuit_workout/{circuitJson}",
            arguments = listOf(navArgument("circuitJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val circuitJson = backStackEntry.arguments?.getString("circuitJson")
            if (circuitJson != null) {
                val circuit = Gson().fromJson(circuitJson, Circuit::class.java)
                val circuitWorkoutViewModel: CircuitWorkoutViewModel = viewModel(factory = viewModelFactory)
                circuitWorkoutViewModel.setupCircuit(circuit)
                CircuitWorkoutScreen(
                    viewModel = circuitWorkoutViewModel,
                    onStop = { navController.popBackStack() },
                    onFinish = {
                        val duration = circuitWorkoutViewModel.getTotalWorkoutTime()
                        val rounds = circuit.rounds
                        navController.navigate("workout_summary/Circuito/${duration}?roundsCompleted=${rounds}") {
                            popUpTo(backStackEntry.destination.id) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(
            route = "emom_workout/{minutes}",
            arguments = listOf(navArgument("minutes") { type = NavType.IntType })
        ) { backStackEntry ->
            val minutes = backStackEntry.arguments?.getInt("minutes") ?: 1
            val emomWorkoutViewModel: EmomWorkoutViewModel = viewModel(factory = viewModelFactory)
            EmomWorkoutScreen(
                viewModel = emomWorkoutViewModel,
                minutes = minutes,
                onStop = { elapsedTime ->
                    navController.navigate("workout_summary/EMOM/${elapsedTime}?roundsCompleted=${minutes}") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                    }
                },
                onFinish = { elapsedTime ->
                    navController.navigate("workout_summary/EMOM/${elapsedTime}?roundsCompleted=${minutes}") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "tabata_workout/{rounds}/{workTime}/{restTime}",
            arguments = listOf(
                navArgument("rounds") { type = NavType.IntType },
                navArgument("workTime") { type = NavType.LongType },
                navArgument("restTime") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val rounds = backStackEntry.arguments?.getInt("rounds") ?: 8
            val workTime = backStackEntry.arguments?.getLong("workTime") ?: 20000
            val restTime = backStackEntry.arguments?.getLong("restTime") ?: 10000
            val tabataWorkoutViewModel: TabataWorkoutViewModel = viewModel(factory = viewModelFactory)
            TabataWorkoutScreen(
                viewModel = tabataWorkoutViewModel,
                rounds = rounds,
                workTime = workTime,
                restTime = restTime,
                onStop = { elapsedTime ->
                    navController.navigate("workout_summary/Tabata/${elapsedTime}?roundsCompleted=${rounds}") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                    }
                },
                onFinish = { elapsedTime ->
                    navController.navigate("workout_summary/Tabata/${elapsedTime}?roundsCompleted=${rounds}") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = "timer/{type}/{time}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("time") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            val time = backStackEntry.arguments?.getLong("time") ?: 0L
            val timerViewModel: TimerViewModel = viewModel(factory = viewModelFactory)
            TimerScreen(
                viewModel = timerViewModel,
                workoutType = type,
                timeInMillis = time,
                onStop = { elapsedTime ->
                    navController.navigate("workout_summary/${type}/${elapsedTime}") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                    }
                },
                onFinish = { elapsedTime ->
                     navController.navigate("workout_summary/${type}/${elapsedTime}") {
                        popUpTo(backStackEntry.destination.id) { inclusive = true }
                    }
                }
            )
        }
        composable("stopwatch") {
            val stopwatchViewModel: StopwatchViewModel = viewModel(factory = viewModelFactory)
            StopwatchScreen(
                viewModel = stopwatchViewModel,
                onStop = { duration ->
                    navController.navigate("workout_summary/Cronômetro/${duration}")
                }
            )
        }
    }
}