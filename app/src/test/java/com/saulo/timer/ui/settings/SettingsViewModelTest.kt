package com.saulo.timer.ui.settings

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.saulo.timer.util.PreferenceManager
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
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var viewModel: SettingsViewModel

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
    fun `setPreparationTime should update state and call preference manager`() = runTest {
        // Arrange
        val initialTime = 10
        `when`(preferenceManager.getPreparationTime()).thenReturn(initialTime)
        viewModel = SettingsViewModel(preferenceManager)

        // Assert initial state
        assertEquals(initialTime, viewModel.preparationTime.value)

        // Act
        val newTime = 20
        viewModel.setPreparationTime(newTime)

        // Assert
        viewModel.preparationTime.test {
            assertEquals(newTime, awaitItem())
            cancelAndConsumeRemainingEvents()
        }
        verify(preferenceManager).setPreparationTime(newTime)
    }
}
