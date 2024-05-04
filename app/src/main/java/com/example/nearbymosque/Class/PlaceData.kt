package com.karimov03.customsearchmap.Classs

import com.google.android.gms.maps.model.LatLng

data class PlaceData(
    val name:String,
    val location:String,
    val latLng: LatLng,
    val time:String,
    var destination:String
)
