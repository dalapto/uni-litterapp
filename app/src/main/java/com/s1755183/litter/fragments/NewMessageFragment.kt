package com.s1755183.litter.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.*
import java.net.URI
import java.util.*


class NewMessageFragment : Fragment(R.layout.fragment_new_message), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private lateinit var nextbutton: Button
    private lateinit var backbutton: Button
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switch: Switch
    private lateinit var checkBox: CheckBox
    private lateinit var time: TextView
    private lateinit var imageView: ImageView
    private lateinit var author: TextView
    private lateinit var tapimage: TextView
    private lateinit var title: EditText
    private lateinit var message: EditText
    private lateinit var viewPager: ViewPager
    private lateinit var frameLayoutMain: FrameLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var newMessageButton: FloatingActionButton
    private lateinit var auth: FirebaseAuth
    private val TAG: String = "NewMessageFragment"
    private var titleText: String = ""
    private var messageText: String = ""
    private var imageMessage: Uri? = null
    private var anonymousPost: Boolean = false
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        super.onViewCreated(view, savedInstanceState)
        nextbutton = view.findViewById(R.id.buttonNewMessageNext)
        nextbutton.setOnClickListener(this)
        backbutton = view.findViewById(R.id.buttonNewMesssageCancel)
        backbutton.setOnClickListener(this)
        switch = view.findViewById(R.id.switchImage)
        switch.setOnCheckedChangeListener(this)
        checkBox = view.findViewById(R.id.checkBoxPost)
        checkBox.setOnCheckedChangeListener(this)
        imageView = view.findViewById(R.id.imageViewNewMessage)
        imageView.setOnClickListener(this)
        title = view.findViewById(R.id.editTextNewTitle)
        message = view.findViewById(R.id.editTextNewMesssageText)
        tapimage = view.findViewById(R.id.textViewTapImage)
        time = view.findViewById(R.id.textViewNewMessageTime)
        time.text = Timestamp.now().toDate().toString()
        author = view.findViewById(R.id.textViewNewMessageAuthor)
        author.text = currentUser.name
        frameLayoutMain = requireActivity().findViewById(R.id.frameLayoutMain)
        viewPager = requireActivity().findViewById(R.id.viewPager)
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout)
        newMessageButton = requireActivity().findViewById(R.id.floatingActionButtonNewMessage)
        val details = (activity as MainActivity?)!!.getMessageDetails()
        if (details != null) {
            titleText = details.title.toString()
            messageText = details.text.toString()
            if (details.image != "") {
                imageMessage = (activity as MainActivity?)!!.getImageURI()
                imageView.setImageURI(imageMessage)
            }
            anonymousPost = details.anonymous
            if (titleText != "") {
                title.setText(titleText)
            }
            if (messageText != "") {
                message.setText(messageText)
            }
            switch.isChecked = imageMessage != null
            checkBox.isChecked = anonymousPost
        }
    }


    override fun onClick(v: View?) {

        when (v?.id) {
            R.id.buttonNewMesssageCancel -> {
                Log.i(TAG, "CANCEL CLICKED")
                val builder = AlertDialog.Builder(this.requireContext())
                builder.setTitle("Cancel Posting New Message")
                builder.setMessage("Are you sure you want to leave without posting this message?")
                builder.setPositiveButton("Quit") { dialog, which ->
                    (activity as MainActivity?)!!.resetMessage()
                    viewPager.visibility = View.VISIBLE
                    appBarLayout.visibility = View.VISIBLE
                    newMessageButton.visibility = View.VISIBLE
                    frameLayoutMain.visibility = View.GONE
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayoutMain,Fragment())
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        addToBackStack(null)
                        commit()
                    }
                }
                builder.setNegativeButton("Cancel") { _, _ -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
            R.id.buttonNewMessageNext -> {
                titleText = title.text.toString()
                messageText = message.text.toString()
                if (titleText == "") {
                    UIHelper.displayAlert(this.requireContext(),"Empty Message","Please enter a Title for your message.")
                }
                else {
                    db.collection("messages").document(titleText).get().addOnSuccessListener { document ->
                        if (!document.exists()) {
                            var imagetext = ""
                            Log.i(TAG,(switch.isChecked).toString())
                            Log.i(TAG,(imageMessage != null).toString())
                            if (((messageText != "") && !switch.isChecked) || ((imageMessage != null) && switch.isChecked)) { //check if text/image is not empty
                                if (!switch.isChecked) {
                                    messageText = message.text.toString()
                                    imageMessage = null
                                    imagetext = ""
                                }
                                else {
                                    (activity as MainActivity?)!!.saveImageURI(imageMessage)
                                    messageText = ""
                                    imagetext = "1"
                                }
                                anonymousPost = checkBox.isChecked
                                (activity as MainActivity?)!!.saveMessageDetails(titleText, messageText, anonymousPost, imagetext)
                                parentFragmentManager.beginTransaction().apply {
                                    replace(R.id.frameLayoutMain, ReviewMessageFragment())
                                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                    addToBackStack(null)
                                    commit()
                                }
                            }
                            else {
                                UIHelper.displayAlert(this.requireContext(),"Empty Message","Please include an image or some text in your message.")
                            }
                        }
                        else {
                            UIHelper.displayAlert(this.requireContext(),"Title already Exists","A message with the title $titleText already exists, please choose another.")
                        }
                    }
                }
            }
            R.id.imageViewNewMessage -> {
                Log.i(TAG, "IMAGE VIEW CLICKED")
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(intent,1)


                //ask for image
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==1 && resultCode==RESULT_OK && data != null && data.data != null) {
            imageMessage = data.data!!
            imageView.setImageURI(imageMessage)
            tapimage.visibility = View.INVISIBLE
            Log.i(TAG,(imageMessage != null).toString())
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when (buttonView?.id) {
            R.id.checkBoxPost -> {
                if (isChecked) {
                    Log.i(TAG, "ANONYMOUS CHECKED")
                    author.text = ("Anonymous " + "Individual")
                }
                else {
                    Log.i(TAG, "NOT ANONYMOUS")
                    author.text = currentUser.name
                }
            }
            R.id.switchImage -> {
                if (isChecked) {
                    Log.i(TAG, "IMAGE CHECKED")
                    imageView.visibility = View.VISIBLE
                    if (imageMessage == null) {
                        tapimage.visibility = View.VISIBLE
                    }
                    message.visibility = View.GONE
                }
                else {
                    Log.i(TAG, "TEXT CHECKED")
                    imageView.visibility = View.GONE
                    tapimage.visibility = View.GONE
                    message.visibility = View.VISIBLE
                }
            }
        }
    }

}