package com.s1755183.litter.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.*
import com.s1755183.litter.fragments.adapters.CommentAdapter
import com.s1755183.litter.fragments.adapters.MessageAdapter
import java.util.*

class MessagesFragment : Fragment(R.layout.fragment_messages), View.OnClickListener, MessageAdapter.RecyclerViewActionListener {

    private lateinit var chipOwn: Chip
    private lateinit var chipKept: Chip
    private lateinit var chipSeen: Chip
    private lateinit var chipUnseen: Chip
    private val TAG: String = "MessagesFragment"
    private lateinit var auth: FirebaseAuth
    private var messages_list : ArrayList<Message> = ArrayList<Message>()
    private lateinit var messages_recycler: RecyclerView
    private var messages_states : HashMap<String, Int> = HashMap<String, Int>()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messages_recycler = view.findViewById(R.id.messagesView)
        messages_recycler.setOnClickListener(this)
        auth = FirebaseAuth.getInstance()
        val adapter = MessageAdapter(messages_list, messages_states, this)
        messages_recycler.adapter = adapter
        chipOwn = view.findViewById(R.id.chipOwn)
        chipOwn.setOnCheckedChangeListener{v, b -> adapter.notifyDataSetChanged() }
        chipKept = view.findViewById(R.id.chipKept)
        chipKept.setOnCheckedChangeListener{v, b -> adapter.notifyDataSetChanged() }
        chipSeen = view.findViewById(R.id.chipSeen)
        chipSeen.setOnCheckedChangeListener{v, b -> adapter.notifyDataSetChanged() }
        chipUnseen = view.findViewById(R.id.chipUnseen)
        chipUnseen.setOnCheckedChangeListener{v, b -> adapter.notifyDataSetChanged() }

        db.collection("messages").orderBy("time")
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    messages_list.clear()
                    messages_states.clear()
                    for (doc in value!!) {
                        Log.i(TAG,"FOUND NEARBY MESSAGE")
                        val hlocation = doc.data["location"] as HashMap<String, Double>
                        val location2 = LatLng(hlocation["latitude"]!!, hlocation["longitude"]!!)
                        val title = doc.data["title"] as String
                        val author = doc.data["author_id"] as String
                        val image = doc.data["image"] as String
                        val text = doc.data["text"] as String
                        val time = doc.data["time"].toString()
                        val views = (doc.data["views"] as Long).toInt()
                        val keeps = (doc.data["keeps"] as Long).toInt()
                        val comments = (doc.data["comments"] as Long).toInt()
                        val anonymous = doc.data["anonymous"] as Boolean
                        if (currentUser.id == author && chipOwn.isChecked) {
                            Log.i(TAG,"OWN")
                            messages_list.add(Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous, comments = comments))
                            messages_states[title] = 4
                        }
                        else {
                            db.collection("users").document(currentUser.id).collection("seenmessages").whereEqualTo("title", doc.data["title"]).get()
                                    .addOnSuccessListener { documents ->
                                        if (!documents.isEmpty) {
                                            for (doc2 in documents) {
                                                if (chipKept.isChecked && doc2.data["kept"] as Boolean) {
                                                    Log.i(TAG,"KEPT")
                                                    messages_list.add(Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous, comments = comments))
                                                    messages_states[title] = 3
                                                } else {
                                                    if (chipSeen.isChecked && doc2.data["seen"] as Boolean) {
                                                        Log.i(TAG,"SEEN")
                                                        messages_list.add(Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous, comments = comments))
                                                        messages_states[title] = 2
                                                    } else {
                                                        if (chipUnseen.isChecked) {
                                                            Log.i(TAG,"UNSEEN")
                                                            messages_list.add(Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous, comments = comments))
                                                            messages_states[title] = 1
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            if (chipUnseen.isChecked) {
                                                Log.i(TAG,"UNSEEN")
                                                messages_list.add(Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous, comments = comments))
                                                messages_states[title] = 0
                                            }
                                        }
                                    }
                        }
                    }
                    Log.i(TAG,messages_states.size.toString())
                    Log.i(TAG,messages_list.size.toString())
                    adapter.notifyDataSetChanged()
               }
    }


    override fun onViewClicked(clickedViewID : Int, clickedItemPosition: Int, commentID: String) {
       TODO()
    }


    override fun onClick(view: View?) {
        TODO()
    }

}