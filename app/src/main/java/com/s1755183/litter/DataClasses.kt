package com.s1755183.litter

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import kotlin.math.*

var currentUser: User = User("","")
var displayOwn : Boolean = true
var displayFollowing : Boolean = true
var displaySeen : Boolean = true
var displayUnseen : Boolean = true
var displayRevealCircle : Boolean = true
var displaySeeCircle : Boolean = true

fun checkDistance(messageLoc :LatLng, currentLoc :LatLng, maxDistance : Double): Boolean {
    return maxDistance > haversineDistance(messageLoc,currentLoc)
}

fun haversineDistance(messageLoc :LatLng, currentLoc :LatLng): Double {
    val a = sin(((messageLoc.latitude-currentLoc.latitude) * (Math.PI/180))/2) * sin(((messageLoc.latitude-currentLoc.latitude) * (Math.PI/180))/2) +
                    cos((messageLoc.latitude)* (Math.PI/180)) * cos((currentLoc.latitude)* (Math.PI/180)) *
                    sin((messageLoc.longitude-currentLoc.longitude) * (Math.PI/180)/2) * sin((messageLoc.longitude-currentLoc.longitude) * (Math.PI/180)/2)
    return 6371 * (2 * atan2(sqrt(a), sqrt(1-a)))
}

fun locationToLngLat(location: Location): LatLng {
    return LatLng(location.latitude, location.longitude)
}

enum class MessageStates {
    OWN, FOLLOWED_SEEN, FOLLOWED_PARTIAL, FOLLOWED_UNSEEN, SEEN, PARTIAL_SEEN, UNSEEN
}


data class User(
    var id: String,
    var name: String,
    var location: LatLng = LatLng(0.0,0.0),
    var messages_made: Int = 0,
    var followed_authors: Int = 0,
    var messages_seen: Int = 0,
    var comments_made: Int = 0,
    var views_got: Int = 0,
    var followers: Int = 0,
    var comments_got: Int = 0
    )

data class Message(
    var title: String? = null,
    var author_id: String? = null,
    var location: LatLng = LatLng(0.0,0.0),
    var text: String? = null,
    var image: String? = null,
    var time: String = Timestamp.now().toDate().toString(),
//    var keeps: Int = 0,
    var views: Int = 0,
    var comments: Int = 0,
    var anonymous: Boolean = true
)

data class MessageState(
        var title: String,
        var seen: Boolean = false, //fully seen = true, partially seen = false, lack of entry indicates unseen/new
        var kept: Boolean = false,
        var timesviewed: Int = 0, //max is displayed in settings as "Other messages: Most Looked at"
        var timescommented: Int = 0 //max is displayed in settings as "Other messgaes: Most commented on"
)

data class Comment(
        val id: String? = null,
        val author_id: String? = null,
        val author_name: String? = null,
        val time: String? = null,
        val text: String? = null
    )