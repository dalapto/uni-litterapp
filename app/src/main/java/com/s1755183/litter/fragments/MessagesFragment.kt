package com.s1755183.litter.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.*
import com.s1755183.litter.fragments.adapters.MessageAdapter
import com.s1755183.litter.fragments.adapters.MessageHolder
import java.util.*
import kotlin.collections.HashMap

class MessagesFragment : Fragment(R.layout.fragment_messages), MessageHolder.FragmentRecyclerViewListener {

    private lateinit var chipOwn: Chip
    private lateinit var chipKept: Chip
    private lateinit var chipSeen: Chip
    private lateinit var chipUnseen: Chip
    private val TAG: String = "MessagesFragment"
    private lateinit var auth: FirebaseAuth
    private var current_messages : ArrayList<Message> = ArrayList<Message>()
    private var all_messages : HashMap<String, Message> = HashMap<String, Message>()
    private lateinit var messages_recycler: RecyclerView
    private var messages_states : HashMap<String, Int> = HashMap<String, Int>()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var adapter : MessageAdapter
    private lateinit var frameLayoutMain: FrameLayout
    private lateinit var appBarLayout: AppBarLayout
    private lateinit var viewPager: ViewPager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        frameLayoutMain = requireActivity().findViewById(R.id.frameLayoutMain)
        viewPager = requireActivity().findViewById(R.id.viewPager)
        appBarLayout = requireActivity().findViewById(R.id.appBarLayout)
        messages_recycler = view.findViewById(R.id.messagesView)
        auth = FirebaseAuth.getInstance()
        adapter = MessageAdapter(current_messages, messages_states, this)
        messages_recycler.adapter = adapter
        chipOwn = view.findViewById(R.id.chipOwn)
        chipOwn.setOnCheckedChangeListener{v, b -> filterMessages() }
        chipKept = view.findViewById(R.id.chipKept)
        chipKept.setOnCheckedChangeListener{v, b -> filterMessages() }
        chipSeen = view.findViewById(R.id.chipSeen)
        chipSeen.setOnCheckedChangeListener{v, b -> filterMessages() }
        chipUnseen = view.findViewById(R.id.chipUnseen)
        chipUnseen.setOnCheckedChangeListener{v, b -> filterMessages() }

        db.collection("messages").orderBy("time")
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    filterMessages()
                }
    }

    fun filterMessages() {
        db.collection("messages").orderBy("time").get().addOnSuccessListener { snap ->
            if (snap != null) {
                for (doc in snap.documents) {
                    if (doc != null) {
                        val hlocation = doc.data?.get("location") as HashMap<String, Double>
                        val location2 = LatLng(hlocation["latitude"]!!, hlocation["longitude"]!!)
                        val title = doc.data?.get("title") as String
                        val author = doc.data!!["author_id"] as String
                        val image = doc.data!!["image"] as String
                        val text = doc.data!!["text"] as String
                        val time = doc.data!!["time"].toString()
                        val views = (doc.data!!["views"] as Long).toInt()
                        val keeps = (doc.data!!["keeps"] as Long).toInt()
                        val comments = (doc.data!!["comments"] as Long).toInt()
                        val anonymous = doc.data!!["anonymous"] as Boolean
                        val msg = Message(title = title, author_id = author, image = image, text = text, time = time, location = location2, keeps = keeps, views = views, anonymous = anonymous, comments = comments)
                        current_messages.clear()
                        val position = current_messages.indexOf(msg)
                        all_messages[title] = msg
                        db.collection("users").document(currentUser.id).collection("seenmessages").whereEqualTo("title", doc.data!!["title"]).get()
                                .addOnSuccessListener { documents ->
                                    if (!documents.isEmpty) {
                                        for (doc2 in documents) {
                                            if (doc2.data["kept"] as Boolean) {
                                                messages_states[title] = 3
                                                if (chipKept.isChecked) {
                                                    insertItem(msg)
                                                } else {
                                                    removeItem(msg, position)
                                                }
                                            } else {
                                                if (doc2.data["seen"] as Boolean) {
                                                    messages_states[title] = 2
                                                    if (chipSeen.isChecked) {
                                                        insertItem(msg)
                                                    } else {
                                                        removeItem(msg, position)
                                                    }
                                                } else {
                                                    messages_states[title] = 1
                                                    if (chipUnseen.isChecked) {
                                                        insertItem(msg)
                                                    } else {
                                                        removeItem(msg, position)
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        if (currentUser.id == author) {
                                            messages_states[title] = 4
                                            if (chipOwn.isChecked) {
                                                Log.i(TAG,title)
                                                insertItem(msg)
                                            } else {
                                                removeItem(msg, position)
                                            }
                                        }
                                        else {
                                            messages_states[title] = 0
                                            if (chipUnseen.isChecked) {
                                                insertItem(msg)
                                            } else {
                                                removeItem(msg, position)
                                            }
                                        }
                                    }
                                }
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }
    }

    fun insertItem(msg: Message) {
        Log.i(TAG,msg.title.toString())
        current_messages.add(msg)
        adapter.notifyItemInserted(current_messages.size)
    }

    fun removeItem(msg: Message, position: Int) {
        if (position > -1) {
            if (current_messages[position] == msg) {
                current_messages.remove(msg)
            }
        }
        messages_states.remove(msg.title.toString())
        adapter.notifyItemRemoved(position)
    }

    override fun onMarkerClicked(title: String) {
        Log.i(TAG,title)
        viewPager.visibility = View.GONE
        appBarLayout.visibility = View.GONE
        frameLayoutMain.visibility = View.VISIBLE
        if (all_messages[title]?.author_id != currentUser.id) {
            (activity as MainActivity?)!!.saveMessage(all_messages[title]!!)
            parentFragmentManager.beginTransaction().apply {
                replace(R.id.frameLayoutMain,ViewMessageFragment())
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                addToBackStack(null)
                commit()
            }
        }
        else {
            if (messages_states[title]!! > 1) {
                (activity as MainActivity?)!!.saveMessage(all_messages[title]!!)
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.frameLayoutMain,EditMessageFragment())
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    addToBackStack(null)
                    commit()
                }
            }
        }
    }
}

