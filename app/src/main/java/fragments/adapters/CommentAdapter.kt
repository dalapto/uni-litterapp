package com.s1755183.litter.fragments.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.s1755183.litter.Comment
import com.s1755183.litter.R
import com.s1755183.litter.currentUser


class CommentAdapter(private val comments: ArrayList<Comment>, private val mListener : RecyclerViewActionListener ): RecyclerView.Adapter<CommentHolder>() {
    override fun getItemCount() = comments.size

    interface RecyclerViewActionListener {
        fun onViewClicked(clickedViewId: Int, clickedItemPosition: Int,commentID: String)
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val item = comments[position]
        holder.time.text = item.time.toString()
        holder.author.text = item.author_name.toString()
        holder.text.text = item.text.toString()
        if (item.author_name == currentUser.name) {
            holder.deletebutton.visibility = View.VISIBLE
            holder.deletebutton.setOnClickListener{view -> mListener.onViewClicked(view.id, holder.adapterPosition, comments[position].id.toString())}
        }
        else {
            holder.deletebutton.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)
        return CommentHolder(view)
    }
}