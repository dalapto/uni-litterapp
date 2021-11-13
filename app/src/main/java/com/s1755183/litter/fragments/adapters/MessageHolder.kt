package com.s1755183.litter.fragments.adapters

import android.graphics.Bitmap
import android.location.Location
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.ui.IconGenerator
import com.s1755183.litter.MainActivity
import com.s1755183.litter.MessageStates
import com.s1755183.litter.R
import com.s1755183.litter.fragments.EditMessageFragment
import com.s1755183.litter.fragments.MapFragment
import com.s1755183.litter.fragments.ViewMessageFragment

class MessageHolder(v: View, private val mListener: MessageHolder.FragmentRecyclerViewListener): RecyclerView.ViewHolder(v), OnMapReadyCallback {
    val time: TextView = v.findViewById(R.id.textViewMitemTime)
    val author: TextView = v.findViewById(R.id.textViewMitemAuthor)
    val title: TextView = v.findViewById(R.id.textViewMitemTitle)
    val comments: TextView = v.findViewById(R.id.textViewMitemComments)
    val keeps: TextView = v.findViewById(R.id.textViewMitemKeeps)
    val views: TextView = v.findViewById(R.id.textViewMitemViews)
    val map: MapView = v.findViewById(R.id.mapViewMitem)
    val card: CardView = v.findViewById(R.id.messageCardView)
    var mMap: GoogleMap? = null
    lateinit var title2: String
    var mstate: MessageStates = MessageStates.UNSEEN

    interface FragmentRecyclerViewListener {
        fun onMarkerClicked(title: String)
    }

    init {
        map.onCreate(null)
        map.getMapAsync(this)
        map.onResume()
    }

    override fun onMapReady(newmap: GoogleMap?) {
        mMap = newmap
        Log.i("MHOLDER", title.text.toString())
        mMap?.setOnMarkerClickListener { marker ->
            if (marker != null && (mstate != MessageStates.UNSEEN && mstate != MessageStates.PARTIAL_SEEN &&  mstate != MessageStates.FOLLOWED_PARTIAL && mstate != MessageStates.FOLLOWED_UNSEEN)) {
                Log.i("MHOLDER", title2)
                mListener.onMarkerClicked(title2)
            }
            true
        }
    }

    fun createMarker(location: LatLng, state: MessageStates?, icontitle: String) {
        val mIconGenerator = IconGenerator(this.map.context)
        if (state != null) {
            mstate = state
        }
        when (state) {
            MessageStates.OWN -> mIconGenerator.setStyle(IconGenerator.STYLE_BLUE)
            MessageStates.FOLLOWED_SEEN -> mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
            MessageStates.FOLLOWED_PARTIAL -> mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
            MessageStates.FOLLOWED_UNSEEN -> mIconGenerator.setStyle(IconGenerator.STYLE_ORANGE)
            else -> mIconGenerator.setStyle(IconGenerator.STYLE_GREEN)
        }
        val iconBitmap: Bitmap = mIconGenerator.makeIcon(icontitle)
        title2 = icontitle
        mMap?.addMarker(MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude-0.0001, location.longitude), 15.0f))
        mMap?.setMinZoomPreference(14.0f)
        mMap?.setMaxZoomPreference(14.0f)
//        mMap.setLatLngBoundsForCameraTarget(l)

    }

}