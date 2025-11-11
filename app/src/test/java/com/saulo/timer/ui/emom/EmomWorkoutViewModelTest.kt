package com.saulo.timer.ui.emom

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.saulo.timer.data.WorkoutLogDao
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
class EmomWorkoutViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var soundAndVibrationManager: SoundAndVibrationManager

    @Mock
    private lateinit var preferenceManager: PreferenceManager

    @Mock
    private lateinit var workoutLogDao: WorkoutLogDao

    private lateinit var viewModel: EmomWorkoutViewModel
    private lateinit var testTimerProvider: TestTimerProvider

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        testTimerProvider = TestTimerProvider()
        viewModel = EmomWorkoutViewModel(testTimerProvider, soundAndVibrationManager, preferenceManager, workoutLogDao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startWorkout should transition from Prepare to Work`() = runTest {
        // Arrange
        `when`(preferenceManager.getPreparationTime()).thenReturn(5) // 5 seconds
        viewModel.setup(minutes = 3)

        // Act
        viewModel.startWorkout()

        // Assert
        viewModel.state.test {
            assertEquals(5000, (awaitItem() as EmomState.Prepare).remainingTime)

            // Simulate timer finish
            testTimerProvider.finishTimer()

            val workState = awaitItem() as EmomState.Work
            assertEquals(60000, workState.remainingTime)
            assertEquals(1, workState.currentMinute)
            verify(soundAndVibrationManager).playWorkStartSound()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `work state should transition to next work state`() = runTest {
        // Arrange
        `when`(preferenceManager.getPreparationTime()).thenReturn(0)
        viewModel.setup(minutes = 3)
        viewModel.startWorkout()

        // Assert
        viewModel.state.test {
            assertEquals(1, (awaitItem() as EmomState.Work).currentMinute)

            // Act
            testTimerProvider.finishTimer()

            // Assert
            val nextWorkState = awaitItem() as EmomState.Work
            assertEquals(60000, nextWorkState.remainingTime)
            assertEquals(2, nextWorkState.currentMinute)
            verify(soundAndVibrationManager).playWorkStartSound()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `last work state should transition to finished state and log workout`() = runTest {
        // Arrange
        `when`(preferenceManager.getPreparationTime()).thenReturn(0)
        viewModel.setup(minutes = 1)
        viewModel.startWorkout()

        // Assert
        viewModel.state.test {
            assertEquals(1, (awaitItem() as EmomState.Work).currentMinute)

            // Act
            testTimerProvider.finishTimer()

            // Assert
            assertEquals(EmomState.Finished, awaitItem())
            verify(soundAndVibrationManager).playFinishSound()
            cancelAndConsumeRemainingEvents()
        }

        advanceUntilIdle()

        val captor = argumentCaptor<WorkoutLog>()
        verify(workoutLogDao).insert(captor.capture())
        assertEquals("EMOM", captor.firstValue.workoutType)
        assertEquals(60000, captor.firstValue.durationInMillis)
    }
}
