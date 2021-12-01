package com.example.maptest.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.maptest.R
import com.example.maptest.geoAPI.API
import com.example.maptest.geoDataClasses.geoDataClass
import com.example.maptest.toast
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.common.util.DistanceCalculator
import com.tomtom.online.sdk.map.MapFragment
import com.tomtom.online.sdk.map.RouteBuilder
import com.tomtom.online.sdk.map.RouteStyle
import com.tomtom.online.sdk.map.TomtomMap
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.example.maptest.databinding.FragmentMapBinding

class MapsFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    var tomtomMap: TomtomMap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    @DelicateCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as MapFragment?

        mapFragment?.getAsyncMap { map ->
            tomtomMap = map
            tomtomMap!!.isMyLocationEnabled = true

            getCoordinates(tomtomMap!!)
        }
    }

    @DelicateCoroutinesApi
    private fun getCoordinates(tomtomMap: TomtomMap) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val answer = API.api.getCoordinates().execute()
                launch(Dispatchers.Main) {
                    coordinatesDataLoaded(answer.body(), tomtomMap)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    coordinatesDataLoaded(null, tomtomMap)
                }
            }
        }
    }

    private fun coordinatesDataLoaded (
        geoDataClass: geoDataClass?,
        tomtomMap: TomtomMap
    ) {
        if (geoDataClass != null) {
            var distance = 0.0

            for (i in geoDataClass.features[0].geometry.coordinates.indices) {
                var routes = mutableListOf<LatLng>()

                for (j in geoDataClass.features[0].geometry.coordinates[i].indices) {
                    for (k in geoDataClass.features[0].geometry.coordinates[i][j].indices) {
                        var p = 0
                        while (p < geoDataClass.features[0].geometry.coordinates[i][j][k].size){
                            routes.add(
                                LatLng(
                                    geoDataClass.features[0].geometry.coordinates[i][j][k][p+1],
                                    geoDataClass.features[0].geometry.coordinates[i][j][k][p]
                                )
                            )
                            p += 2
                        }
                    }
                }

                routes = routes.distinct().toMutableList()
                tomtomMap.addRoute(
                    RouteBuilder(routes)
                        .style(RouteStyle.DEFAULT_INACTIVE_ROUTE_STYLE)
                )

                distance += DistanceCalculator.calcDistInKilometers(
                    LatLng(routes[routes.size-1].latitude, routes[routes.size-1].longitude),
                    LatLng(routes[0].latitude, routes[0].longitude))
            }

            toast("Длина маршрута = ${distance.format(2)} км")
        }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}