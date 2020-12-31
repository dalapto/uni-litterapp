package com.s1755183.litter

import com.google.android.gms.maps.model.LatLng

data class Message(
        val id: String? = null,
        val author_id: String? = null,
        val location: LatLng = LatLng(0.0,0.0),
        val title: String? = null,
        val text: String? = null,
        val image: String? = null,
        val keeps: Int = 0,
        val views: Int = 0,
        val anonymous: Boolean = true
)
