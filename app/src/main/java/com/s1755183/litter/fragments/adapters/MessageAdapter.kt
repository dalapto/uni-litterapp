package com.s1755183.litter.fragments.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.Message
import com.s1755183.litter.R

class MessageAdapter(private val messages: ArrayList<Message>, private val messages_states: HashMap<String, Int>, private val mListener: MessageHolder.FragmentRecyclerViewListener): RecyclerView.Adapter<MessageHolder>() {


    override fun getItemCount() = messages.size

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val item = messages[position]
        holder.time.text = item.time.toString()
        holder.comments.text = "Comments: "+item.comments.toString()
//        holder.keeps.text = "Keeps: "+item.keeps.toString()
        holder.views.text = "Views: "+item.views.toString()
        holder.title.text = item.title.toString()
        holder.map.isClickable = false

        if (item.anonymous) {
            holder.author.text = "Anonymous"
        }
        else {
            db.collection("users").document(item.author_id.toString()).get().addOnSuccessListener { doc ->
                if (doc != null) {
                    holder.author.text = doc.data?.get("name").toString()
                }
            }
        }
        when (messages_states[item.title]) {
            4 -> {
                holder.card.setCardBackgroundColor(Color.parseColor("#E91E88E5")) //blue
            }
            3 -> {
                holder.card.setCardBackgroundColor(Color.parseColor("#C1802E")) //orange
            }
            2 -> {
                holder.card.setCardBackgroundColor(Color.parseColor("#49A84B")) //green
            }
            1 -> {
                holder.card.setCardBackgroundColor(Color.parseColor("#49A84B")) //green
                holder.title.text = "?".repeat(item.title.toString().length / 2)+item.title.toString().drop(item.title.toString().length / 2)
            }
            else -> {
                holder.card.setCardBackgroundColor(Color.parseColor("#49A84B")) //green
                holder.title.text = "?".repeat(item.title.toString().length)
            }
        }
        holder.card.setOnClickListener{ view ->
            if (holder.map.visibility == View.GONE) {
                holder.map.visibility = View.VISIBLE
                holder.createMarker(item.location,messages_states[item.title],holder.title.text.toString())
            }
            else {
                holder.map.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item, parent, false)
        return MessageHolder(view, mListener)
    }


}