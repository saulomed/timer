package com.saulo.timer.ui.amrap

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
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

@ExperimentalCoroutinesApi
class AmrapViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: AmrapViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AmrapViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setTime should update time state`() = runTest {
        // Assert
        viewModel.time.test {
            assertEquals(0, awaitItem()) // Initial state

            // Act
            viewModel.setTime(10)

            // Assert
            assertEquals(10, awaitItem())

            // Act
            viewModel.setTime(20)

            // Assert
            assertEquals(20, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
