package com.s1755183.litter

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore


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

    var lastUser: User = User("nameless","idless",0.0, emptyList(), emptyList())
    lateinit var currentUser: User
    fun getUser(db: FirebaseFirestore, userid : String) {
        val docRef = db.collection("users").document(userid)
        docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        Log.d("DatabaseType", "Document data: ${document.data}")
                        lastUser.id = document.data?.get("id") as String
                        lastUser.name = document.data?.get("name") as String
                        lastUser.pickup_range = document.data?.get("pickup_range") as Double
                        lastUser.my_messages = stringToList(document.data?.get("my_messages") as String?)
                        lastUser.kept_messages = stringToList(document.data?.get("kept_messages") as String?)
                        Log.d("DatabaseType", "${lastUser.id} + ${lastUser.name}")
                    }
                    else {
                        Log.d("DatabaseType", "no user found")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("DatabaseType", "User get failed with ", exception)
                }
    }




data class Message(
    var title: String? = null,
    var author_id: String? = null,
    var location: LatLng = LatLng(0.0,0.0),
    var text: String? = null,
    var image: String? = null,
    var time: Timestamp = Timestamp.now(),
    var keeps: Int = 0,
    var views: Int = 0,
    var anonymous: Boolean = true
)


data class Comment(
        val message_id: String? = null,
        val id: String? = null,
        val author_id: String? = null,
        val text: String? = null
    )
