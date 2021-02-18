package com.s1755183.litter.fragments.adapters

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.s1755183.litter.R

class CommentHolder(v: View): RecyclerView.ViewHolder(v) {
    val time: TextView = v.findViewById(R.id.textViewCommentTime)
    val author: TextView = v.findViewById(R.id.textViewCommentAuthor)
    val text: TextView = v.findViewById(R.id.textViewCommentText)
    val deletebutton: ImageButton = v.findViewById(R.id.deleteCommentButton)
}
