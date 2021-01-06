package com.s1755183.litter.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.s1755183.litter.R
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.DocumentReference
import com.s1755183.litter.AuthenticationActivity


class SettingsFragment : Fragment(R.layout.fragment_settings), View.OnClickListener {

    private lateinit var logoutbutton: Button


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logoutbutton = view.findViewById(R.id.buttonLogout)
        logoutbutton.setOnClickListener(this)
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