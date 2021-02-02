package com.s1755183.litter.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.UIHelper
import com.s1755183.litter.R
import com.s1755183.litter.User


class RegisterFragment : Fragment(R.layout.fragment_register), View.OnClickListener {

    lateinit var usernameEditText : EditText
    lateinit var email2EditText: EditText
    lateinit var password2EditText: EditText
    lateinit var confirmPasswordEditText: EditText
    lateinit var registerButton : Button
    lateinit var progressBar: ProgressBar
    lateinit var backButton: Button
    private lateinit var auth: FirebaseAuth
    private val TAG: String = "RegisterFragment"
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        registerButton = view.findViewById(R.id.buttonRegister)
        registerButton.setOnClickListener(this)
        progressBar  = view.findViewById(R.id.progressBarRegister)
        usernameEditText  = view.findViewById(R.id.editTextUsername)
        email2EditText  = view.findViewById(R.id.editTextEmail2)
        password2EditText  = view.findViewById(R.id.editTextPassword2)
        confirmPasswordEditText  = view.findViewById(R.id.editTextConfirmPassword)
        backButton = requireActivity().findViewById(R.id.buttonBackAuth)
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener(this)
    }


    override fun onClick(view: View?) {
        when(view?.id){
            R.id.buttonRegister -> {
                Log.i(TAG, "register button clicked")
                val username : String = usernameEditText.text.toString()
                val email2: String = email2EditText.text.toString()
                val password2 : String = password2EditText.text.toString()
                val confirmPassword  : String = confirmPasswordEditText.text.toString()
                UIHelper.displayProgress(progressBar)
                if (email2 != "" && email2.contains('@') && email2.contains('.')) {
                    if (password2.length > 5 && password2.contains(Regex("[A-Z]")) && password2.contains(Regex("[0-9]")) && password2.contains(Regex("[a-z]"))) {
                        if (password2 == confirmPassword) {
                            auth.createUserWithEmailAndPassword(email2, password2).addOnCompleteListener(this.requireActivity(), OnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.i(TAG, "sucessful register")
                                    val user = User(auth.uid!!, username)
                                    db.collection("users").document(auth.uid!!).set(user).addOnSuccessListener { Log.i(TAG,"DocumentSnapshot added with ID: ${auth.uid!!}") }
                                    UIHelper.hideProgress(progressBar)
                                    FirebaseAuth.getInstance().signOut()
                                    Toast.makeText(this.requireContext(),"Sucessfully created new account.",Toast.LENGTH_LONG).show()
                                    parentFragmentManager.beginTransaction().apply {
                                        replace(R.id.frameLayoutAuth,LoginFragment())
                                        setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                                        addToBackStack(null)
                                        commit()
                                    }
                                } else {
                                    UIHelper.hideProgress(progressBar)
                                    Log.i(TAG, "failed register")
                                    UIHelper.displayAlert(this.requireContext(), "Registration Failed", "Please check your internet connection.")
                                }
                            })
                        } else {
                            UIHelper.hideProgress(progressBar)
                            UIHelper.displayAlert(this.requireContext(), "Registration Failed", "Passwords do not match.")
                            Log.i(TAG, "password mismatch")
                        }
                    } else {
                        UIHelper.hideProgress(progressBar)
                        UIHelper.displayAlert(this.requireContext(), "Registration Failed", "Passwords must have at least 1 uppercase letter, 1 lowercase letter, 1 number and be at least 6 characters long.")
                        Log.i(TAG, "password too short")
                    }
                } else {
                    UIHelper.hideProgress(progressBar)
                    UIHelper.displayAlert(this.requireContext(), "Registration Failed", "Please enter an email.")
                    Log.i(TAG, "no email entered")
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




