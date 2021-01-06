package com.s1755183.litter

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.fragments.MapFragment
import com.s1755183.litter.fragments.MessagesFragment
import com.s1755183.litter.fragments.SettingsFragment
import com.s1755183.litter.fragments.adapters.ViewPagerAdapter


class MainActivity : FragmentActivity() {


    private val TAG: String = "MainActivity"
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    //lateinit var currentUser: User
    lateinit var tabLayout: TabLayout
    lateinit var mapsTab : TabItem
    lateinit var messagesTab : TabItem
    lateinit var logoutTab : TabItem
    lateinit var viewPager: ViewPager


    private fun setupTabs(){
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(MapFragment(),"Map")
        adapter.addFragment(MessagesFragment(),"Messages")
        adapter.addFragment(SettingsFragment(),"Settings")
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_baseline_map_24)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_baseline_mms_24)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_baseline_settings_applications_24)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        setupTabs()

        auth = FirebaseAuth.getInstance()
        Log.i(TAG, auth.currentUser!!.uid)
    }




    private fun getUser(collectionid: String, docid: String): User {
        val docRef = db.collection(collectionid).document(docid)
        var uid = ""
        var uname = ""
        var urange = 0.0
        var umess = {}
        var ukept = {}
        docRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot != null) {

            }
            else {

            }
            //uid = documentSnapshot.get("id") as String
            //uname = documentSnapshot.get("name") as String
            //urange = documentSnapshot.get("pickup_range") as Double
            //umess = documentSnapshot.get("my_messages") as List<String>?
            //ukept = documentSnapshot.get("kept_messages") as List<String>?
        }
        return User(uid, uname, urange)
    }


}


