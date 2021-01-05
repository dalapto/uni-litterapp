package com.s1755183.litter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity(), View.OnClickListener {

        lateinit var loginButton : Button
        lateinit var forgotPasswordButton : TextView
        lateinit var createAccountButton : TextView
        lateinit var emailEditText : EditText
        lateinit var passwordEditText : EditText
        private lateinit var auth: FirebaseAuth
        private val TAG: String = "LoginActivity"


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_login)
            auth = FirebaseAuth.getInstance()
            loginButton = findViewById(R.id.buttonLogin)
            loginButton.setOnClickListener(this)
            forgotPasswordButton = findViewById(R.id.textViewForgotPassword)
            forgotPasswordButton.setOnClickListener(this)
            createAccountButton = findViewById(R.id.textViewCreateAccount)
            createAccountButton.setOnClickListener(this)
            emailEditText = findViewById(R.id.editTextEmail2)
            passwordEditText = findViewById(R.id.editTextPassword2)

        }

    override fun onResume() {
        super.onResume()
        if(auth.currentUser != null ){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

        override fun onClick(view: View?) {
            when(view?.id){
                R.id.buttonLogin-> {
                    Log.i(TAG, "login button clicked")
                    val givenEmail = emailEditText.text.toString()
                    val givenPassword = passwordEditText.text.toString()
                    Log.i(TAG, givenEmail)
                    Log.i(TAG, givenPassword)
                    Helper.displayProgress(findViewById(R.id.progressBarLogin))
                    if (givenEmail != "" && givenPassword != "") {
                        auth.signInWithEmailAndPassword(givenEmail, givenPassword).addOnCompleteListener(this@LoginActivity, OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.i(TAG, "sucessfully logged in")
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                Helper.hideProgress(findViewById(R.id.progressBarLogin))
                                startActivity(intent)
                                finish()
                            } else {
                                Helper.hideProgress(findViewById(R.id.progressBarLogin))
                                Helper.displayAlert(this, "Login Failed","Please ensure your password and email are correct.")
                                Log.i(TAG, "login failed")
                            }
                        })
                    }
                    else {
                        Helper.hideProgress(findViewById(R.id.progressBarLogin))
                        Helper.displayAlert(this, "Login Failed","Please enter an email and a password.")
                        Log.i(TAG, "null email or password")
                    }
                }
                R.id.textViewForgotPassword->{
                    val intent = Intent(this, ForgotPasswordActivity::class.java)
                    startActivity(intent)
                }
                R.id.textViewCreateAccount->{
                    val intent = Intent(this, SignupActivity::class.java)
                    startActivity(intent)
                }
            }
        }
}


