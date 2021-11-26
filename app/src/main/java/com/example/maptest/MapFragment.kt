package com.example.maptest

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.location.Locations
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.map.MapConstants.DEFAULT_ZOOM_LEVEL
import com.tomtom.online.sdk.map.MapFragment

class MapFragment : Fragment() {

    var tomtomMap: TomtomMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?

        /*mapFragment!!.getAsyncMap { map ->
            tomtomMap = map
            tomtomMap!!.isMyLocationEnabled = true

            val orientation = MapConstants.ORIENTATION_NORTH

            tomtomMap!!.centerOn(CameraPosition.builder()
                .focusPosition(LatLng(42.845404364, 132.44898522200018))
                .zoom(DEFAULT_ZOOM_LEVEL)
                .bearing(orientation.toDouble())
                .build());
        }*/

        return inflater.inflate(R.layout.fragment_map, container, false)
    }
}