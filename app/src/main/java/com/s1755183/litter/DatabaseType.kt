package com.s1755183.litter

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore


data class User(
        val id: String = "",
        val name: String = "",
        val pickup_range: Double = 0.5,
        val my_messages: List<String>? = null,
        val kept_messages: List<String>? = null
    )

    fun stringToImage(stringImage : String) {

    }

    fun stringToList(stringList : String) : List<String> {
        return stringList.split('|')
    }

    fun getUser(db: FirebaseFirestore) {

    }


    data class Comment(
        val message_id: String? = null,
        val id: String? = null,
        val author_id: String? = null,
        val text: String? = null
    )

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