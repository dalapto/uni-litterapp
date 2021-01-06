package com.s1755183.litter

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.s1755183.litter.fragments.LoginFragment

class AuthenticationActivity : AppCompatActivity(), View.OnClickListener {


        private val TAG: String = "AuthenticationActivity"
        lateinit var backButton: Button

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_authentication)
            backButton = findViewById(R.id.buttonBackAuth)
            backButton.visibility = View.INVISIBLE
            backButton.setOnClickListener(this)
            supportFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayoutAuth,LoginFragment())
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                commit()
            }
        }

    override fun onClick(view: View?) {
        if (view?.id == R.id.buttonBackAuth) {
            val currentFragment : Fragment? = supportFragmentManager.findFragmentById(R.id.frameLayoutAuth)
            when(currentFragment?.id){
                R.id.registerFragment -> {

                }
                R.id.resetFragment -> {

                }
                else -> {

                }
            }
        }
    }

}



