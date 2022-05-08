package com.udacity.project4.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.GeofenceStatusCodes
import com.udacity.project4.R
import java.util.concurrent.TimeUnit
import kotlin.time.milliseconds

fun geofenceError(context: Context, errorCode: Int): String {
    val resources = context.resources
    return when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
            R.string.geofence_not_available
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
            R.string.geofence_too_many_geofences
        )
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
            R.string.geofence_too_many_pending_intents
        )
        else -> resources.getString(R.string.geofence_unknown_error)
    }
}


fun isGranted(context: Context, permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

object GeofencingConstants {
    val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
    const val GEOFENCE_RADIUS_IN_METERS = 100f
    const val EXTRA_GEOFENCE_INDEX = "GEOFENCE_INDEX"
}