package com.s1755183.litter

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Math.pow
import kotlin.math.pow
import kotlin.math.sqrt


data class User(
        var id: String = "",
        var name: String = "",
        var pickup_range: Double = 0.5,
        var my_messages: List<String>? = null,
        var kept_messages: List<String>? = null
    )

    fun stringToImage(stringImage : String) {

    }

    fun imageToString(image : String) {

    }

    fun stringToList(stringList : String?) : List<String> {
        if (stringList != null) {
            return stringList.split('|')
        }
        else {
            return emptyList()
        }
    }

    fun ListToString(list: List<String>?): String {
        if (list != null) {
            return list.joinToString("|")
        }
        else {
            return ""
        }
    }


    var currentUser: User = User("","",0.0, emptyList(), emptyList())



data class Message(
    var title: String? = null,
    var author_id: String? = null,
    var location: LatLng = LatLng(0.0,0.0),
    var text: String? = null,
    var image: String? = null,
    var time: String = Timestamp.now().toDate().toString(),
    var keeps: Int = 0,
    var views: Int = 0,
    var anonymous: Boolean = true
)


    fun checkDistance(messageLoc :LatLng, currentLoc :LatLng, maxDistance : Double): Boolean {
        return sqrt((messageLoc.latitude - currentLoc.latitude).pow(2) + (messageLoc.longitude - currentLoc.longitude).pow(2)) <= maxDistance
    }


data class MessageState(
        var title: String,
        var seen: Boolean = false, //fully seen = true, partially seen = false, lack of entry indicates unseen/new
        var kept: Boolean = false,
        var timesviewed: Int = 0, //max is displayed in settings as "Other messages: Most Looked at"
        var timescommented: Int = 0 //max is displayed in settings as "Other messgaes: Most commented on"
)


data class Comment(
        val id: String? = null,
        val message_id: String? = null,
        val author_id: String? = null,
        val date: String? = null,
        val text: String? = null
    )
