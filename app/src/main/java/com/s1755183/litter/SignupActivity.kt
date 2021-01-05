package com.s1755183.litter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore




class SignupActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var registerButton : Button
    lateinit var email2 : String
    lateinit var password2 : String
    lateinit var confirmPassword : String
    lateinit var username : String
    private lateinit var auth: FirebaseAuth
    private val TAG: String = "SignupActivity"
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        auth = FirebaseAuth.getInstance()
        registerButton = findViewById(R.id.buttonRegister)
        registerButton.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.buttonRegister -> {
                Log.i(TAG, "register button clicked")
                username = (findViewById<EditText>(R.id.editTextUsername)).text.toString()
                email2 = (findViewById<EditText>(R.id.editTextEmail2)).text.toString()
                password2 = (findViewById<EditText>(R.id.editTextPassword2)).text.toString()
                confirmPassword = (findViewById<EditText>(R.id.editTextConfirmPassword)).text.toString()
                Log.i(TAG, email2)
                Log.i(TAG, password2)
                Helper.displayProgress(findViewById(R.id.progressBarRegister))
                if (email2 != "" && email2.contains('@') && email2.contains('.')) {
                    if (password2.length > 5 && password2.contains(Regex("[A-Z]")) && password2.contains(Regex("[0-9]")) && password2.contains(Regex("[a-z]"))) {
                        if (password2 == confirmPassword) {
                            auth.createUserWithEmailAndPassword(email2, password2).addOnCompleteListener(this, OnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.i(TAG, "sucessful register")
                                    Toast.makeText(this, "Successfully Registered", Toast.LENGTH_LONG).show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    val user = User(auth.uid!!, username)
                                    db.collection("users").document(auth.uid!!).set(user).addOnSuccessListener { Log.i(TAG,"DocumentSnapshot added with ID: ${auth.uid!!}") }
                                    Helper.hideProgress(findViewById(R.id.progressBarRegister))
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Helper.hideProgress(findViewById(R.id.progressBarRegister))
                                    Log.i(TAG, "failed register")
                                    Helper.displayAlert(this, "Registration Failed", "Please check your internet connection.")
                                    Toast.makeText(this, "Registration Failed", Toast.LENGTH_LONG).show()
                                }
                            })
                        } else {
                            Helper.hideProgress(findViewById(R.id.progressBarLogin))
                            Helper.displayAlert(this, "Registration Failed", "Passwords do not match.")
                            Log.i(TAG, "password mismatch")
                        }
                    } else {
                        Helper.hideProgress(findViewById(R.id.progressBarLogin))
                        Helper.displayAlert(this, "Registration Failed", "Passwords must have at least 1 uppercase letter, 1 lowercase letter, 1 number and be at least 6 characters long.")
                        Log.i(TAG, "password too short")
                    }
                } else {
                    Helper.hideProgress(findViewById(R.id.progressBarLogin))
                    Helper.displayAlert(this, "Registration Failed", "Please enter an email.")
                    Log.i(TAG, "no email entered")
                }
            }
        }
    }




}


