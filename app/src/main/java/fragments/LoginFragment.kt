package com.s1755183.litter.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.s1755183.litter.UIHelper
import com.s1755183.litter.MainActivity
import com.s1755183.litter.R

class LoginFragment : Fragment(R.layout.fragment_login), View.OnClickListener {

        lateinit var loginButton : Button
        lateinit var forgotPasswordButton : TextView
        lateinit var createAccountButton : TextView
        lateinit var emailEditText : EditText
        lateinit var passwordEditText : EditText
        lateinit var progressBar: ProgressBar
        private lateinit var auth: FirebaseAuth
        private val TAG: String = "LoginFragment"



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        loginButton = view.findViewById(R.id.buttonLogin)
        loginButton.setOnClickListener(this)
        forgotPasswordButton = view.findViewById(R.id.textViewForgotPassword)
        forgotPasswordButton.setOnClickListener(this)
        createAccountButton = view.findViewById(R.id.textViewCreateAccount)
        createAccountButton.setOnClickListener(this)
        emailEditText = view.findViewById(R.id.editTextEmail2)
        passwordEditText = view.findViewById(R.id.editTextPassword2)
        progressBar = view.findViewById(R.id.progressBarLogin)

        if(auth.currentUser != null ){
            val intent = Intent(this.requireContext(), MainActivity::class.java)
            startActivity(intent)
            this.requireActivity().finish()
            //DISPLAY welcome back message
        }
    }

        override fun onClick(view: View?) {
            when(view?.id){
                R.id.buttonLogin -> {
                    Log.i(TAG, "login button clicked")
                    val givenEmail = emailEditText.text.toString()
                    val givenPassword = passwordEditText.text.toString()
                    Log.i(TAG, givenEmail)
                    Log.i(TAG, givenPassword)
                    UIHelper.displayProgress(progressBar)
                    if (givenEmail != "" && givenPassword != "") {
                        auth.signInWithEmailAndPassword(givenEmail, givenPassword).addOnCompleteListener(this.requireActivity(), OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.i(TAG, "sucessfully logged in")
                                val intent = Intent(this.requireContext(), MainActivity::class.java)
                                UIHelper.hideProgress(progressBar)
                                startActivity(intent)
                                this.requireActivity().finish()
                            } else {
                                UIHelper.hideProgress(progressBar)
                                UIHelper.displayAlert(this.requireContext(), "Login Failed", "Please ensure your password and email are correct.")
                                Log.i(TAG, "login failed")
                            }
                        })
                    }
                    else {
                        UIHelper.hideProgress(progressBar)
                        UIHelper.displayAlert(this.requireContext(), "Login Failed", "Please enter an email and a password.")
                        Log.i(TAG, "null email or password")
                    }
                }
                R.id.textViewForgotPassword ->{
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayoutAuth,ResetFragment())
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        commit()
                    }
                }
                R.id.textViewCreateAccount ->{
                    parentFragmentManager.beginTransaction().apply {
                        replace(R.id.frameLayoutAuth,RegisterFragment())
                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        commit()
                    }
                }
            }
        }
}


