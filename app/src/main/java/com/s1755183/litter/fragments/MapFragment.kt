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
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.ktx.model.circleOptions
import com.google.maps.android.ui.IconGenerator
import com.s1755183.litter.*
import com.s1755183.litter.R
import com.s1755183.litter.fragments.adapters.ViewPagerAdapter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback, View.OnClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var currentLocation: Location
    private var lastLocation: Location? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationPermissionGranted: Boolean = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var locationCallback: LocationCallback
    private val TAG: String = "MapFragment"
    private var markers : HashMap<String, Marker> = HashMap<String, Marker>()
    private var messages : HashMap<String, Message> = HashMap<String, Message>()
    private var our_messages : HashMap<String, Message> = HashMap<String, Message>()
    private var markermessages : HashBiMap<String, String> = HashBiMap.create()
    private var messages_states : HashMap<String, Int> = HashMap<String, Int>()
    private lateinit var frameLayoutMain: FrameLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var viewPager: ViewPager
    private lateinit var newMessageButton: FloatingActionButton
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var closeCirc : Circle? = null
    private var farCirc : Circle? = null


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
                        if (lastLocation == null) {
                            zoomTo(location, 15.0f)
                        }
                        currentUser.location = locationToLngLat(location)
                        currentLocation = location
                        lastLocation = location
                        updateCircles()
                        updateMarkers()
                        checkProximity()
                        //mMap.setMinZoomPreference(15.0f)
                    }
                }
            }
        }
    }

    private fun updateMarkers() {
        db.collection("messages").get().addOnSuccessListener { result ->
            val temp = result.documents.toMutableList()
            for (doc in temp) {
                val hlocation = doc.data?.get("location") as HashMap<String, Double>
                val location2 = LatLng(hlocation["latitude"]!!, hlocation["longitude"]!!) as LatLng
                if (checkDistance(location2, locationToLngLat(currentLocation), 10.0)) {
                    val title = doc.data?.get("title") as String
                    val author = doc.data?.get("author_id") as String
                    val image = doc.data?.get("image") as String
                    val text = doc.data?.get("text") as String
                    val time = doc.data?.get("time").toString()
                    val views = (doc.data?.get("views") as Long).toInt()
                    val keeps = (doc.data?.get("keeps") as Long).toInt()
                    val comments = (doc.data?.get("comments") as Long).toInt()
                    val anonymous = doc.data?.get("anonymous") as Boolean
                    if (author == currentUser.id) {
                        our_messages[title] = Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous, comments = comments)
                    } else {
                        messages[title] = Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous, comments = comments)
                    }
                }
            }
            for (msg in messages) {
                db.collection("messages").document(msg.key).get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        messages.remove(msg.key)
                        markers[markermessages.inverse()[msg.key]]!!.remove()
                        markers.remove(markermessages.inverse()[msg.key])
                        markermessages.inverse().remove(msg.key)
                    } else {
                        db.collection("users").document(currentUser.id).collection("seenmessages").whereEqualTo("title", msg.key).get()
                                .addOnSuccessListener { documents ->
                                    if (!documents.isEmpty) {
                                        for (doc in documents) {
                                            if (doc.data["kept"] as Boolean) {
                                                createMarker(msg.value.location, msg.key, state=3)
                                            } else {
                                                if (doc.data["seen"] as Boolean) {
                                                    createMarker(msg.value.location, msg.key, state=2)
                                                } else {
                                                    createMarker(msg.value.location, msg.key, state=1)
                                                }
                                            }
                                        }
                                    } else {
                                        createMarker(msg.value.location, msg.key, state=0)
                                    }
                                }
                    }
                }
            }
            for (msg in our_messages) {
                db.collection("messages").document(msg.key).get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        our_messages.remove(msg.key)
                        markers[markermessages.inverse()[msg.key]]!!.remove()
                        markers.remove(markermessages.inverse()[msg.key])
                        markermessages.inverse().remove(msg.key)
                    } else {
                        createMarker(msg.value.location, msg.key, state=4)
                    }
                }
            }
        }
    }


    private fun updateCircles() {
        closeCirc?.remove()
        farCirc?.remove()
        closeCirc = mMap.addCircle(CircleOptions().center(locationToLngLat(currentLocation)).radius(175.0).fillColor(Color.parseColor("#3271cce7")).strokeColor(Color.parseColor("#1071cce7")))
        farCirc = mMap.addCircle(CircleOptions().center(locationToLngLat(currentLocation)).radius(500.0).fillColor(Color.parseColor("#287198e7")).strokeColor(Color.parseColor("#087198e7")))
    }
    private fun checkProximity() {
        for (msg in messages_states) {
            if (msg.value < 2) {
                if (checkDistance(messages[msg.key]!!.location, locationToLngLat(currentLocation),0.0045)) {
                    val proximity = checkDistance(messages[msg.key]!!.location, locationToLngLat(currentLocation),0.00245)
                    val messagestate = MessageState(title = msg.key, seen = proximity)
                    db.collection("users").document(currentUser.id).collection("seenmessages").document(msg.key).set(messagestate)
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
        locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
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
        createLocationRequest()
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
            if (messages_states[markermessages[marker.id]]!! < 2) {
                marker.title ="Too far away to read this..."
                marker.showInfoWindow()
            }
            else {
                viewPager.visibility = View.GONE
                appBarLayout.visibility = View.GONE
                newMessageButton.visibility = View.GONE
                frameLayoutMain.visibility = View.VISIBLE
                if (messages[markermessages[marker.id]] != null) {
                    (activity as MainActivity?)!!.saveMessage(messages[markermessages[marker.id]]!!)
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayoutMain,ViewMessageFragment())
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        addToBackStack(null)
                        commit()
                    }
                }
                else {
                    (activity as MainActivity?)!!.saveMessage(our_messages[markermessages[marker.id]]!!)
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayoutMain,EditMessageFragment())
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        addToBackStack(null)
                        commit()
                    }
                }
            }
            true
        }


    }




    private fun createMarker(position: LatLng, title: String = "New Marker", state: Int = 0) {
        if (markermessages.containsValue(title)) {
            markers[markermessages.inverse()[title]]?.remove()
            markermessages.inverse().remove(title)
        }
        val mIconGenerator = IconGenerator(this.requireContext())
        when (state) {
            4 -> mIconGenerator.setStyle(IconGenerator.STYLE_BLUE)
            3 -> mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
            else -> mIconGenerator.setStyle(IconGenerator.STYLE_GREEN)
        }
        var title2: String = when (state) {
            0 -> "?".repeat(title.length)
            1 -> "?".repeat(title.length/2)+title.drop(title.length/2)
            else -> title
        }
        val iconBitmap: Bitmap = mIconGenerator.makeIcon(title2)
        val marker = mMap.addMarker(MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)))
        markers[marker.id] = marker
        Log.i(TAG,marker.id)
        Log.i(TAG,title)
        markermessages[marker.id] = title
        messages_states[title] = state
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