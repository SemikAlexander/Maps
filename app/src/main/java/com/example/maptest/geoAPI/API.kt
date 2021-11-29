package com.example.maptest.geoAPI

import com.example.maptest.geoDataClasses.geoDataClass
import retrofit2.Call
import retrofit2.create
import retrofit2.http.GET

interface  API {
    @GET("russia.geo.json")
    fun getCoordinates():Call<geoDataClass>

    companion object {
        val api by lazy { retrofit.create<API>() }
    }
}