package com.saulo.timer.ui.emom

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
class EmomViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: EmomViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = EmomViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setMinutes should update minutes state`() = runTest {
        // Assert
        viewModel.minutes.test {
            assertEquals(1, awaitItem()) // Initial state

            // Act
            viewModel.setMinutes(10)

            // Assert
            assertEquals(10, awaitItem())

            // Act
            viewModel.setMinutes(5)

            // Assert
            assertEquals(5, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
