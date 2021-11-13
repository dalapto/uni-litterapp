package com.s1755183.litter.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.google.common.collect.HashBiMap
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.ui.IconGenerator
import com.s1755183.litter.*
import com.s1755183.litter.R
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
    private var following : HashSet<String> = HashSet<String>()
    private var our_messages : HashMap<String, Message> = HashMap<String, Message>()
    private var markermessages : HashBiMap<String, String> = HashBiMap.create()
    private var messages_states : HashMap<String, MessageState> = HashMap<String, MessageState>()
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
                db.collection("users").document(currentUser.id).get().addOnSuccessListener { document ->
                    if (document != null) {
                        currentUser.messages_seen = (document.data?.get("messages_seen") as Long).toInt()
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
                    val comments = (doc.data?.get("comments") as Long).toInt()
                    val anonymous = doc.data?.get("anonymous") as Boolean
                    if (author == currentUser.id) {
                        our_messages[title] = Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, views = views, anonymous = anonymous, comments = comments)
                    } else {
                        messages[title] = Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, views = views, anonymous = anonymous, comments = comments)
                        }
                    }
                }
            }
            db.collection("users").document(currentUser.id).collection("following").get().addOnSuccessListener { follow_result ->
                val following_list = follow_result.documents.toMutableList()
                following.clear()
                for (followed_author in following_list) {
                    following.add(followed_author.data?.get("author_id") as String)
            }
            for (msg in messages) {
                db.collection("messages").document(msg.key).get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        messages.remove(msg.key)
                        markers[markermessages.inverse()[msg.key]]!!.remove()
                        markers.remove(markermessages.inverse()[msg.key])
                        markermessages.inverse().remove(msg.key)
                    } else {
                        var mstate = MessageState.UNSEEN
                        db.collection("users").document(currentUser.id).collection("seenmessages").whereEqualTo("title", msg.key).get()
                                .addOnSuccessListener { documents ->
                                    if (!documents.isEmpty) {
                                        for (doc in documents) {
                                            mstate = if (doc.data["seen"] as Boolean) {
                                                (if (following.contains(msg.value.author_id)) MessageState.FOLLOWED_SEEN else MessageState.SEEN)
                                            } else {
                                                if (following.contains(msg.value.author_id)) MessageState.FOLLOWED_PARTIAL else MessageState.PARTIAL_SEEN
                                            }
                                        }
                                    }
                                    else {
                                        mstate = if (following.contains(msg.value.author_id)) MessageState.FOLLOWED_UNSEEN else MessageState.UNSEEN
                                    }
                                    createMarker(msg.value.location, msg.key, state=mstate)
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
                        createMarker(msg.value.location, msg.key, state=MessageState.OWN)
                    }
                }
            }
        }
    }


    private fun updateCircles() {
        closeCirc?.remove()
        farCirc?.remove()
        if (displaySeeCircle) closeCirc = mMap.addCircle(CircleOptions().center(locationToLngLat(currentLocation)).radius(175.0).fillColor(Color.parseColor("#3271cce7")).strokeColor(Color.parseColor("#1071cce7")))
        if (displayRevealCircle) farCirc = mMap.addCircle(CircleOptions().center(locationToLngLat(currentLocation)).radius(500.0).fillColor(Color.parseColor("#287198e7")).strokeColor(Color.parseColor("#087198e7")))
    }
    private fun checkProximity() {
        for (msg in messages_states) {
            if (msg.value == MessageState.PARTIAL_SEEN || msg.value == MessageState.UNSEEN || msg.value == MessageState.FOLLOWED_PARTIAL || msg.value == MessageState.FOLLOWED_UNSEEN) {
                if (checkDistance(messages[msg.key]!!.location, locationToLngLat(currentLocation),0.49 )) {
                    val proximity = checkDistance(messages[msg.key]!!.location, locationToLngLat(currentLocation),0.17459)
                    val messagestate = MessageState(title = msg.key, seen = proximity)
                    db.collection("users").document(currentUser.id).collection("seenmessages").document(msg.key).set(messagestate)
                    if (proximity && (msg.value == MessageState.PARTIAL_SEEN || msg.value == MessageState.UNSEEN || msg.value == MessageState.FOLLOWED_PARTIAL || msg.value == MessageState.FOLLOWED_UNSEEN)) {
                        if (msg.value == MessageState.FOLLOWED_UNSEEN || msg.value == MessageState.FOLLOWED_PARTIAL) {
                            msg.setValue(MessageState.FOLLOWED_SEEN)
                        }
                        else {
                            msg.setValue(MessageState.SEEN)
                        }
                        db.collection("users").document(currentUser.id).update("messages_seen", currentUser.messages_seen+1)
                        db.collection("messages").document(msg.key).update("views",(1+messages[msg.key]!!.views).toLong())
                        db.collection("users").document(messages[msg.key]!!.author_id!!).get().addOnSuccessListener { document ->
                            if (document != null && currentUser.id != messages[msg.key]!!.author_id!!) {
                                val views_got = (document.data?.get("views_got") as Long).toInt()
                                db.collection("users").document(messages[msg.key]!!.author_id!!).update("views_got",views_got+1)
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
            if (messages_states[markermessages[marker.id]]!! == MessageState.FOLLOWED_UNSEEN || messages_states[markermessages[marker.id]]!! == MessageState.FOLLOWED_PARTIAL || messages_states[markermessages[marker.id]]!! == MessageState.UNSEEN || messages_states[markermessages[marker.id]]!! == MessageState.PARTIAL_SEEN) {
                marker.title ="You are too far away to read this, move closer!"
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


    enum class MessageState {
        OWN, FOLLOWED_SEEN, FOLLOWED_PARTIAL, FOLLOWED_UNSEEN, SEEN, PARTIAL_SEEN, UNSEEN
    }


    private fun createMarker(position: LatLng, title: String = "New Marker", old_state: Int = 0, state: MessageState) {
        if (markermessages.containsValue(title)) {
            markers[markermessages.inverse()[title]]?.remove()
            markermessages.inverse().remove(title)
        }
        val mIconGenerator = IconGenerator(this.requireContext())
        when (state) {
            MessageState.OWN -> mIconGenerator.setStyle(IconGenerator.STYLE_BLUE)
            MessageState.FOLLOWED_SEEN -> mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
            MessageState.FOLLOWED_PARTIAL -> mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
            MessageState.FOLLOWED_UNSEEN -> mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
            else -> mIconGenerator.setStyle(IconGenerator.STYLE_GREEN)

        }
        var title2: String = when (state) {
            MessageState.FOLLOWED_PARTIAL -> "?".repeat(title.length/2)+title.drop(title.length/2)
            MessageState.PARTIAL_SEEN -> "?".repeat(title.length/2)+title.drop(title.length/2)
            MessageState.FOLLOWED_UNSEEN -> "?".repeat(title.length)
            MessageState.UNSEEN -> "?".repeat(title.length)
            else -> title
        }
        val iconBitmap: Bitmap = mIconGenerator.makeIcon(title2)
        when (state) {
            MessageState.OWN -> {
                if (displayOwn) {
                    val marker = mMap.addMarker(MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)))
                    markers[marker.id] = marker
                    markermessages[marker.id] = title
                    messages_states[title] = state
                }
            }
            MessageState.FOLLOWED_SEEN -> {
                if (displayFollowing) {
                    val marker = mMap.addMarker(MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)))
                    markers[marker.id] = marker
                    markermessages[marker.id] = title
                    messages_states[title] = state
                }
            }
            MessageState.FOLLOWED_PARTIAL -> {
                if (displayFollowing) {
                    val marker = mMap.addMarker(MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)))
                    markers[marker.id] = marker
                    markermessages[marker.id] = title
                    messages_states[title] = state
                }
            }
            MessageState.FOLLOWED_UNSEEN -> {
                if (displayFollowing) {
                    val marker = mMap.addMarker(MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)))
                    markers[marker.id] = marker
                    markermessages[marker.id] = title
                    messages_states[title] = state
                }
            }
            MessageState.SEEN -> {
                if (displaySeen) {
                    val marker = mMap.addMarker(MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)))
                    markers[marker.id] = marker
                    markermessages[marker.id] = title
                    messages_states[title] = state
                }
            }
            else -> {
                if (displayUnseen) {
                    val marker = mMap.addMarker(MarkerOptions().position(position).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)))
                    markers[marker.id] = marker
                    markermessages[marker.id] = title
                    messages_states[title] = state
                }
            }
        }
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