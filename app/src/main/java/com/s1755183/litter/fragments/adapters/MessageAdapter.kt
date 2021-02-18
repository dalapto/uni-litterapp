package com.s1755183.litter.fragments.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.s1755183.litter.Message
import com.s1755183.litter.R
import com.s1755183.litter.currentUser

class MessageAdapter (private val messages: ArrayList<Message>, private val mListener : CommentAdapter.RecyclerViewActionListener ): RecyclerView.Adapter<MessageHolder>() {
        override fun getItemCount() = messages.size

        interface RecyclerViewActionListener {
            fun onViewClicked(clickedViewId: Int, clickedItemPosition: Int,commentID: String)
        }


        override fun onBindViewHolder(holder: MessageHolder, position: Int) {
            val item = messages[position]
            holder.time.text = item.time.toString()
            holder.author.text = item.author_id.toString()
            holder.comments.text = item.comments.toString()
            holder.keeps.text = item.keeps.toString()
            holder.views.text = item.views.toString()

            if (item.author_id == currentUser.id) {
                holder.card.setCardBackgroundColor(Color.parseColor("#E91E88E5"))
            }
//            else {
//                if (message is kept by user) {
//                    holder.card.setCardBackgroundColor(Color.parseColor("#ORANGE"))
//                }
//                else {
//                    if (message is seen by user) {
//                    holder.title.text = item.text.toString()
//                    }
//                    else {
//                        if (message is partially-seen by user) {
//                            holder.title.text = item.text.toString()
//                        }
//                        else {
//                            holder.title.text = item.text.toString().size * '?'
//                        }
//                    }
//                }
//          }
            holder.card.setOnClickListener{view ->
                if (holder.map.visibility == View.GONE) {
                    holder.map.visibility = View.VISIBLE
                }
                else {
                    holder.map.visibility = View.GONE
                }
            }

            holder.map.setOnClickListener{view -> mListener.onViewClicked(view.id, holder.adapterPosition, messages[position].title.toString())}

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.comment_item, parent, false)
        return MessageHolder(view)
    }

    }