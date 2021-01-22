package com.s1755183.litter.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.FragmentContainer
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.maps.android.ui.IconGenerator
import com.s1755183.litter.Message
import com.s1755183.litter.R
import com.s1755183.litter.currentUser
import com.s1755183.litter.fragments.adapters.ViewPagerAdapter

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback, View.OnClickListener {

    private val DEFAULT_ZOOM = 15.0f
    private lateinit var mMap: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(-55.9431, -3.2010)
    private var locationPermissionGranted: Boolean = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var locationCallback: LocationCallback
    private val TAG: String = "MapFragment"
    private var markers : HashMap<String, Marker> = HashMap<String, Marker>()
    private var messages : HashMap<String, Message> = HashMap<String, Message>()
    private var markermessages : HashBiMap<String, String> = HashBiMap.create()
    private lateinit var frameLayoutMain: FrameLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var viewPager: ViewPager
    private lateinit var newMessageButton: FloatingActionButton


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        frameLayoutMain = requireActivity().findViewById(R.id.frameLayoutMain)
        viewPager = requireActivity().findViewById(R.id.viewPager)
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout)
        newMessageButton = view.findViewById(R.id.floatingActionButtonNewMessage)
        newMessageButton.setOnClickListener(this)

        val mapFragmentLoad = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragmentLoad.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireContext())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (location != null) {
                        currentLocation = location
                        zoomTo(currentLocation, 15.0f)
                        mMap.setMinZoomPreference(15.0f)
                        createMarker(currentLocation, "WOOT")
                    }
                }

            }
        }
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
        val client: SettingsClient = LocationServices.getSettingsClient(this.requireContext())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    exception.startResolutionForResult(
                        this.requireActivity(),
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        Log.i(TAG, "Starting location updates")
        createLocationRequest()
        Log.i(TAG, "location request created")
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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
        mMap.setOnMarkerClickListener { marker ->
            Log.i(TAG, "MARKER CLICKED")
            //val message = markermessages[marker.id]
            // marker = markermessages.inverse()[message.title]
            viewPager.visibility = View.GONE
            appBarLayout.visibility = View.GONE
            newMessageButton.visibility = View.GONE
            frameLayoutMain.visibility = View.VISIBLE

            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayoutMain,ViewMessageFragment())
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                addToBackStack(null)
                commit()
            }
            true
        }

        Toast.makeText(this.requireContext(), "Welcome ${currentUser.name}!", Toast.LENGTH_LONG).show()
    }


    private fun createMarker(loca: Location, title: String = "New Marker") {
        Log.i(TAG, "creating marker")
        val position = locationToLngLat(loca)
        val mIconGenerator : IconGenerator = IconGenerator(this.requireContext())
        mIconGenerator.setStyle(IconGenerator.STYLE_GREEN)
        val iconBitmap : Bitmap = mIconGenerator.makeIcon(title)
        val marker = mMap.addMarker(
                MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap))
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
        markers[marker.id] = marker
        //messages[message.title] = message
        //markermessages[marker.id] = message.title
    }

    private fun createMarker(position: LatLng, title: String = "New Marker") {
        Log.i(TAG, "creating marker")
        val marker = mMap.addMarker(MarkerOptions().position(position).title(title))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position))
        markers[marker.id] = marker
        //messages[message.title] = message
        //markermessages[marker.id] = message.title
    }

    private fun locationToLngLat(location: Location): LatLng {
        return LatLng(location.latitude, location.longitude)
    }


    private fun getLocationPermission() {
        Log.i(TAG, "Checking Permissions")
        if (ContextCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            Log.i(TAG, "We have permission")
        } else {
            ActivityCompat.requestPermissions(
                this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            Log.i(TAG, "Requesting permission")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    fun zoomTo(location: Location, zoom: Float) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), zoom))
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.floatingActionButtonNewMessage -> {
                /// GET RID OF TAB LAYOUT ALSO
                viewPager.visibility = View.GONE
                appBarLayout.visibility = View.GONE
                newMessageButton.visibility = View.GONE
                frameLayoutMain.visibility = View.VISIBLE
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.frameLayoutMain, NewMessageFragment())
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    commit()
                }
            }
        }
    }


}