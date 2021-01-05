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



class MessagesFragment : Fragment(R.layout.fragment_messages) {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }
}