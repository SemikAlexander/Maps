package com.example.maptest.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.maptest.BuildConfig.*
import com.example.maptest.R
import com.example.maptest.R.drawable.*
import com.example.maptest.toast
import com.tomtom.online.sdk.common.location.LatLng
import com.tomtom.online.sdk.common.util.DistanceCalculator
import com.tomtom.online.sdk.map.*
import com.tomtom.online.sdk.map.Icon.Factory.*
import com.tomtom.online.sdk.routing.OnlineRoutingApi
import com.tomtom.online.sdk.routing.RoutingApi
import com.tomtom.online.sdk.routing.RoutingException
import com.tomtom.online.sdk.routing.route.*
import com.tomtom.online.sdk.routing.route.RouteCalculationDescriptor.*
import com.tomtom.online.sdk.routing.route.description.RouteType.*
import com.tomtom.online.sdk.routing.route.information.FullRoute
import com.tomtom.online.sdk.search.OnlineSearchApi
import com.tomtom.online.sdk.search.SearchApi
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchQueryBuilder
import com.tomtom.online.sdk.search.data.reversegeocoder.ReverseGeocoderSearchResponse
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

class FreeUseActivity : AppCompatActivity(), OnMapReadyCallback,
    TomtomMapCallback.OnMapLongClickListener {

    private lateinit var tomtomMap: TomtomMap
    private lateinit var searchApi: SearchApi
    private lateinit var routingApi: RoutingApi
    private var route: Route? = null
    private var departurePosition: LatLng? = null
    private var destinationPosition: LatLng? = null
    private var wayPointPosition: LatLng? = null
    private var departureIcon: Icon? = null
    private var destinationIcon: Icon? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_free_use)

        initTomTomServices()
        initUIViews()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        clearMap()
        finish()
    }

    override fun onMapReady(tomtomMap: TomtomMap) {
        this.tomtomMap = tomtomMap
        this.tomtomMap.let {
            it.isMyLocationEnabled = true
            it.addOnMapLongClickListener(this)
            it.markerSettings.setMarkersClustering(true)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        tomtomMap.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onMapLongClick(latLng: LatLng) {
        if (isDeparturePositionSet && isDestinationPositionSet) {
            clearMap()
        } else {
            handleLongClick(latLng)
        }
    }

    private fun handleLongClick(latLng: LatLng) {
        searchApi
            .reverseGeocoding(
                ReverseGeocoderSearchQueryBuilder(
                    latLng.latitude,
                    latLng.longitude
                ).build()
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSingleObserver<ReverseGeocoderSearchResponse?>() {
                override fun onSuccess(response: ReverseGeocoderSearchResponse) {
                    processResponse(response)
                }

                override fun onError(e: Throwable) {
                    handleApiError(e)
                }

                private fun processResponse(response: ReverseGeocoderSearchResponse) {
                    if (response.hasResults()) {
                        processFirstResult(response.addresses[0].position)
                    } else {
                        toast(getString(R.string.geocode_no_results))
                    }
                }

                private fun processFirstResult(geocodedPosition: LatLng) {
                    if (!isDeparturePositionSet) {
                        setAndDisplayDeparturePosition(geocodedPosition)
                    } else {
                        destinationPosition = geocodedPosition
                        tomtomMap.removeMarkers()
                        drawRoute(departurePosition, destinationPosition)

                        val distance =
                            DistanceCalculator
                                .calcDistInKilometers(destinationPosition, departurePosition)

                        toast("Длина маршрута = ${distance.format(2)} км")

                        /*В документации TomTom мной не было найдено функции для отрисовки маршрута на проложенном маршруте.*/
                    }
                }

                private fun setAndDisplayDeparturePosition(geocodedPosition: LatLng) {
                    departurePosition = geocodedPosition
                    createMarkerIfNotPresent(departurePosition, departureIcon)
                }
            })
    }

    private val isDestinationPositionSet: Boolean
        get() = destinationPosition != null

    private val isDeparturePositionSet: Boolean
        get() = departurePosition != null

    private fun handleApiError(e: Throwable) {
        toast(getString(R.string.api_response_error, e.localizedMessage))
    }

    private fun initTomTomServices() {
        val mapKeys = mapOf(
            ApiKeyType.MAPS_API_KEY to MAPS_API_KEY
        )
        val mapProperties = MapProperties.Builder()
            .keys(mapKeys)
            .build()
        val mapFragment = MapFragment.newInstance(mapProperties)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mapFragment, mapFragment)
            .commit()

        mapFragment.getAsyncMap(this)

        searchApi = OnlineSearchApi.create(this, SEARCH_API_KEY)
        routingApi = OnlineRoutingApi.create(this, ROUTING_API_KEY)
    }

    private fun initUIViews() {
        departureIcon = fromResources(this, ic_map_route_departure)
        destinationIcon = fromResources(this, ic_map_route_destination)
    }

    private fun clearMap() {
        tomtomMap.clear()
        departurePosition = null
        destinationPosition = null
        route = null
    }

    private fun createRouteCalculationDescriptor(
        routeDescriptor: RouteDescriptor,
        wayPoints: Array<LatLng>?
    ): RouteCalculationDescriptor {
        return if (wayPoints != null) Builder()
            .routeDescription(routeDescriptor)
            .waypoints(listOf(*wayPoints)).build()
        else Builder()
            .routeDescription(routeDescriptor).build()
    }

    private fun drawRoute(start: LatLng?, stop: LatLng?) {
        wayPointPosition = null
        drawRouteWithWayPoints(start, stop, null)
    }

    private fun createRouteSpecification(
        start: LatLng,
        stop: LatLng,
        wayPoints: Array<LatLng>?
    ): RouteSpecification {
        val routeDescriptor = RouteDescriptor.Builder()
            .routeType(FASTEST)
            .build()

        return RouteSpecification.Builder(start, stop)
            .routeCalculationDescriptor(createRouteCalculationDescriptor(routeDescriptor, wayPoints))
            .build()
    }

    private fun drawRouteWithWayPoints(
        start: LatLng?,
        stop: LatLng?,
        wayPoints: Array<LatLng>?
    ) {
        val routeSpecification = createRouteSpecification(start!!, stop!!, wayPoints)

        routingApi.planRoute(routeSpecification, object : RouteCallback {
            override fun onSuccess(routePlan: RoutePlan) {
                displayRoutes(routePlan.routes)
                tomtomMap.displayRoutesOverview()
            }

            override fun onError(error: RoutingException) {
                handleApiError(error)
                clearMap()
            }

            private fun displayRoutes(routes: List<FullRoute>) {
                for (fullRoute in routes) {
                    route =
                        tomtomMap.addRoute(
                            RouteBuilder(fullRoute.getCoordinates())
                                .startIcon(departureIcon)
                                .endIcon(destinationIcon)
                        )
                }
            }
        })
    }

    private fun createMarkerIfNotPresent(position: LatLng?, icon: Icon?) {
        val optionalMarker = tomtomMap.findMarkerByPosition(position)
        if (!optionalMarker.isPresent) {
            tomtomMap.addMarker(MarkerBuilder((position)!!).icon(icon))
        }
    }
}

private fun Double.format(digits: Int) = "%.${digits}f".format(this)
