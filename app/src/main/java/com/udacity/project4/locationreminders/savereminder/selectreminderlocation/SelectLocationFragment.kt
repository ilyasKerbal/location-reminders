package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.isGranted
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

private const val DEFAULT_ZOOM = 15
private const val REQUEST_LOCATION_PERMISSION = 1

/**
 * Reference: https://developers.google.com/maps/documentation/android-sdk/current-place-tutorial#get-the-location-of-the-android-device-and-position-the-map
 * */

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback{

    private var map: GoogleMap? = null

    private var currentMarker: Marker? = null
    private var currentPOI: PointOfInterest? = null

    // Request the last known location of the user's device.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        binding.btnSaveLocation.setOnClickListener {
            onLocationSelected()
        }
        if (currentPOI == null){
            binding.btnSaveLocation.visibility = View.GONE
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.onSaveLocation(currentPOI)
    }

    override fun onDestroy() {
        super.onDestroy()
        currentMarker = null
        currentPOI = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    private fun checkFineLocationPermission(): Boolean {
        return isGranted(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap

        _viewModel.selectedPOI.value?.let { poi ->
            updateCurrentPoi(poi)
        }

        map?.setOnMapLongClickListener { latLng ->
            binding.btnSaveLocation.visibility = View.VISIBLE
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address: String = addresses[0].getAddressLine(0)
            updateCurrentPoi(PointOfInterest(latLng, null, address))
        }

        map?.setOnPoiClickListener { poi ->
            binding.btnSaveLocation.visibility = View.VISIBLE
            updateCurrentPoi(poi)
        }


        try {
            val success = map?.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        myLocationEnabledCheck()

        getDeviceLocationAndMoveCamera()
    }

    private fun myLocationEnabledCheck() {
        if (checkFineLocationPermission()) {
            map?.isMyLocationEnabled = true
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                myLocationEnabledCheck()
                getDeviceLocationAndMoveCamera()
            }
        }
    }

    private fun getDeviceLocationAndMoveCamera() {
        try {
            if (checkFineLocationPermission()) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        val location = task.result
                        if (location != null) {
                            map?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(location.latitude, location.longitude),
                                    DEFAULT_ZOOM.toFloat()
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("SelectLocationFragment","Something went wrong: ${e.message}")
        }
    }

    private fun updateCurrentPoi(poi: PointOfInterest) {
        if (currentMarker != null) {
            currentMarker?.remove()
        }
        currentPOI = poi
        currentMarker = map?.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
        )
        currentMarker?.showInfoWindow()
    }



}
