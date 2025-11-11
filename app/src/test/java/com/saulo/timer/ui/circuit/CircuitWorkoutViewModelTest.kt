package com.saulo.timer.ui.circuit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.saulo.timer.data.WorkoutLogDao
import com.saulo.timer.model.Circuit
import com.saulo.timer.model.Exercise
import com.saulo.timer.model.WorkoutLog
import com.saulo.timer.util.PreferenceManager
import com.saulo.timer.util.SoundAndVibrationManager
import com.saulo.timer.util.TestTimerProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class CircuitWorkoutViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var soundAndVibrationManager: SoundAndVibrationManager

    @Mock
    private lateinit var preferenceManager: PreferenceManager

    @Mock
    private lateinit var workoutLogDao: WorkoutLogDao

    private lateinit var viewModel: CircuitWorkoutViewModel
    private lateinit var testTimerProvider: TestTimerProvider

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        testTimerProvider = TestTimerProvider()
        viewModel = CircuitWorkoutViewModel(testTimerProvider, soundAndVibrationManager, preferenceManager, workoutLogDao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startWorkout should transition from Prepare to Work`() = runTest {
        // Arrange
        val exercise1 = Exercise(name = "Push Ups", workTime1 = 30, workTime2 = null, restBetweenWork = 0, restTime = 15)
        val circuit = Circuit(name = "Test Circuit", exercises = listOf(exercise1), rounds = 1, restBetweenRounds = 60)
        `when`(preferenceManager.getPreparationTime()).thenReturn(5)
        viewModel.setupCircuit(circuit)

        // Act
        viewModel.startWorkout()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value as CircuitWorkoutState.Prepare
        assertEquals(5000, state.remainingTime)

        // Act 2
        testTimerProvider.finishTimer()
        advanceUntilIdle()

        // Assert 2
        val workState = viewModel.state.value as CircuitWorkoutState.Work
        assertEquals(exercise1, workState.exercise)
        assertEquals(30000, workState.remainingTime)
        verify(soundAndVibrationManager).playWorkStartSound()
    }

    @Test
    fun `work should transition to rest then to next work`() = runTest {
        // Arrange
        val exercise1 = Exercise(name = "Push Ups", workTime1 = 30, workTime2 = null, restBetweenWork = 0, restTime = 15)
        val exercise2 = Exercise(name = "Squats", workTime1 = 45, workTime2 = null, restBetweenWork = 0, restTime = 10)
        val circuit = Circuit(name = "Test Circuit", exercises = listOf(exercise1, exercise2), rounds = 1, restBetweenRounds = 60)
        `when`(preferenceManager.getPreparationTime()).thenReturn(0)
        viewModel.setupCircuit(circuit)

        // Act
        viewModel.startWorkout()
        advanceUntilIdle()

        // Assert: Should be in first Work state
        val workState1 = viewModel.state.value as CircuitWorkoutState.Work
        assertEquals(exercise1, workState1.exercise)
        assertEquals(30000, workState1.remainingTime)

        // Act 2: Finish first work
        testTimerProvider.finishTimer()
        advanceUntilIdle()

        // Assert 2: Should be in Rest state
        val restState = viewModel.state.value as CircuitWorkoutState.Rest
        assertEquals(exercise2, restState.nextExercise)
        assertEquals(15000, restState.remainingTime)

        // Act 3: Finish rest
        testTimerProvider.finishTimer()
        advanceUntilIdle()

        // Assert 3: Should be in second Work state
        val workState2 = viewModel.state.value as CircuitWorkoutState.Work
        assertEquals(exercise2, workState2.exercise)
        assertEquals(45000, workState2.remainingTime)
    }

    @Test
    fun `end of round should transition to rest between rounds`() = runTest {
        // Arrange
        val exercise1 = Exercise(name = "Push Ups", workTime1 = 30, workTime2 = null, restBetweenWork = 0, restTime = 15)
        val circuit = Circuit(name = "Test Circuit", exercises = listOf(exercise1), rounds = 2, restBetweenRounds = 60)
        `when`(preferenceManager.getPreparationTime()).thenReturn(0)
        viewModel.setupCircuit(circuit)

        // Act
        viewModel.startWorkout()
        advanceUntilIdle()

        // Assert: Should be in first Work state
        assertEquals(1, viewModel.currentRound.value)
        assertTrue(viewModel.state.value is CircuitWorkoutState.Work)

        // Act 2: Finish first round
        testTimerProvider.finishTimer()
        advanceUntilIdle()

        // Assert 2: Should be in RestBetweenRounds state
        val restState = viewModel.state.value as CircuitWorkoutState.RestBetweenRounds
        assertEquals(60000, restState.remainingTime)

        // Act 3: Finish rest between rounds
        testTimerProvider.finishTimer()
        advanceUntilIdle()

        // Assert 3: Should be in second round's Work state
        assertEquals(2, viewModel.currentRound.value)
        val workState2 = viewModel.state.value as CircuitWorkoutState.Work
        assertEquals(exercise1, workState2.exercise)
    }

    @Test
    fun `last exercise of last round should finish and log workout`() = runTest {
        // Arrange
        val exercise1 = Exercise(name = "Push Ups", workTime1 = 30, workTime2 = null, restBetweenWork = 0, restTime = 15)
        val circuit = Circuit(name = "Test Circuit", exercises = listOf(exercise1), rounds = 1, restBetweenRounds = 60)
        `when`(preferenceManager.getPreparationTime()).thenReturn(0)
        viewModel.setupCircuit(circuit)

        // Act
        viewModel.startWorkout()
        advanceUntilIdle()

        // Assert: Should be in Work state
        assertTrue(viewModel.state.value is CircuitWorkoutState.Work)

        // Act 2: Finish workout
        testTimerProvider.finishTimer()
        advanceUntilIdle()

        // Assert 2: Should be in Finished state
        assertTrue(viewModel.state.value is CircuitWorkoutState.Finished)

        // Assert 3: Verify workout was logged
        val captor = argumentCaptor<WorkoutLog>()
        verify(workoutLogDao).insert(captor.capture())
        assertEquals("Circuito: Test Circuit", captor.firstValue.workoutType)
    }
}
