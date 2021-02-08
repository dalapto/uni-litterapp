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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.ui.IconGenerator
import com.s1755183.litter.*
import com.s1755183.litter.R
import com.s1755183.litter.fragments.adapters.ViewPagerAdapter
import java.util.*
import kotlin.collections.HashMap

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback, View.OnClickListener {

    private val DEFAULT_ZOOM = 15.0f
    private lateinit var mMap: GoogleMap
    private lateinit var currentLocation: Location
    private var lastLocation: Location? = null
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val defaultLocation = LatLng(-55.9431, -3.2010)
    private var locationPermissionGranted: Boolean = false
    private val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
    private lateinit var locationCallback: LocationCallback
    private val TAG: String = "MapFragment"
    private var markers : HashMap<String, Marker> = HashMap<String, Marker>()
    private var messages : HashMap<String, Message> = HashMap<String, Message>()
    private var our_messages : HashMap<String, Message> = HashMap<String, Message>()
    private var markermessages : HashBiMap<String, String> = HashBiMap.create()
    private lateinit var frameLayoutMain: FrameLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var viewPager: ViewPager
    private lateinit var newMessageButton: FloatingActionButton
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


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
                        currentLocation = location
                        lastLocation = location
                        //mMap.setMinZoomPreference(15.0f)
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
                                    val anonymous = doc.data?.get("anonymous") as Boolean
                                    if (author == currentUser.id) {
                                        our_messages[title] = Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous)
                                    } else {
                                        messages[title] = Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous)
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
                                                                createMarker(msg.value.location, msg.key, author=false, kept=true)
                                                            } else {
                                                                if (doc.data["seen"] as Boolean) {
                                                                    createMarker(msg.value.location, msg.key)
                                                                } else {
                                                                    createMarker(msg.value.location, msg.key, author = false, partial=true)
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        createMarker(msg.value.location, msg.key, author=false, kept=false, partial=false)
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
                                        createMarker(msg.value.location, msg.key, true)
                                    }
                                }
                            }
                        }
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
            //val message = markermessages[marker.id]
            // marker = markermessages.inverse()[message.title]
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
            true
        }


    }




    private fun createMarker(position: LatLng, title: String = "New Marker", author: Boolean = false, kept: Boolean = false, partial: Boolean = false) {
        var title2 = title
        if (markermessages.containsValue(title)) {
            markers[markermessages.inverse()[title]]!!.position = position
        }
        else {
            val mIconGenerator = IconGenerator(this.requireContext())
            if (author) {
                mIconGenerator.setStyle(IconGenerator.STYLE_BLUE)
            } else {
                if (kept) {
                    mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
                }
                else {
                    mIconGenerator.setStyle(IconGenerator.STYLE_GREEN)
                    title2 = if (partial) {
                        ("?".repeat(title.length/2)+title.drop(title.length/2))
                    } else {
                        ("?".repeat(title.length))
                    }
                }
            }
            val iconBitmap: Bitmap = mIconGenerator.makeIcon(title2)
            val marker = mMap.addMarker(
                    MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap))
            )
            markers[marker.id] = marker
            Log.i(TAG,marker.id)
            Log.i(TAG,title)
            markermessages[marker.id] = title
        }
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