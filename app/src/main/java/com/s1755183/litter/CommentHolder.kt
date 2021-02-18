package com.s1755183.litter

import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.s1755183.litter.fragments.CommentsFragment

class CommentHolder(v: View): RecyclerView.ViewHolder(v) {
    val time: TextView
    val author: TextView
    val text: TextView
    val deletebutton: ImageButton



    init {
        time = v.findViewById(R.id.textViewCommentTime)
        author = v.findViewById(R.id.textViewCommentAuthor)
        text = v.findViewById(R.id.textViewCommentText)
        deletebutton = v.findViewById(R.id.deleteCommentButton)


    }



}
