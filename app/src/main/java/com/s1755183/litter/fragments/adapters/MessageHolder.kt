package com.s1755183.litter.fragments.adapters

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.MapView
import com.s1755183.litter.R

class MessageHolder(v: View): RecyclerView.ViewHolder(v) {
    val time: TextView = v.findViewById(R.id.textViewMitemTime)
    val author: TextView = v.findViewById(R.id.textViewMitemAuthor)
    val title: TextView = v.findViewById(R.id.textViewMitemTitle)
    val comments: TextView = v.findViewById(R.id.textViewMitemComments)
    val keeps: TextView = v.findViewById(R.id.textViewMitemKeeps)
    val views: TextView = v.findViewById(R.id.textViewMitemViews)
    val map: MapView = v.findViewById(R.id.mapViewMitem)
    val card: CardView = v.findViewById(R.id.messageCardView)
}