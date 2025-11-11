package com.saulo.timer.ui.fortime

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
class ForTimeViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ForTimeViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = ForTimeViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setTimeCap should update timeCap state`() = runTest {
        // Assert
        viewModel.timeCap.test {
            assertEquals(null, awaitItem()) // Initial state

            // Act
            viewModel.setTimeCap(15)

            // Assert
            assertEquals(15, awaitItem())

            // Act
            viewModel.setTimeCap(null) // No time cap

            // Assert
            assertEquals(null, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
