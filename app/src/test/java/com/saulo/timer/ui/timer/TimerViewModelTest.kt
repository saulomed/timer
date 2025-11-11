package com.saulo.timer.ui.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.saulo.timer.data.WorkoutLogDao
import com.saulo.timer.model.WorkoutLog
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
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class TimerViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var soundAndVibrationManager: SoundAndVibrationManager

    @Mock
    private lateinit var workoutLogDao: WorkoutLogDao

    private lateinit var viewModel: TimerViewModel
    private lateinit var testTimerProvider: TestTimerProvider

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        testTimerProvider = TestTimerProvider()
        viewModel = TimerViewModel(testTimerProvider, soundAndVibrationManager, workoutLogDao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startTimer should change state to Running`() = runTest {
        // Arrange
        viewModel.setup("Test", 10000)
        assertTrue(viewModel.state.value is TimerState.Paused)

        // Act
        viewModel.startTimer()
        advanceUntilIdle()

        // Assert
        val state = viewModel.state.value as TimerState.Running
        assertEquals(10000, state.remainingTime)
    }

    @Test
    fun `pauseTimer should change state to Paused`() = runTest {
        // Arrange
        viewModel.setup("Test", 10000)
        viewModel.startTimer()
        advanceUntilIdle()
        assertTrue(viewModel.state.value is TimerState.Running)

        // Act
        viewModel.pauseTimer()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.state.value is TimerState.Paused)
    }

    @Test
    fun `timer finish should change state to Finished and log workout`() = runTest {
        // Arrange
        viewModel.setup("Test", 10000)
        viewModel.startTimer()
        advanceUntilIdle()

        // Act
        testTimerProvider.finishTimer()
        advanceUntilIdle()

        // Assert
        assertTrue(viewModel.state.value is TimerState.Finished)

        val captor = argumentCaptor<WorkoutLog>()
        verify(workoutLogDao).insert(captor.capture())
        assertEquals("Test", captor.firstValue.workoutType)
        assertEquals(10000, captor.firstValue.durationInMillis)
    }
}
