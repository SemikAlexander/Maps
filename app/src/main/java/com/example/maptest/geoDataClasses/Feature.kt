package com.example.maptest.geoDataClasses

data class Feature(
    val geometry: Geometry,
    val properties: Properties,
    val type: String
)