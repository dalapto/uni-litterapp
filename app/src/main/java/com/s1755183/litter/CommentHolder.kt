package com.s1755183.litter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CommentHolder(v: View): RecyclerView.ViewHolder(v) {
    val date: TextView
    val author: TextView
    val text: TextView

    init {
        // Define click listener for the ViewHolder's View.
        date = v.findViewById(R.id.textViewCommentTime)
        author = v.findViewById(R.id.textViewCommentAuthor)
        text = v.findViewById(R.id.textViewCommentText)
    }


}
