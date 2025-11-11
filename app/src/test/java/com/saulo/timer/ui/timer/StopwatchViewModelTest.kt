package com.saulo.timer.ui.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.saulo.timer.data.WorkoutLogDao
import com.saulo.timer.model.WorkoutLog
import com.saulo.timer.util.SoundAndVibrationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class StopwatchViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var soundAndVibrationManager: SoundAndVibrationManager

    @Mock
    private lateinit var workoutLogDao: WorkoutLogDao

    private lateinit var viewModel: StopwatchViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = StopwatchViewModel(soundAndVibrationManager, workoutLogDao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startTimer should increment time`() = runTest {
        // Act
        viewModel.startTimer()
        assertTrue(viewModel.isRunning.value)

        // Advance time by 3 seconds
        advanceTimeBy(3001)

        // Assert
        assertEquals(3000, viewModel.time.value)
    }

    @Test
    fun `pauseTimer should stop incrementing time`() = runTest {
        // Arrange
        viewModel.startTimer()
        advanceTimeBy(2001)
        assertEquals(2000, viewModel.time.value)

        // Act
        viewModel.pauseTimer()
        assertFalse(viewModel.isRunning.value)

        // Advance time and check that it didn't change
        advanceTimeBy(3001)
        assertEquals(2000, viewModel.time.value)
    }

    @Test
    fun `stopTimer should reset time and log workout`() = runTest {
        // Arrange
        viewModel.startTimer()
        advanceTimeBy(5001)
        assertEquals(5000, viewModel.time.value)

        // Act
        viewModel.stopTimer()
        advanceUntilIdle() // For the logWorkout coroutine

        // Assert
        assertEquals(0, viewModel.time.value)
        assertFalse(viewModel.isRunning.value)

        val captor = argumentCaptor<WorkoutLog>()
        verify(workoutLogDao).insert(captor.capture())
        assertEquals("FOR TIME (Stopwatch)", captor.firstValue.workoutType)
        assertEquals(5000, captor.firstValue.durationInMillis)
    }
}
