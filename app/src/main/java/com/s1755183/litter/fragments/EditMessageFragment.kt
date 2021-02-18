package com.s1755183.litter.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Tasks
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.s1755183.litter.*
import com.s1755183.litter.R
import java.io.File
import java.util.*


class EditMessageFragment : Fragment(R.layout.fragment_edit_message), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private val TAG : String = "ViewMessage"
    private lateinit var frameLayoutMain: FrameLayout
    private lateinit var backButton: Button
    private lateinit var updateButton: Button
    private lateinit var deleteButton: Button
    private lateinit var commentsButton: Button
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switch: Switch
    private lateinit var checkBox: CheckBox
    private lateinit var imageView: ImageView
    private lateinit var tapimage2: TextView
    private lateinit var viewcount: TextView
    private lateinit var keeps: TextView
    private lateinit var time: TextView
    private lateinit var author: TextView
    private lateinit var title: TextView
    private lateinit var message: EditText
    private lateinit var viewPager: ViewPager
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var newMessageButton: FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var msg: Message
    private var imageMessage: Uri? = null
    private var oldimage: Boolean = false



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        msg = (activity as MainActivity?)!!.getMessageDetails()!!
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
        commentsButton = view.findViewById(R.id.buttonViewComments2)
        commentsButton.setOnClickListener(this)
        backButton = view.findViewById(R.id.buttonEditMessageBack)
        backButton.setOnClickListener(this)
        updateButton = view.findViewById(R.id.buttonEditMessageUpdate)
        updateButton.setOnClickListener(this)
        deleteButton = view.findViewById(R.id.buttonDelete)
        deleteButton.setOnClickListener(this)
        switch = view.findViewById(R.id.switchEditImage)
        switch.setOnCheckedChangeListener(this)
        viewcount = view.findViewById(R.id.textViewEditMessageViewcount)
        tapimage2 = view.findViewById(R.id.textViewTapImage2)
        keeps = view.findViewById(R.id.textViewEditKeeps)
        imageView = view.findViewById(R.id.imageViewEditMessage)
        imageView.setOnClickListener(this)
        checkBox = view.findViewById(R.id.checkBoxEditPost)
        checkBox.setOnCheckedChangeListener(this)
        title = view.findViewById(R.id.textViewEditTitle)
        message = view.findViewById(R.id.editTextEditMesssageText)
        time = view.findViewById(R.id.textViewEditMessageTime)
        author = view.findViewById(R.id.textViewEditMessageAuthor)
        frameLayoutMain = requireActivity().findViewById(R.id.frameLayoutMain)
        viewPager = requireActivity().findViewById(R.id.viewPager)
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout)
        newMessageButton = requireActivity().findViewById(R.id.floatingActionButtonNewMessage)
        Log.i(TAG, msg.time)
        db.collection("messages").document(msg.title!!).get().addOnSuccessListener { document ->
            if (document != null) {
                title.text = msg.title
                time.text = msg.time
                keeps.text = "Keeps " + document.data?.get("keeps").toString()
                viewcount.text = "Views " + document.data?.get("views").toString()
                commentsButton.text = " " + document.data?.get("comments").toString() + " Comments"
                if (document.data?.get("text").toString() == "") {
                    imageView.visibility = View.VISIBLE
                    switch.isChecked = true
                    storageReference.child("images/${document.data?.get("image").toString()}").getBytes((7L * 1024 * 1024)).addOnSuccessListener { bytes ->
                        imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                        oldimage = true
                        Log.i(TAG, "SET IMAGE")
                    }
                    message.visibility = View.INVISIBLE
                    tapimage2.visibility = View.INVISIBLE
                } else {
                    message.setText(document.data?.get("text").toString())
                    message.visibility = View.VISIBLE
                    imageView.visibility = View.INVISIBLE
                    switch.isChecked = false
                }
                if (document.data?.get("anonymous") as Boolean) {
                    author.text = "Anonymous Individual"
                    checkBox.isChecked = true
                } else {
                    db.collection("users").document(msg.author_id!!).get().addOnSuccessListener { document2 ->
                        if (document2 != null) {
                            checkBox.isChecked = false
                            author.text = document2.data?.get("name") as String
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==1 && resultCode== Activity.RESULT_OK && data != null && data.data != null) {
            imageMessage = data.data!!
            imageView.setImageURI(imageMessage)
            tapimage2.visibility = View.INVISIBLE
            Log.i(TAG,(imageMessage != null).toString())
        }
    }

    override fun onClick(v: View?) {
        Log.i(TAG, "CLICKED")
        when (v?.id) {
            R.id.buttonViewComments2 -> {
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.frameLayoutMain, CommentsFragment())
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    addToBackStack(null)
                    commit()
                }
            }
            R.id.buttonEditMessageBack -> {
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
            R.id.imageViewEditMessage -> {

                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent,1)


                //ask for image
            }
            R.id.buttonEditMessageUpdate -> {
                var wantupdate = false
                var canupdate = true
                var updatetext = false
                var updateimage = false
                val doc = db.collection("messages").document(msg.title!!)
                if (!switch.isChecked) {
                    if (msg.text != message.text.toString()) {
                        wantupdate = true
                        updatetext = true
                        if (message.text.toString() == "") {
                            UIHelper.displayAlert(this.requireContext(), "Empty Message", "Please enter a message to upload.")
                            canupdate = false
                        }
                    }
                } else {
                    if (!oldimage) {
                        wantupdate = true
                        if (imageMessage == null) {
                            Log.i(TAG, "we have an empty image")
                            canupdate = false
                            UIHelper.displayAlert(this.requireContext(), "Empty Message", "Please enter an image to upload.")
                        }
                        else {
                            updateimage = true
                            Log.i(TAG, "we have a new image")
                        }
                    }
                }
                if (msg.anonymous != checkBox.isChecked) wantupdate = true
                if (wantupdate) {
                    if (canupdate) {
                        if (updatetext) {
                            doc.update("image", "")
                            doc.update("text", message.text.toString())
                        }
                        if (updateimage) {
                            val randomKey : String = UUID.randomUUID().toString()
                            val storageRef: StorageReference = storageReference.child("images/$randomKey")
                            storageRef.putFile(imageMessage!!).addOnSuccessListener { taskSnapshot ->
                                doc.update("image", randomKey)
                                doc.update("text", "")
                            }
                        }
                        doc.update("anonymous", checkBox.isChecked)
                        Toast.makeText(this.requireContext(),"Sucessfully updated message.", Toast.LENGTH_LONG).show()
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
                }
                else {
                    UIHelper.displayAlert(this.requireContext(),"Nothing to Update","Please make some changes to your message before attempting to update it.")
                }
            }
            R.id.buttonDelete -> {
                val builder = AlertDialog.Builder(this.requireContext())
                builder.setTitle("Delete Message")
                builder.setMessage("Are you sure you want to delete this message?")
                builder.setPositiveButton("Delete") { dialog, which ->
                    (activity as MainActivity?)!!.resetMessage()
                    viewPager.visibility = View.VISIBLE
                    appBarLayout.visibility = View.VISIBLE
                    newMessageButton.visibility = View.VISIBLE
                    frameLayoutMain.visibility = View.GONE
                    db.collection("messages").document(msg.title.toString()).delete()
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayoutMain, Fragment())
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        addToBackStack(null)
                        commit()
                    }
                    Toast.makeText(this.requireContext(),"Sucessfully deleted message.", Toast.LENGTH_LONG).show()
                }
                builder.setNegativeButton("Cancel") { _, _ -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.checkBoxEditPost -> {
                if (isChecked) {
                    Log.i(TAG, "ANONYMOUS CHECKED")
                    author.text = ("Anonymous " + "Individual")
                }
                else {
                    Log.i(TAG, "NOT ANONYMOUS")
                    author.text = currentUser.name
                }
            }
            R.id.switchEditImage -> {
                if (isChecked) {
                    Log.i(TAG, "IMAGE CHECKED")
                    imageView.visibility = View.VISIBLE
                    if (imageMessage == null && !oldimage)  {
                        tapimage2.visibility = View.VISIBLE
                    }
                    message.visibility = View.GONE
                }
                else {
                    Log.i(TAG, "TEXT CHECKED")
                    imageView.visibility = View.GONE
                    tapimage2.visibility = View.GONE
                    message.visibility = View.VISIBLE
                }
            }
        }
    }

}