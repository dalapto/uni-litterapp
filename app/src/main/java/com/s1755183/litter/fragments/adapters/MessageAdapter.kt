package com.s1755183.litter.fragments.adapters

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.s1755183.litter.Message
import com.s1755183.litter.R
import com.s1755183.litter.currentUser

class MessageAdapter (private val messages: ArrayList<Message>, private val messages_states: HashMap<String, Int>, private val mListener : MessageAdapter.RecyclerViewActionListener ): RecyclerView.Adapter<MessageHolder>() {
        override fun getItemCount() = messages.size

        interface RecyclerViewActionListener {
            fun onViewClicked(clickedViewId: Int, clickedItemPosition: Int, title: String)
        }


        override fun onBindViewHolder(holder: MessageHolder, position: Int) {
            val item = messages[position]
            holder.time.text = item.time.toString()
            holder.author.text = item.author_id.toString()
            holder.comments.text = "Comments: "+item.comments.toString()
            holder.keeps.text = "Keeps: "+item.keeps.toString()
            holder.views.text = "Views: "+item.views.toString()
            holder.title.text = item.title.toString()

            Log.i("MessageAdapter",messages_states.size.toString())
            when (messages_states[item.title]) {
                4 -> {
                    Log.i("MessageAdapter","BIND OWN")
                    holder.card.setCardBackgroundColor(Color.parseColor("#E91E88E5")) //blue
                }
                3 -> {
                    Log.i("MessageAdapter","BIND KEPT")
                    holder.card.setCardBackgroundColor(Color.parseColor("#FFFB8C00")) //orange
                }
                2 -> {
                    Log.i("MessageAdapter","BIND SEEN")
                    holder.card.setCardBackgroundColor(Color.parseColor("#49A84B")) //green
                }
                1 -> {
                    Log.i("MessageAdapter","BIND PART")
                    holder.card.setCardBackgroundColor(Color.parseColor("#49A84B")) //green
                    holder.title.text = "?".repeat(item.title.toString().length)
                }
                else -> {
                    Log.i("MessageAdapter","BIND UNSEEN")
                    holder.card.setCardBackgroundColor(Color.parseColor("#49A84B")) //green
                    holder.title.text = "?".repeat(item.title.toString().length/2)+item.title.toString().drop(item.title.toString().length/2)
                }
            }
            holder.card.setOnClickListener{view ->
                if (holder.map.visibility == View.GONE) {
                    holder.map.visibility = View.VISIBLE
                }
                else {
                    holder.map.visibility = View.GONE
                }
            }
//            holder.map.setOnClickListener{view -> mListener.onViewClicked(view.id, holder.adapterPosition, messages[position].title.toString())}
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.message_item, parent, false)
        return MessageHolder(view)
    }

    }