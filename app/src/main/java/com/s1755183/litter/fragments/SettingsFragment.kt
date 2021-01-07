package com.s1755183.litter.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import androidx.fragment.app.Fragment
import com.s1755183.litter.*


class SettingsFragment : Fragment(R.layout.fragment_settings), View.OnClickListener {

    private lateinit var logoutbutton: Button
    private lateinit var email: TextView
    private lateinit var name: TextView
    private val TAG: String = "SettingsFragment"
    private lateinit var auth: FirebaseAuth




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        super.onViewCreated(view, savedInstanceState)
        logoutbutton = view.findViewById(R.id.buttonLogout)
        logoutbutton.setOnClickListener(this)
        email = view.findViewById(R.id.textViewEmail)
        name = view.findViewById(R.id.textViewUsername)
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


}