package com.s1755183.litter

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val DEFAULT_ZOOM = 15
    private lateinit var mMap: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted : Boolean = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private val TAG: String = "MapsActivity"
    private lateinit var locationCallback: LocationCallback




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //Log.i(TAG, auth.currentUser.toString())
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    if (location != null) {
                        currentLocation = location
                        createMarker(location,"WOOT")
                    }
                }
            }
        }

        Log.i(TAG, "DONE")
    }

    override fun onResume() {
        super.onResume()
        getLocationPermission()
        startLocationUpdates()
    }

    private fun createLocationRequest() {
        Log.i(TAG, "creating location request")
         locationRequest = LocationRequest.create().apply {
             interval = 10000
             fastestInterval = 5000
             priority = LocationRequest.PRIORITY_HIGH_ACCURACY
         }
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                try {
                    exception.startResolutionForResult(this@MapsActivity,
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }

    }




    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(){
        Log.i(TAG, "Starting location updates")
        createLocationRequest()
        Log.i(TAG, "location request created")
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLocationPermission()
        mMap.isMyLocationEnabled = true
    }


    private fun createMarker(marker: LatLng, markertext: String = "New Marker") {
        mMap.addMarker(MarkerOptions().position(marker).title(markertext))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
    }

    private fun createMarker(loca: Location, markertext: String = "New Marker") {
        Log.i(TAG, "creating marker")
        val marker = locationToLngLat(loca)
        mMap.addMarker(MarkerOptions().position(marker).title(markertext))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
    }

    private fun locationToLngLat(location: Location):LatLng {
        return LatLng(location.latitude, location.longitude)
    }



    private fun getLocationPermission() {
        Log.i(TAG, "Checking Permissions")
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            Log.i(TAG, "We have permission")
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
            Log.i(TAG, "Requesting permission")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                    Log.i(TAG, "We have permissions")
                }
            }
        }
        //updateLocationUI()
    }

    private fun getDeviceLocation() {
        Log.i(TAG, "Getting location")
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful && task.result != null) {
                        Log.i(TAG, "Setting current location")
                        currentLocation = task.result
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation.latitude, currentLocation.longitude), DEFAULT_ZOOM.toFloat()))
                        createMarker(currentLocation)
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }



}