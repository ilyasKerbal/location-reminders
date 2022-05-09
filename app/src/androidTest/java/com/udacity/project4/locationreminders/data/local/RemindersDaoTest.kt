package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get: Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getReminders_verifyLoadedAllReminders() = runBlockingTest {

        val reminder1 = TestingData.items[0]
        val reminder2 = TestingData.items[1]
        val reminder3 = TestingData.items[2]

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)
        database.reminderDao().saveReminder(reminder3)


        val loadedReminders = database.reminderDao().getReminders()

        assertThat(loadedReminders.size, `is`(3))
        assertThat(loadedReminders[0].id, `is`(reminder1.id))
        assertThat(loadedReminders[1].id, `is`(reminder2.id))
        assertThat(loadedReminders[2].id, `is`(reminder3.id))
    }

    @Test
    fun saveReminder_getReminderById_verifyCorrectData() = runBlockingTest {

        val reminder = TestingData.items[0]
        database.reminderDao().saveReminder(reminder)

        val loadedReminder = database.reminderDao().getReminderById(reminder.id)

        assertThat(loadedReminder as ReminderDTO, notNullValue())
        assertThat(loadedReminder.id, `is`(reminder.id))
        assertThat(loadedReminder.title, `is`(reminder.title))
        assertThat(loadedReminder.description, `is`(reminder.description))
        assertThat(loadedReminder.location, `is`(reminder.location))
        assertThat(loadedReminder.latitude, `is`(reminder.latitude))
        assertThat(loadedReminder.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteAllReminders_verifyEmpty() = runBlockingTest {

        val reminder1 = TestingData.items[0]
        val reminder2 = TestingData.items[1]
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        database.reminderDao().deleteAllReminders()

        val loadedReminders = database.reminderDao().getReminders()
        assertThat(loadedReminders.size, `is`(0))
    }

}