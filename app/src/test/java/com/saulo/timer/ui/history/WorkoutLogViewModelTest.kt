package com.saulo.timer.ui.history

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.saulo.timer.data.WorkoutLogDao
import com.saulo.timer.model.WorkoutLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
import java.util.Date

@ExperimentalCoroutinesApi
class WorkoutLogViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var workoutLogDao: WorkoutLogDao

    private lateinit var viewModel: WorkoutLogViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `allLogs should return logs from dao`() = runTest {
        // Arrange
        val fakeLogs = listOf(
            WorkoutLog(1, "AMRAP", 600000, Date()),
            WorkoutLog(2, "TABATA", 240000, Date())
        )
        `when`(workoutLogDao.getAllLogs()).thenReturn(flowOf(fakeLogs))

        // Act
        viewModel = WorkoutLogViewModel(workoutLogDao)

        // Assert
        viewModel.allLogs.test {
            assertEquals(emptyList<WorkoutLog>(), awaitItem()) // Initial empty list
            assertEquals(fakeLogs, awaitItem()) // Mocked data
            cancelAndConsumeRemainingEvents()
        }
    }
}
