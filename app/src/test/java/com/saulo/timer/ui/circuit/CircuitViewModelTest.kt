package com.saulo.timer.ui.circuit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.saulo.timer.data.CircuitDao
import com.saulo.timer.model.Exercise
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class CircuitViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var circuitDao: CircuitDao

    private lateinit var viewModel: CircuitViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = CircuitViewModel(circuitDao)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addExercise should add a new exercise to the list`() = runTest {
        // Arrange
        val initialSize = viewModel.exercises.size

        // Act
        viewModel.addExercise()

        // Assert
        val newSize = viewModel.exercises.size
        assertEquals(initialSize + 1, newSize)
    }

    @Test
    fun `removeExercise should remove the exercise from the list`() = runTest {
        // Arrange
        val exercise = Exercise("Test Exercise", 30, null, 15, 5)
        viewModel.exercises.add(exercise)
        val initialSize = viewModel.exercises.size

        // Act
        viewModel.removeExercise(exercise)

        // Assert
        val newSize = viewModel.exercises.size
        assertEquals(initialSize - 1, newSize)
    }
}
