package com.saulo.timer.ui.tabata

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
class TabataWorkoutViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var soundAndVibrationManager: SoundAndVibrationManager

    @Mock
    private lateinit var preferenceManager: PreferenceManager

    @Mock
    private lateinit var workoutLogDao: WorkoutLogDao

    private lateinit var viewModel: TabataWorkoutViewModel
    private lateinit var testTimerProvider: TestTimerProvider

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        testTimerProvider = TestTimerProvider()
        viewModel = TabataWorkoutViewModel(testTimerProvider, soundAndVibrationManager, preferenceManager, workoutLogDao, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startWorkout should transition from Prepare to Work`() = runTest {
        // Arrange
        `when`(preferenceManager.getPreparationTime()).thenReturn(5) // 5 seconds
        viewModel.setup(rounds = 8, work = 20000, rest = 10000)

        // Act
        viewModel.startWorkout()

        // Assert
        viewModel.state.test {
            assertEquals(5000, (awaitItem() as TabataState.Prepare).remainingTime)

            // Simulate timer finish
            testTimerProvider.finishTimer()

            assertEquals(20000, (awaitItem() as TabataState.Work).remainingTime)
            verify(soundAndVibrationManager).playWorkStartSound()
            verify(soundAndVibrationManager).vibrate()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `work state should transition to rest state`() = runTest {
        // Arrange
        `when`(preferenceManager.getPreparationTime()).thenReturn(0)
        viewModel.setup(rounds = 2, work = 20000, rest = 10000)
        viewModel.startWorkout()

        // Assert
        viewModel.state.test {
            assertEquals(20000, (awaitItem() as TabataState.Work).remainingTime)

            // Act
            testTimerProvider.finishTimer()

            // Assert
            assertEquals(10000, (awaitItem() as TabataState.Rest).remainingTime)
            verify(soundAndVibrationManager).playWorkEndSound()
            verify(soundAndVibrationManager).playRestSound()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `rest state should transition to work state and increment round`() = runTest {
        // Arrange
        `when`(preferenceManager.getPreparationTime()).thenReturn(0)
        viewModel.setup(rounds = 2, work = 20000, rest = 10000)
        viewModel.startWorkout() // Starts in Work
        testTimerProvider.finishTimer() // Work -> Rest

        // Assert
        viewModel.state.test {
            assertEquals(10000, (awaitItem() as TabataState.Rest).remainingTime)
            assertEquals(1, viewModel.currentRound.value)

            // Act
            testTimerProvider.finishTimer()

            // Assert
            assertEquals(20000, (awaitItem() as TabataState.Work).remainingTime)
            assertEquals(2, viewModel.currentRound.value)
            verify(soundAndVibrationManager).playWorkStartSound()
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `last work state should transition to finished state and log workout`() = runTest {
        // Arrange
        `when`(preferenceManager.getPreparationTime()).thenReturn(0)
        viewModel.setup(rounds = 1, work = 20000, rest = 10000)
        viewModel.startWorkout()

        // Assert state transitions
        viewModel.state.test {
            assertEquals(20000, (awaitItem() as TabataState.Work).remainingTime)

            // Act
            testTimerProvider.finishTimer()

            // Assert
            assertEquals(TabataState.Finished, awaitItem())
            verify(soundAndVibrationManager).playFinishSound()
            cancelAndConsumeRemainingEvents()
        }

        // Execute all pending coroutines (including the one from viewModelScope.launch)
        advanceUntilIdle()

        // Verify workout was logged
        val captor = argumentCaptor<WorkoutLog>()
        verify(workoutLogDao).insert(captor.capture())
        assertEquals("TABATA", captor.firstValue.workoutType)
        assertEquals(20000, captor.firstValue.durationInMillis)
    }
}
