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
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.location.*
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
    private var image: Uri? = null
    private lateinit var viewPager: ViewPager
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var newMessageButton: FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var msg: Message
    private var imageMessage: Uri? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        msg = (activity as MainActivity?)!!.getMessageDetails()!!
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference
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
        title.text = msg.title
        time.text = msg.time
        keeps.text = "Keeps "+ msg.keeps.toString()
        viewcount.text = "Views "+ msg.views.toString()
        if (msg.text == "") {
            imageView.visibility = View.VISIBLE
            Log.i(TAG, "GETTING IMAGE")
            storageReference.child("images/${msg.image}").getBytes((7L*1024*1024)).addOnSuccessListener{
                bytes -> imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes,0,bytes.size))
                Log.i(TAG, "SET IMAGE")
            }
            switch.isChecked = true
            message.visibility = View.INVISIBLE
        }
        else {
            switch.isChecked = false
            message.setText(msg.text)
            message.visibility = View.VISIBLE
            imageView.visibility = View.INVISIBLE
        }
        if (msg.anonymous) {
            author.text = "Anonymous Individual"
            checkBox.isChecked = true
        }
        else {
            author.text = currentUser.name
            checkBox.isChecked = false
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
            R.id.buttonEditMessageBack -> {
                Log.i(TAG, "BACK CLICKED")
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
                Log.i(TAG, "IMAGE VIEW CLICKED")
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent,1)


                //ask for image
            }
            R.id.buttonEditMessageUpdate -> {
                Log.i(TAG, "BACK CLICKED")
                //(db.collection("messages").document(msg.title.toString())).update("views",(msg.views).toLong())
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
            R.id.buttonDelete -> {
                Log.i(TAG, "BACK CLICKED")
                //(db.collection("messages").document(msg.title.toString())).update("views",(msg.views).toLong())
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
                    if (imageMessage == null && msg.image == "")  {
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