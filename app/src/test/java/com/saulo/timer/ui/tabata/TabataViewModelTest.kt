package com.saulo.timer.ui.tabata

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
class TabataViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: TabataViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TabataViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setRounds should update rounds state`() = runTest {
        viewModel.rounds.test {
            assertEquals(8, awaitItem()) // Initial state
            viewModel.setRounds(12)
            assertEquals(12, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setWorkTime should update workTime state`() = runTest {
        viewModel.workTime.test {
            assertEquals(20, awaitItem()) // Initial state
            viewModel.setWorkTime(30)
            assertEquals(30, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `setRestTime should update restTime state`() = runTest {
        viewModel.restTime.test {
            assertEquals(10, awaitItem()) // Initial state
            viewModel.setRestTime(15)
            assertEquals(15, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
    }
}
