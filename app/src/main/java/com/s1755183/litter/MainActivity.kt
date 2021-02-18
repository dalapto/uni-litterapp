package com.s1755183.litter

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.fragments.MapFragment
import com.s1755183.litter.fragments.MessagesFragment
import com.s1755183.litter.fragments.NewMessageFragment
import com.s1755183.litter.fragments.SettingsFragment
import com.s1755183.litter.fragments.adapters.ViewPagerAdapter


class MainActivity :  AppCompatActivity() {

    private val TAG: String = "MainActivity"
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var tabLayout: TabLayout
    lateinit var mapsTab : TabItem
    lateinit var messagesTab : TabItem
    lateinit var logoutTab : TabItem
    lateinit var viewPager: ViewPager
    lateinit var frameLayoutMain: FrameLayout
    lateinit var appBarLayout: AppBarLayout


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
        auth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_main)
        frameLayoutMain = findViewById(R.id.frameLayoutMain)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        appBarLayout = findViewById(R.id.appBarLayout)
        setupTabs()
        Log.i(TAG, auth.currentUser!!.uid)
        db.collection("users").document(auth.uid!!).get().addOnSuccessListener { document ->
            if (document != null) {
                currentUser.id = document.data?.get("id") as String
                currentUser.name = document.data?.get("name") as String
                currentUser.pickup_range = document.data?.get("pickup_range") as Double
                currentUser.my_messages = stringToList(document.data?.get("my_messages") as String?)
                currentUser.kept_messages = stringToList(document.data?.get("kept_messages") as String?)
            }
        }

        db.collection("users").document(auth.uid!!).get().addOnSuccessListener { document ->
            if (document != null) {
                Toast.makeText(this, "Welcome ${document.data?.get("name").toString()}!", Toast.LENGTH_LONG).show()
            }
        }


    }



    private var newmessage : Message? = null
    private var imageuri : Uri? = null

    fun saveMessageDetails(title: String, text: String?, anonymous: Boolean, image: String?) {
        newmessage = Message(title =title, text = text, anonymous = anonymous, image = image)
    }

    fun saveMessage(msg: Message) {
        newmessage = msg
    }

    fun getMessageDetails() : Message? {
        return newmessage
    }
    fun resetMessage() {
        newmessage = null
        imageuri = null
    }

    fun saveImageURI(imageMessage: Uri?) {
        imageuri = imageMessage
    }

    fun getImageURI(): Uri? {
        return imageuri
    }

    fun deleteComment() {

    }


}


