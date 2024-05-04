package com.example.nearbymosque.Class

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject

class GoogleMapsApiClient(private val context: Context) {
    private val TAG = "GoogleMapsApiClient"
    private var requestQueue: RequestQueue? = null

    fun getDirections(origin: LatLng, destination: LatLng, apiKey: String, callback: (List<List<LatLng>>?) -> Unit) {
        val url = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=${origin.latitude},${origin.longitude}" +
                "&destination=${destination.latitude},${destination.longitude}" +
                "&key=$apiKey"

        val stringRequest = StringRequest(Request.Method.GET, url,
            Response.Listener<String> { response ->
                val directions = parseDirections(response)
                callback(directions)
            },
            Response.ErrorListener { error ->
                Log.e(TAG, "Error fetching directions: $error")
                callback(null)
            })

        getRequestQueue().add(stringRequest)
    }

    private fun parseDirections(response: String): List<List<LatLng>>? {
        try {
            val jsonObject = JSONObject(response)
            val routes = jsonObject.getJSONArray("routes")
            val directions = mutableListOf<List<LatLng>>()

            for (i in 0 until routes.length()) {
                val legs = routes.getJSONObject(i).getJSONArray("legs")
                val path = mutableListOf<LatLng>()

                for (j in 0 until legs.length()) {
                    val steps = legs.getJSONObject(j).getJSONArray("steps")

                    for (k in 0 until steps.length()) {
                        val polyline = steps.getJSONObject(k).getJSONObject("polyline").getString("points")
                        path.addAll(decodePolyline(polyline))
                    }
                }
                directions.add(path)
            }
            return directions
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing directions: $e")
        }
        return null
    }

    private fun decodePolyline(polyline: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        val len = polyline.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = polyline[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = polyline[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng(lat.toDouble() / 1e5, lng.toDouble() / 1e5)
            poly.add(latLng)
        }
        return poly
    }

    private fun getRequestQueue(): RequestQueue {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(context.applicationContext)
        }
        return requestQueue!!
    }
}
