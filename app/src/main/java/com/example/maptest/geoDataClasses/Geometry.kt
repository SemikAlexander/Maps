package com.example.maptest.geoDataClasses

data class Geometry(
    val coordinates: List<List<List<List<Double>>>>,
    val type: String
)