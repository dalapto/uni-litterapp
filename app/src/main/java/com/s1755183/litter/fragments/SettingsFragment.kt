package com.s1755183.litter.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.s1755183.litter.LoginActivity
import com.s1755183.litter.R
import androidx.fragment.app.Fragment



class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

//    fun onClick(view: View?) {
//        when (view?.id) {
//            R.id.buttonLogin -> {
//                val intent = Intent(this.requireContext(), LoginActivity::class.java)
//                val builder = AlertDialog.Builder(this.requireContext())
//                builder.setTitle("Log Out")
//                builder.setMessage("Are you sure you want to log out?")
//                builder.setPositiveButton("Logout") { dialog, which ->
//                    FirebaseAuth.getInstance().signOut()
//                    startActivity(intent)
//                }
//                builder.setNegativeButton("Cancel") { _, _ -> }
//                val alertDialog: AlertDialog = builder.create()
//                alertDialog.setCancelable(false)
//                alertDialog.show()
//            }
//        }
//    }
}