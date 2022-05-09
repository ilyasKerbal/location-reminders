package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.TestingData
import com.udacity.project4.locationreminders.getOrAwaitValue

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var app: Application

    @Before
    fun setUp() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        app = ApplicationProvider.getApplicationContext()
        viewModel = SaveReminderViewModel(app, fakeDataSource)
    }

    @Test
    fun saveReminder_showLoading() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()

        viewModel.saveReminder(TestingData.reminderDataItem)

        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()

        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateEnteredData_returnTrue() {
        val res = viewModel.validateEnteredData(TestingData.reminderDataItem)

        MatcherAssert.assertThat(res, `is`(true))
    }

    @Test
    fun saveReminder_success() {

        viewModel.saveReminder(TestingData.reminderDataItem)

        MatcherAssert.assertThat(
            viewModel.showSnackBar.getOrAwaitValue(), `is`(app.getString(R.string.reminder_saved))
        )

        Assert.assertEquals(viewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    @Test
    fun validateEnteredData_titleEmpty_returnFalse(){

        val reminderData = TestingData.reminderDataItem.copy()
        reminderData.title = ""

        val res = viewModel.validateEnteredData(reminderData)

        MatcherAssert.assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
        MatcherAssert.assertThat(res, `is`(false))
    }

    @Test
    fun validateEnteredData_titleNull_returnFalse(){
        val reminderData = TestingData.reminderDataItem.copy()
        reminderData.title = null

        val res = viewModel.validateEnteredData(reminderData)

        MatcherAssert.assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))

        MatcherAssert.assertThat(res, `is`(false))
    }

    @Test
    fun validateEnteredData_locationNull_returnFalse(){
        val reminderData = TestingData.reminderDataItem.copy()
        reminderData.location = null

        val res = viewModel.validateEnteredData(reminderData)

        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            Matchers.`is`(R.string.err_select_location)
        )

        MatcherAssert.assertThat(res, `is`(false))
    }

    @Test
    fun validateEnteredData_locationEmpty_returnFalse(){

        val reminderData = TestingData.reminderDataItem.copy()
        reminderData.location = ""

        val res = viewModel.validateEnteredData(reminderData)

        MatcherAssert.assertThat(
            viewModel.showSnackBarInt.getOrAwaitValue(),
            Matchers.`is`(R.string.err_select_location)
        )

        MatcherAssert.assertThat(res, `is`(false))
    }
}