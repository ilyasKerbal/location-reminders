package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.GeofencingConstants.GEOFENCE_RADIUS_IN_METERS
import com.udacity.project4.utils.GeofencingConstants.GEOFENCE_RADIUS_MAX
import com.udacity.project4.utils.GeofencingConstants.GEOFENCE_RADIUS_MIN
import com.udacity.project4.utils.SingleLiveEvent
import kotlinx.coroutines.launch

class SaveReminderViewModel(val app: Application, val dataSource: ReminderDataSource) :
    BaseViewModel(app) {
    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val selectedPOI = MutableLiveData<PointOfInterest>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()
    val radius = MutableLiveData<Int>(GEOFENCE_RADIUS_IN_METERS.toInt())
    val radius_min = GEOFENCE_RADIUS_MIN
    val radius_max = GEOFENCE_RADIUS_MAX


    val addGeoFencingRequest = SingleLiveEvent<ReminderDataItem>()


    fun onSaveLocation(poi: PointOfInterest?) {
        selectedPOI.value = poi
        latitude.value = poi?.latLng?.latitude
        longitude.value = poi?.latLng?.longitude
        navigationCommand.value = NavigationCommand.Back
        poi?.let {
            reminderSelectedLocationStr.value = poi.name
        }
    }

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null

        addGeoFencingRequest.value = null
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) {
        if (validateEnteredData(reminderData)) {
            // saveReminder(reminderData)
            addGeoFencingRequest.value = reminderData
        }
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude,
                    reminderData.longitude,
                    reminderData.id
                )
            )
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }


    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    fun onAddGeofencingSucceeded(reminderData: ReminderDataItem) {
        showToast.value = app.getString(R.string.geofences_added)
        saveReminder(reminderData)
    }

    fun onAddGeofencingFailed() {
        showSnackBar.value = app.getString(R.string.geofences_not_added)
    }
}