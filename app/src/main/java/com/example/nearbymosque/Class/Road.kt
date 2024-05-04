package com.example.nearbymosque.Class

import android.location.Location

data class Road(
    val name: String,  // Name of the road (optional)
    val location: Location, // Geographic location of the road
    val length: Double,  // Length of the road in meters (optional)
    val speedLimit: Int,  // Speed limit in kilometers per hour (optional)
    val lanes: Int  // Number of lanes on the road
)
