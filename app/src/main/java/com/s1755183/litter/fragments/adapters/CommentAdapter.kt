package com.s1755183.litter.fragments.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.s1755183.litter.Comment
import com.s1755183.litter.CommentHolder
import com.s1755183.litter.R

private val comments =  ArrayList<Comment>()

class CommentAdapter(private val comments: ArrayList<Comment>): RecyclerView.Adapter<CommentHolder>() {

    override fun getItemCount() = comments.size

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        val item = comments[position]
        holder.date.text = item.date.toString()
        holder.author.text = item.author_id.toString()
        holder.text.text = item.text.toString()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.comment_item, parent, false)
        return CommentHolder(view)
    }

}