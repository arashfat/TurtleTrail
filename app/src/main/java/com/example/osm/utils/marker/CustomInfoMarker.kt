package com.example.osm.utils.marker

import android.widget.Button
import android.widget.TextView
import com.example.osm.R
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow

class CustomInfoMarker(mapView: MapView, private val onClick: () -> Unit) :
    MarkerInfoWindow(R.layout.marker_info_window, mapView) {
    override fun onOpen(item: Any?) {
        val marker = item as? Marker ?: return

        val addressText = mView.findViewById<TextView>(R.id.address_text)
        val routeButton = mView.findViewById<Button>(R.id.route_button)

        addressText.text = marker.subDescription

        routeButton.setOnClickListener {
            onClick.invoke()
        }
    }
}