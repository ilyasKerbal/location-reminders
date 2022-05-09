package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.TestingData
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get: Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: RemindersListViewModel

    @Before
    fun setUp() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        viewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }


    @Test
    fun loadReminders_showLoading() = runBlockingTest {
        // Pause dispatcher to verify value
        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_success() = runBlockingTest {
        TestingData.items.forEach { reminderDTO ->
            fakeDataSource.saveReminder(reminderDTO)
        }

        viewModel.loadReminders()

        val loadedItems = viewModel.remindersList.getOrAwaitValue()
        assertThat(loadedItems.size, `is`(TestingData.items.size))
        for (i in loadedItems.indices) {
            assertThat(loadedItems[i].title, `is`(TestingData.items[i].title))
        }

        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_success_noReminders() = runBlockingTest {
        fakeDataSource.deleteAllReminders()

        viewModel.loadReminders()

        val loadedItems = viewModel.remindersList.getOrAwaitValue()
        assertThat(loadedItems.size, `is`(0))

        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_error_dataSource() = runBlockingTest {
        fakeDataSource.setReturnError(true)

        viewModel.loadReminders()

        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("Test exception"))

        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

}