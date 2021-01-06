package com.s1755183.litter.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.s1755183.litter.UIHelper
import com.s1755183.litter.R

class ResetFragment : Fragment(R.layout.fragment_reset), View.OnClickListener {
        lateinit var resetButton : Button
        lateinit var email3EditText: EditText
        private lateinit var auth: FirebaseAuth
        private val TAG: String = "ResetFragment"
        lateinit var backButton: Button


        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            auth = FirebaseAuth.getInstance()
            resetButton = view.findViewById(R.id.buttonReset)
            resetButton.setOnClickListener(this)
            email3EditText = view.findViewById(R.id.editTextEmail3)
            backButton = requireActivity().findViewById(R.id.buttonBackAuth)
            backButton.visibility = View.VISIBLE
            backButton.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when(view?.id){
                R.id.buttonReset -> {
                    Log.i(TAG, "reset button clicked")
                    val email3 : String = email3EditText.text.toString()
                    Log.i(TAG, email3)
                    if (email3 != "" && email3.contains('@') && email3.contains('.')) {
                        auth.sendPasswordResetEmail(email3).addOnCompleteListener(this.requireActivity(), OnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(this.requireContext(), LoginFragment::class.java)
                                    val builder = AlertDialog.Builder(this.requireContext())
                                    builder.setTitle("Reset Email Sent")
                                    builder.setMessage("Please check your email for a link to reset your password.")
                                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                                    builder.setPositiveButton("OK") { dialog, which ->
                                        parentFragmentManager.beginTransaction().apply {
                                            replace(R.id.frameLayoutAuth,LoginFragment())
                                            setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                            addToBackStack(null)
                                            commit()
                                        }
                                    }
                                    val alertDialog: AlertDialog = builder.create()
                                    alertDialog.setCancelable(false)
                                    alertDialog.show()
                                    Log.i(TAG, "reset link sent")
                                } else {
                                    UIHelper.displayAlert(this.requireContext(), "Reset Failed", "Failed to email password-reset link.")
                                    Log.i(TAG, "unable to send email")
                                }
                            })
                    } else {
                        UIHelper.displayAlert(this.requireContext(), "Reset Failed", "Please enter a valid email.")
                        Log.i(TAG, "bad email entered")
                    }
                }
                R.id.buttonBackAuth -> {
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayoutAuth, LoginFragment())
                        backButton.visibility = View.INVISIBLE
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        commit()
                    }
                }
            }
        }
    }
