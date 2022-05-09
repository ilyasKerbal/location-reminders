package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var localRepository: RemindersLocalRepository
    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        localRepository =
            RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    @Test
    fun getReminder_nonExistId_returnError() = runBlocking {

        val result = localRepository.getReminder("1")
        result as Result.Error
        assertThat(result.message, Matchers.`is`("Reminder not found!"))
    }

    @Test
    fun saveReminder_getReminder_verifyCorrectData() = runBlocking {

        val reminder = TestingData.items[0]
        localRepository.saveReminder(reminder)

        val result = localRepository.getReminder(reminder.id)

        assertThat(result, not(nullValue()))
        result as Result.Success
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.latitude, `is`(reminder.latitude))
        assertThat(result.data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun saveReminder_getReminders_verifyAllReminders() = runBlocking {

        val reminder1 = TestingData.items[0]
        val reminder2 = TestingData.items[1]
        val reminder3 = TestingData.items[2]

        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)
        localRepository.saveReminder(reminder3)

        val result = localRepository.getReminders()

        result as Result.Success
        assertThat(result.data.size, `is`(3))
        assertThat(result.data[0].id, `is`(reminder1.id))
        assertThat(result.data[1].id, `is`(reminder2.id))
        assertThat(result.data[2].id, `is`(reminder3.id))
    }

    @Test
    fun saveReminders_deleteAllReminders_verifyEmpty() = runBlocking {

        val reminder1 = TestingData.items[0]
        val reminder2 = TestingData.items[1]
        localRepository.saveReminder(reminder1)
        localRepository.saveReminder(reminder2)


        val result1 = localRepository.getReminders() as Result.Success

        assertThat(result1.data.size, `is`(2))

        localRepository.deleteAllReminders()

        val result2 = localRepository.getReminders() as Result.Success
        assertThat(result2.data.size, `is`(0))
    }
}