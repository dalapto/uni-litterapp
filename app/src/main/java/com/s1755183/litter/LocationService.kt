package com.s1755183.litter

import android.app.Service
import android.content.Intent
import android.os.IBinder

class LocationService: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //initialise()
        return super.onStartCommand(intent, flags, startId)
    }

}