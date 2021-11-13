package com.s1755183.litter

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.fragments.MapFragment
import com.s1755183.litter.fragments.MessagesFragment
import com.s1755183.litter.fragments.SettingsFragment
import com.s1755183.litter.fragments.adapters.ViewPagerAdapter


class MainActivity :  AppCompatActivity() {

    private val TAG: String = "MainActivity"
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    lateinit var tabLayout: TabLayout
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
                currentUser.messages_made = (document.data?.get("messages_made") as Long).toInt()
                currentUser.followed_authors = (document.data?.get("messages_kept") as Long).toInt()
                currentUser.messages_seen = (document.data?.get("messages_seen") as Long).toInt()
                currentUser.comments_made = (document.data?.get("comments_made") as Long).toInt()
                Toast.makeText(this, "Welcome ${document.data?.get("name").toString()}!", Toast.LENGTH_LONG).show()
            }
        }

    }



    private var newmessage : Message? = null
    private var imageuri : Uri? = null


    fun saveMessageDetails(title: String, text: String?, anonymous: Boolean, image: String?) {
        newmessage = Message(title = title, text = text, anonymous = anonymous, image = image)
    }

    fun saveMessage(msg: Message) {
        newmessage = msg
    }

    fun incrementComments() {
        newmessage?.comments = newmessage?.comments?.plus(1)!!
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

}


