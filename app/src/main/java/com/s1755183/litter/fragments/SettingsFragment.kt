package com.s1755183.litter.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.*
import com.s1755183.litter.R


class SettingsFragment : Fragment(R.layout.fragment_settings), OnMapReadyCallback, View.OnClickListener {

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
    private lateinit var sMap: GoogleMap
    private val TAG: String = "SettingsFragment"
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db.collection("users").document(auth.uid!!).get().addOnSuccessListener { document ->
            if (document != null) {
                currentUser.messages_made = (document.data?.get("messages_made") as Long).toInt()
                currentUser.messages_kept = (document.data?.get("messages_kept") as Long).toInt()
                currentUser.messages_seen = (document.data?.get("messages_seen") as Long).toInt()
                currentUser.comments_made = (document.data?.get("comments_made") as Long).toInt()
                currentUser.views_got = (document.data?.get("views_got") as Long).toInt()
                currentUser.keeps_got = (document.data?.get("keeps_got") as Long).toInt()
                currentUser.comments_got = (document.data?.get("comments_got") as Long).toInt()
                messagesMade.text =  "Messages Posted: " + currentUser.messages_made.toString()
                messagesKept.text =  "Messages Kept: "+ currentUser.messages_kept.toString()
                messagesSeen.text =  "Messages Seen: "+ currentUser.messages_seen.toString()
                commentsMade.text = "Comments Made: "+ currentUser.comments_made.toString()
                viewsGot.text =  "Views on my messages: "+ currentUser.views_got.toString()
                keepsGot.text =  "Keeps on my messages: "+ currentUser.keeps_got.toString()
                commentsGot.text =  "Comments on my messages: "+ currentUser.comments_got.toString()
            }
        }
        val mapFragmentLoad = childFragmentManager.findFragmentById(R.id.mapViewSettings) as? SupportMapFragment
        mapFragmentLoad?.getMapAsync(this)

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
        name.text =  currentUser.name
        email.text = auth.currentUser!!.email as String

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
                    startActivity(intent)
                    this.requireActivity().finish()
                }
                builder.setNegativeButton("Cancel") { _, _ -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        sMap = googleMap
        val sydney = LatLng(-34.0, 151.0)
        sMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        sMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

}