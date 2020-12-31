package com.s1755183.litter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

    class ForgotPasswordActivity : AppCompatActivity(), View.OnClickListener {
        lateinit var resetButton : Button
        lateinit var email3 : String
        private lateinit var auth: FirebaseAuth
        private val TAG: String = "ForgotActivity"

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_forgot)
            auth = FirebaseAuth.getInstance()
            resetButton = findViewById(R.id.buttonReset)
            resetButton.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            when(view?.id){
                R.id.buttonReset -> {
                    Log.i(TAG, "reset button clicked")
                    email3 = (findViewById<EditText>(R.id.editTextEmail3)).text.toString()
                    Log.i(TAG, email3)
                    if (email3 != "" && email3.contains('@') && email3.contains('.')) {
                        auth.sendPasswordResetEmail(email3).addOnCompleteListener(this, OnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val intent = Intent(this, LoginActivity::class.java)
                                    val builder = AlertDialog.Builder(this)
                                    builder.setTitle("Reset Email Sent")
                                    builder.setMessage("Please check your email for a link to reset your password.")
                                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                                    builder.setPositiveButton("OK") { dialog, which ->
                                        startActivity(intent)
                                        finish()
                                    }
                                    val alertDialog: AlertDialog = builder.create()
                                    alertDialog.setCancelable(false)
                                    alertDialog.show()
                                    Log.i(TAG, "reset link sent")
                                } else {
                                    Helper.displayAlert(this, "Reset Failed", "Failed to email password-reset link.")
                                    Log.i(TAG, "unable to send email")
                                }
                            })
                    } else {
                        Helper.displayAlert(this, "Reset Failed", "Please enter a valid email.")
                        Log.i(TAG, "bad email entered")
                    }
                }
            }
        }
    }
