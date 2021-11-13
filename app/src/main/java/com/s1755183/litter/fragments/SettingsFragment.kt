package com.s1755183.litter.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.ui.IconGenerator
import com.s1755183.litter.*
import com.s1755183.litter.R


class SettingsFragment : Fragment(R.layout.fragment_settings), OnMapReadyCallback, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private lateinit var logoutbutton: Button
    private lateinit var email: TextView
    private lateinit var name: TextView
    private lateinit var messagesMade: TextView
    private lateinit var messagesKept: TextView
    private lateinit var messagesSeen: TextView
    private lateinit var commentsMade: TextView
    private lateinit var viewsGot: TextView
    private lateinit var keepsGot: TextView
    private lateinit var commentsGot: TextView
    private lateinit var revealHelp: ImageButton
    private lateinit var revealCheck: CheckBox
    private lateinit var seeHelp: ImageButton
    private lateinit var seeCheck: CheckBox
    private lateinit var ownHelp: ImageButton
    private lateinit var ownCheck: CheckBox
    private lateinit var keptHelp: ImageButton
    private lateinit var keptCheck: CheckBox
    private lateinit var seenHelp: ImageButton
    private lateinit var seenCheck: CheckBox
    private lateinit var unseenHelp: ImageButton
    private lateinit var unseenCheck: CheckBox
    private lateinit var sMapView: MapView
    private lateinit var sMap: GoogleMap
    private val TAG: String = "SettingsFragment"
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var closeCirc: Circle
    private lateinit var farCirc: Circle
    private lateinit var markerOwn: Marker
    private lateinit var markerKept: Marker
    private lateinit var markerSeen: Marker
    private lateinit var markerAlmostSeen: Marker
    private lateinit var markerUnseen: Marker

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db.collection("users").document(auth.uid!!).get().addOnSuccessListener { document ->
            if (document != null) {
                currentUser.messages_made = (document.data?.get("messages_made") as Long).toInt()
                currentUser.followed_authors = (document.data?.get("messages_kept") as Long).toInt()
                currentUser.messages_seen = (document.data?.get("messages_seen") as Long).toInt()
                currentUser.comments_made = (document.data?.get("comments_made") as Long).toInt()
                currentUser.views_got = (document.data?.get("views_got") as Long).toInt()
                currentUser.followers = (document.data?.get("keeps_got") as Long).toInt()
                currentUser.comments_got = (document.data?.get("comments_got") as Long).toInt()
                messagesMade.text =  "Messages Posted: " + currentUser.messages_made.toString()
                messagesKept.text =  "Messages Kept: "+ currentUser.followed_authors.toString()
                messagesSeen.text =  "Messages Seen: "+ currentUser.messages_seen.toString()
                commentsMade.text = "Comments Made: "+ currentUser.comments_made.toString()
                viewsGot.text =  "Views on my messages: "+ currentUser.views_got.toString()
                keepsGot.text =  "Keeps on my messages: "+ currentUser.followers.toString()
                commentsGot.text =  "Comments on my messages: "+ currentUser.comments_got.toString()
            }
        }
        logoutbutton = view.findViewById(R.id.buttonLogout)
        logoutbutton.setOnClickListener(this)
        email = view.findViewById(R.id.textViewEmail)
        name = view.findViewById(R.id.textViewUsername)
        messagesMade = view.findViewById(R.id.textViewPosted)
        messagesKept = view.findViewById(R.id.textViewTotalKept)
        messagesSeen = view.findViewById(R.id.textViewTotalSeen)
        commentsMade = view.findViewById(R.id.textViewTotalComments)
        viewsGot = view.findViewById(R.id.textViewMyViews)
        keepsGot = view.findViewById(R.id.textViewMyKeeps)
        commentsGot = view.findViewById(R.id.textViewMyComments)
        revealCheck = view.findViewById(R.id.checkBoxReveal)
        revealCheck.setOnCheckedChangeListener(this)
        seeCheck = view.findViewById(R.id.checkBoxSeeCircle)
        seeCheck.setOnCheckedChangeListener(this)
        ownCheck = view.findViewById(R.id.checkBoxOwn)
        ownCheck.setOnCheckedChangeListener(this)
        keptCheck = view.findViewById(R.id.checkBoxKept)
        keptCheck.setOnCheckedChangeListener(this)
        seenCheck = view.findViewById(R.id.checkBoxSeen)
        seenCheck.setOnCheckedChangeListener(this)
        unseenCheck = view.findViewById(R.id.checkBoxUnseen)
        unseenCheck.setOnCheckedChangeListener(this)
        revealHelp = view.findViewById(R.id.imageButtonRevealHelp)
        revealHelp.setOnClickListener(this)
        seeHelp = view.findViewById(R.id.imageButtonSeeHelp)
        seeHelp.setOnClickListener(this)
        ownHelp = view.findViewById(R.id.imageButtonOwnHelp)
        ownHelp.setOnClickListener(this)
        keptHelp = view.findViewById(R.id.imageButtonKeptHelp)
        keptHelp.setOnClickListener(this)
        seenHelp = view.findViewById(R.id.imageButtonSeenHelp)
        seenHelp.setOnClickListener(this)
        unseenHelp = view.findViewById(R.id.imageButtonUnseenHelp)
        unseenHelp.setOnClickListener(this)
        name.text =  currentUser.name
        email.text = auth.currentUser!!.email as String
        sMapView = view.findViewById(R.id.mapViewSettings)
        sMapView.isClickable = false
        sMapView.onCreate(null)
        sMapView.getMapAsync(this)
        sMapView.onResume()

    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonLogout -> {
                val intent = Intent(this.requireContext(), AuthenticationActivity::class.java)
                val builder = AlertDialog.Builder(this.requireContext())
                builder.setTitle("Log Out")
                builder.setMessage("Are you sure you want to log out?")
                builder.setPositiveButton("Logout") { dialog, which ->
                    FirebaseAuth.getInstance().signOut()
                    auth.signOut()
                    startActivity(intent)
                    this.requireActivity().finish()
                }
                builder.setNegativeButton("Cancel") { _, _ -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
            R.id.imageButtonRevealHelp -> {
                UIHelper.displayAlert(this.requireContext(),"Reveal Range", "The range at which messages are partially revealed.")
            }
            R.id.imageButtonSeeHelp -> {
                UIHelper.displayAlert(this.requireContext(),"See/View Range", "The range at which messages can be viewed.")
            }
            R.id.imageButtonOwnHelp -> {
                UIHelper.displayAlert(this.requireContext(),"My Messages", "Messages you post on the map.")
            }
            R.id.imageButtonKeptHelp -> {
                UIHelper.displayAlert(this.requireContext(),"Kept Messages", "Messages you saw and kept.")
            }
            R.id.imageButtonSeenHelp -> {
                UIHelper.displayAlert(this.requireContext(),"Seen Messages", "Messages you can view on the map.")
            }
            R.id.imageButtonUnseenHelp -> {
                UIHelper.displayAlert(this.requireContext(),"Unseen Messages", "Messages you have yet to view.")
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        sMap = googleMap
        sMap.setMinZoomPreference(14.0f)
        sMap.setMaxZoomPreference(14.0f)
        sMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(40.6990, -74.0558), 14.0f))
        sMap.addCircle(CircleOptions().center(LatLng(40.6990, -74.0558)).radius(20.0).fillColor(Color.parseColor("#3949AB")).strokeColor(Color.parseColor("#3949AB")))
        if (ownCheck.isChecked) showOwn()
        if (keptCheck.isChecked) showKept()
        if (unseenCheck.isChecked) showUnseen()
        if (seenCheck.isChecked) showSeen()
        if (seeCheck.isChecked) showCloseCircle()
        if (revealCheck.isChecked) showFarCircle()
    }

    fun showOwn() {
        val mIconGenerator = IconGenerator(this.sMapView.context)
        mIconGenerator.setStyle(IconGenerator.STYLE_BLUE)
        val iconOwn: Bitmap = mIconGenerator.makeIcon("My Message")
        markerOwn = sMap.addMarker(MarkerOptions().position(LatLng(40.6952, -74.0514)).icon(BitmapDescriptorFactory.fromBitmap(iconOwn)))

    }

    fun showKept() {
        val mIconGenerator = IconGenerator(this.sMapView.context)
        mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
        val iconKept: Bitmap = mIconGenerator.makeIcon("Kept")
        markerKept = sMap.addMarker(MarkerOptions().position(LatLng(40.7011, -74.0559)).icon(BitmapDescriptorFactory.fromBitmap(iconKept)))
    }

    fun showSeen() {
        val mIconGenerator = IconGenerator(this.sMapView.context)
        mIconGenerator.setStyle(IconGenerator.STYLE_GREEN)
        val iconSeen: Bitmap = mIconGenerator.makeIcon("Seen")
        markerSeen = sMap.addMarker(MarkerOptions().position(LatLng(40.7037, -74.0528)).icon(BitmapDescriptorFactory.fromBitmap(iconSeen)))
    }

    fun showUnseen() {
        val mIconGenerator = IconGenerator(this.sMapView.context)
        mIconGenerator.setStyle(IconGenerator.STYLE_GREEN)
        val iconNotQuite: Bitmap = mIconGenerator.makeIcon("Unseen")
        markerAlmostSeen = sMap.addMarker(MarkerOptions().position(LatLng(40.6965, -74.0600)).icon(BitmapDescriptorFactory.fromBitmap(iconNotQuite)))
        val iconUnseen: Bitmap = mIconGenerator.makeIcon("????")
        markerUnseen = sMap.addMarker(MarkerOptions().position(LatLng(40.7045, -74.0603)).icon(BitmapDescriptorFactory.fromBitmap(iconUnseen)))
    }

    fun showFarCircle() {
        farCirc = sMap.addCircle(CircleOptions().center(LatLng(40.6990, -74.0558)).radius(500.0).fillColor(Color.parseColor("#287198e7")).strokeColor(Color.parseColor("#087198e7")))
    }

    fun showCloseCircle() {
        closeCirc = sMap.addCircle(CircleOptions().center(LatLng(40.6990, -74.0558)).radius(175.0).fillColor(Color.parseColor("#3271cce7")).strokeColor(Color.parseColor("#1071cce7")))
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.checkBoxReveal -> {
                if (revealCheck.isChecked) {
                    showFarCircle()
               }
                else {
                    farCirc.remove()
               }
                displayRevealCircle = revealCheck.isChecked
            }
            R.id.checkBoxSeeCircle -> {
                if (seeCheck.isChecked) {
                    showCloseCircle()
                }
                else {
                    closeCirc.remove()
                }
                displaySeeCircle = seeCheck.isChecked
            }
            R.id.checkBoxOwn -> {
                if (ownCheck.isChecked) {
                    showOwn()
                }else {
                    markerOwn.remove()
                }
                displayOwn = ownCheck.isChecked
            }
            R.id.checkBoxKept -> {
                if (keptCheck.isChecked) {
                    showKept()
                }
                else {
                    markerKept.remove()
                }
                displayFollowing = keptCheck.isChecked
            }
            R.id.checkBoxSeen -> {
                if (seenCheck.isChecked) {
                    showSeen()
                }
                else {
                    markerSeen.remove()
                }
                displaySeen = seenCheck.isChecked
            }
            R.id.checkBoxUnseen -> {
                if (unseenCheck.isChecked) {
                    showUnseen()
                }
                else {
                    markerAlmostSeen.remove()
                    markerUnseen.remove()
                }
                displayUnseen = unseenCheck.isChecked
            }
        }
    }

}