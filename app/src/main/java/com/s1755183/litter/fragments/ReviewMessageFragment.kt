package com.s1755183.litter.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.s1755183.litter.*
import com.s1755183.litter.R
import java.util.*


class ReviewMessageFragment : Fragment(R.layout.fragment_review_message), OnMapReadyCallback, View.OnClickListener {

    private val DEFAULT_ZOOM = 15.0f
    private lateinit var mMap: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationPermissionGranted: Boolean = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var locationCallback: LocationCallback
    private val TAG: String = "ReviewMapFragment"
    private var messageMarker: Marker? = null
    private lateinit var frameLayoutMain: FrameLayout
    private lateinit var backButton: Button
    private lateinit var postButton: Button
    private lateinit var titletext: String
    private lateinit var messagetext: String
    private var image: Uri? = null
    private var anonymouspost: Boolean = false
    private lateinit var viewPager: ViewPager
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var newMessageButton: FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage. reference
        backButton = view.findViewById(R.id.buttonBack)
        backButton.setOnClickListener(this)
        postButton = view.findViewById(R.id.buttonPost)
        postButton.setOnClickListener(this)
        frameLayoutMain = requireActivity().findViewById(R.id.frameLayoutMain)
        viewPager = requireActivity().findViewById(R.id.viewPager)
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout)
        newMessageButton = requireActivity().findViewById(R.id.floatingActionButtonNewMessage)

        val message = (activity as MainActivity?)!!.getMessageDetails()
        if (message!!.image != "") {
            image = (activity as MainActivity?)!!.getImageURI()
        }
        titletext = message.title.toString()
        messagetext = message.text.toString()
        anonymouspost = message.anonymous

        val mapFragmentLoad = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragmentLoad.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireContext())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    if (location != null) {
                        currentLocation = location
                        zoomTo(currentLocation, 18.0f)
                        if (messageMarker == null) {
                            messageMarker = createMarker(currentLocation, titletext)
                            messageMarker!!.showInfoWindow()
                        }
                        /*else {
                            messageMarker!!.position = LatLng(currentLocation.latitude,currentLocation.longitude)
                            messageMarker!!.showInfoWindow()
                        }*/
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
        mMap.setMinZoomPreference(15.0f)
        getLocationPermission()
    }

    private fun createMarker(marker: LatLng, markertext: String = "New Marker") {
        val temp = mMap.addMarker(
            MarkerOptions().position(marker).title(markertext).draggable(true)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
        messageMarker = temp
    }

    private fun createMarker(loca: Location, markertext: String = "New Marker"): Marker {
        Log.i(TAG, "creating marker")
        val marker = locationToLngLat(loca)
        val temp = mMap.addMarker(
            MarkerOptions().position(marker).title(markertext).draggable(true)
        )
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker))
        return temp
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

    private fun getDeviceLocation() {
        Log.i(TAG, "Getting location")
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this.requireActivity()) { task ->
                    if (task.isSuccessful && task.result != null) {
                        Log.i(TAG, "Setting current location")
                        currentLocation = task.result
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    currentLocation.latitude,
                                    currentLocation.longitude
                                ), DEFAULT_ZOOM.toFloat()
                            )
                        )
                        //zoomTo(currentLocation,15.0f)
                        //createMarker(currentLocation,"TITLE")
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }


    fun zoomTo(location: Location, zoom: Float) {
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    location.latitude,
                    location.longitude
                ), zoom
            )
        )
    }

    fun zoomTo(location: LatLng, zoom: Float) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }


    override fun onClick(v: View?) {

        Log.i(TAG, "CLICKED")
        when (v?.id) {
            R.id.buttonBack -> {
                Log.i(TAG, "BACK CLICKED")
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.frameLayoutMain, NewMessageFragment())
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    addToBackStack(null)
                    commit()
                }
            }
            R.id.buttonPost -> {
                (activity as MainActivity?)!!.resetMessage()
                viewPager.visibility = View.VISIBLE
                appBarLayout.visibility = View.VISIBLE
                newMessageButton.visibility = View.VISIBLE
                frameLayoutMain.visibility = View.GONE


                val randomKey : String = UUID.randomUUID().toString()
                val storageRef: StorageReference = storageReference.child("images/$randomKey")

                storageRef.putFile(image!!).addOnSuccessListener {
                        taskSnapshot ->
                    val downloadUrl = taskSnapshot.task.snapshot.storage.downloadUrl.toString()
                    val message = Message(image = randomKey, text = messagetext, title = titletext.toLowerCase(), location = LatLng(currentLocation.latitude, currentLocation.longitude), author_id = auth.uid, anonymous = anonymouspost)
                    db.collection("messages").document(titletext).set(message).addOnSuccessListener {
                        Log.i(TAG, "DocumentSnapshot added with UID: ${auth.uid}")
                    }
                }.addOnFailureListener {exception ->

                    }


                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.frameLayoutMain, Fragment())
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    addToBackStack(null)
                    commit()
                }
                //save details

            }
        }
    }


}