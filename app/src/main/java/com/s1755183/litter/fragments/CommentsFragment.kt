package com.s1755183.litter.fragments

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.s1755183.litter.*
import com.s1755183.litter.fragments.adapters.CommentAdapter
import java.util.*
import kotlin.collections.ArrayList


class CommentsFragment : Fragment(R.layout.fragment_comments), View.OnClickListener, CommentAdapter.RecyclerViewActionListener {

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
        val adapter = CommentAdapter(comments_list, this)
        comments_recycler.adapter = adapter
        db.collection("messages").document(msg.title!!).collection("comments").orderBy("time")
                .addSnapshotListener { value, e ->
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    comments_list.clear()
                    for (doc in value!!) {
                        val comment_id = doc.data["id"].toString()
                        val comment_author_name = doc.data["author_name"].toString()
                        val comment_author_id = doc.data["author_id"].toString()
                        val comment_time = doc.data["time"].toString()
                        val comment_text = doc.data["text"].toString()
                        Log.i(TAG, comments_list.size.toString())
                        comments_list.add(Comment(id = comment_id, author_id = comment_author_id, author_name = comment_author_name, time = comment_time, text = comment_text))
                    }
                    adapter.notifyDataSetChanged();
                }

    }

    fun insertItem(position: Int) {
//        mExampleList.add(position, ExampleItem(R.drawable.ic_android, "New Item At Position$position", "This is Line 2"))
//        mAdapter.notifyItemInserted(position)
    }


    override fun onViewClicked(clickedViewID : Int, clickedItemPosition: Int, commentID: String) {
        when (clickedViewID) {
            R.id.deleteCommentButton -> {
                val builder = AlertDialog.Builder(this.requireContext())
                builder.setTitle("Delete Comment")
                builder.setMessage("Are you sure you want to delete this comment?")
                builder.setPositiveButton("Delete") { dialog, which ->
                    db.collection("messages").document(msg.title.toString()).collection("comments").document(commentID).delete()
                    comments_list.removeAt(clickedItemPosition)
                    comments_recycler.adapter?.notifyItemRemoved(clickedItemPosition)
                    Toast.makeText(this.requireContext(),"Sucessfully deleted comment.", Toast.LENGTH_LONG).show()
                }
                builder.setNegativeButton("Cancel") { _, _ -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            }
        }
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.buttonBackComment -> {
                var next_fragment: Fragment
                if (msg.author_id == auth.currentUser!!.uid) {
                    next_fragment = EditMessageFragment()
                } else {
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
                val comment_text = commentText.text.toString()
                if (comment_text == "") {
                    UIHelper.displayAlert(this.requireContext(), "Empty Comment", "Please enter some text to send as a comment.")
                } else {
                    val randomKey: String = UUID.randomUUID().toString()
                    val comment = Comment(id= randomKey, author_id = currentUser.id, author_name = currentUser.name, text = comment_text, time = Timestamp.now().toDate().toString())
                    db.collection("messages").document(msg.title!!).collection("comments").document(randomKey).set(comment)
                    Toast.makeText(this.requireContext(), "Sucessfully commented on message.", Toast.LENGTH_LONG).show()
                    commentText.setText("")
                }
                hideKeyboardFrom(this.requireContext())
            }
        }
    }

    fun hideKeyboardFrom(context: Context) {
        val view = getView()?.getRootView()?.getWindowToken()
        val imm: InputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view, 0)
    }

}