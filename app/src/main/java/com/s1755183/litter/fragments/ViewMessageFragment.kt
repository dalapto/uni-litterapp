package com.s1755183.litter.fragments

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.s1755183.litter.*
import com.s1755183.litter.R
import kotlin.collections.ArrayList


class ViewMessageFragment : Fragment(R.layout.fragment_view_message), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private val TAG : String = "ViewMessage"
    private lateinit var frameLayoutMain: FrameLayout
    private lateinit var viewLayout: ConstraintLayout
    private lateinit var backButton: Button
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switch: Switch
    private lateinit var toggleFollow: ToggleButton
    private lateinit var imageView: ImageView
    private lateinit var switchkeeptext: TextView
    private lateinit var viewcount: TextView
    private lateinit var keeps: TextView
    private lateinit var time: TextView
    private lateinit var author: TextView
    private lateinit var title: TextView
    private lateinit var message: TextView
    private lateinit var commentsButton: Button
    private var image: Uri? = null
    private lateinit var viewPager: ViewPager
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var newMessageButton: FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var msg: Message
    private var followers: Int = 0
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var comments_list: ArrayList<Comment> = ArrayList<Comment>()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        msg = (activity as MainActivity?)!!.getMessageDetails()!!
        viewLayout = view.findViewById(R.id.fragmentViewMessage)
        commentsButton = view.findViewById(R.id.buttonViewComments)
        commentsButton.setOnClickListener(this)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        backButton = view.findViewById(R.id.buttonBackView)
        backButton.setOnClickListener(this)
        toggleFollow = view.findViewById(R.id.toggleButtonFollow)
        toggleFollow.setOnCheckedChangeListener(this)
        switchkeeptext = view.findViewById(R.id.textViewKeep)
        viewcount = view.findViewById(R.id.textViewMessageViewcount)
        keeps = view.findViewById(R.id.textViewKeeps)
        imageView = view.findViewById(R.id.imageViewMessageImage)
        imageView.setOnClickListener(this)
        title = view.findViewById(R.id.textViewMessageTitle)
        message = view.findViewById(R.id.textViewMessageText)
        time = view.findViewById(R.id.textViewMessageTime)
        author = view.findViewById(R.id.textViewMessageAuthor)
        frameLayoutMain = requireActivity().findViewById(R.id.frameLayoutMain)
        viewPager = requireActivity().findViewById(R.id.viewPager)
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout)
        newMessageButton = requireActivity().findViewById(R.id.floatingActionButtonNewMessage)
        db.collection("users").document(auth.uid!!).get().addOnSuccessListener { document ->
            if (document != null) {
                currentUser.messages_made = (document.data?.get("messages_made") as Long).toInt()
//                currentUser.followed_authors = (document.data?.get("messages_kept") as Long).toInt()
                currentUser.messages_seen = (document.data?.get("messages_seen") as Long).toInt()
                currentUser.comments_made = (document.data?.get("comments_made") as Long).toInt()
            }
        }
//        db.collection("users").document(currentUser.id).collection("seenmessages").document(msg.title!!).get().addOnSuccessListener {
//            doc -> if (doc.data?.get("kept") as Boolean) {
//            viewLayout.setBackgroundColor(Color.parseColor("#C1802E"))
//            switch.isChecked = true
//            }
//        }
        db.collection("messages").document(msg.title!!).get().addOnSuccessListener { document ->
            if (document != null) {
                title.text = msg.title
                time.text = msg.time
//                keeps.text = "Keeps " + document.data?.get("keeps").toString()
//                followers = (document.data?.get("followers") as Long).toInt()
                viewcount.text = "Views " + document.data?.get("views").toString()
                commentsButton.text = " " + document.data?.get("comments").toString() + " Comments"
                if (document.data?.get("text").toString() == "") {
                    message.text = ""
                    imageView.visibility = View.VISIBLE
                    Log.i(TAG, "GETTING IMAGE")
                    storageReference.child("images/${document.data?.get("image").toString()}").getBytes((7L * 1024 * 1024)).addOnSuccessListener { bytes ->
                        imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                        Log.i(TAG, "SET IMAGE")
                    }
                    message.visibility = View.INVISIBLE
                } else {
                    message.text = document.data?.get("text").toString()
                    message.visibility = View.VISIBLE
                    imageView.visibility = View.INVISIBLE
                }
                if (document.data?.get("anonymous") as Boolean) {
                    author.text = "Anonymous Individual"
                } else {
                    db.collection("users").document(msg.author_id!!).get().addOnSuccessListener { document2 ->
                        if (document2 != null) {
                            author.text = document2.data?.get("name") as String
                        }
                    }
                }
            }
        }
        db.collection("users").document(currentUser.id).collection("following").document(msg.author_id!!).get().addOnSuccessListener {
                doc -> if (doc.data?.get("author_id").toString() == msg.author_id!!) {
                    viewLayout.setBackgroundColor(Color.parseColor("#C1802E"))
                    toggleFollow.isChecked = true
                }
        }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonBackView -> {
                (activity as MainActivity?)!!.resetMessage()
                viewPager.visibility = View.VISIBLE
                appBarLayout.visibility = View.VISIBLE
                newMessageButton.visibility = View.VISIBLE
                frameLayoutMain.visibility = View.GONE
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.frameLayoutMain, Fragment())
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    addToBackStack(null)
                    commit()
                }
            }
            R.id.buttonViewComments -> {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.frameLayoutMain, CommentsFragment())
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.toggleButtonFollow -> {
                var auth_keeps_got = 0
                db.collection("users").document(msg.author_id!!).get().addOnSuccessListener { document ->
                    if (document != null) {
                        auth_keeps_got = (document.data?.get("keeps_got") as Long).toInt()
                    }
                }
                if (isChecked) {
                    viewLayout.setBackgroundColor(Color.parseColor("#C1802E"))
                    db.collection("users").document(currentUser.id).collection("following").document(msg.author_id!!).get().addOnSuccessListener { doc ->
                        if (doc != null) {
                            db.collection("users").document(currentUser.id).update("followed_authors", currentUser.followed_authors+1)
                            db.collection("users").document(msg.author_id!!).update("followers", auth_keeps_got+1)
//                            db.collection("messages").document(msg.title!!).update("keeps",(1+followers).toLong())
                            db.collection("users").document(currentUser.id).collection("following").document(msg.author_id!!).set(mapOf("author_id" to msg.author_id!!))
                            followers++
                        }
                    }
                } else {
                    viewLayout.setBackgroundColor(Color.parseColor("#49A84B"))
                    db.collection("users").document(currentUser.id).collection("following").document(msg.author_id!!).get().addOnSuccessListener { doc ->
                        if (doc != null) {
                            db.collection("users").document(currentUser.id).update("followed_authors", currentUser.followed_authors-1)
                            db.collection("users").document(msg.author_id!!).update("followers", auth_keeps_got-1)
//                            db.collection("messages").document(msg.title!!).update("keeps",(followers-1).toLong())
                            db.collection("users").document(currentUser.id).collection("following").document(msg.author_id.toString()).delete()
                            followers--
                        }
                    }
                }
            }
        }
    }

}