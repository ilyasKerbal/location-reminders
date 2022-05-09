package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

object TestingData {

    val reminderDataItem = ReminderDataItem(
        "CIH BANK",
        "Description 1",
        "Location 1",
        31.930820925930362,
        -4.426461503943409
    )

    val items = arrayListOf(
        ReminderDTO(
            "CIH BANK",
            "Description 1",
            "Location 1",
            31.930820925930362,
            -4.426461503943409
        ),
        ReminderDTO(
            "Florence",
            "Description 2",
            "Location 2",
            31.93032674249183,
            -4.428711440747988
        ),
        ReminderDTO(
            "MARJANE MARKET",
            "description 3",
            "location 3",
            31.931137859884146,
            -4.453245241655544
        )
    )
}