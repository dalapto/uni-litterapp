package com.s1755183.litter.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.*
import com.s1755183.litter.fragments.adapters.CommentAdapter

class CommentsFragment : Fragment(R.layout.fragment_comments), View.OnClickListener {

    private lateinit var backButton: Button
    private lateinit var addCommmentButton: Button
    private lateinit var commentText: EditText
    private val TAG: String = "CommentsFragment"
    private lateinit var auth: FirebaseAuth
    private var comments_list : ArrayList<Comment> = ArrayList<Comment>()
    private lateinit var comments_recycler: RecyclerView
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var msg: Message

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        comments_recycler = view.findViewById(R.id.commentsView)
        auth = FirebaseAuth.getInstance()
        backButton = view.findViewById(R.id.buttonBackComment)
        backButton.setOnClickListener(this)
        addCommmentButton = view.findViewById(R.id.buttonAddComment)
        addCommmentButton.setOnClickListener(this)
        commentText = view.findViewById(R.id.editTextNewComment)
        msg = (activity as MainActivity?)!!.getMessageDetails()!!
        db.collection("messages").document(msg.title!!).collection("comments")
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    for (doc in value!!) {
                        val comment_author = doc.data["author_id"].toString()
                        val comment_time = doc.data["time"].toString()
                        val comment_text = doc.data["text"].toString()
                        Log.i(TAG,comments_list.size.toString())
                        comments_list.add(Comment(null,msg.title!!,comment_author,comment_time,comment_text))
                    }
                    val adapter = CommentAdapter(comments_list)
                    comments_recycler.adapter = adapter
                }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonBackComment -> {
                var next_fragment : Fragment
                if (msg.author_id == auth.currentUser!!.uid) {
                    next_fragment = EditMessageFragment()
                }
                else {
                    next_fragment = ViewMessageFragment()
                }
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.frameLayoutMain, next_fragment)
                    setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    addToBackStack(null)
                    commit()
                }
            }
            R.id.buttonAddComment -> {
                //take inputs and add comment & time to collection
            }
        }
    }

}