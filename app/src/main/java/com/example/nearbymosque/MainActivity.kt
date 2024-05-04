package com.example.nearbymosque

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.SortedList
import com.example.nearbymosque.Adapters.Adapterim
import com.example.nearbymosque.Adapters.PlaceDataAdapter
import com.example.nearbymosque.Class.GoogleMapsApiClient
import com.example.nearbymosque.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.karimov03.customsearchmap.Classs.PlaceData
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var placeClient: PlacesClient
    private var handler = android.os.Handler()
    private lateinit var runnable: Runnable
    private lateinit var mosqueLocation: LatLng
    private lateinit var map: GoogleMap
    private lateinit var sortedList: ArrayList<PlaceData>
    private val markerList = mutableListOf<Marker>()
    private lateinit var placeDataAdapter: PlaceDataAdapter
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private lateinit var myLocation: LatLng
    private lateinit var destin: LatLng
    private var chekedMosque = false
    private var polylineList = mutableListOf<Polyline>()
    private var starAndfinish = mutableListOf<Marker>()
    private var polyline: Polyline? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var userLocationMarker: Marker? = null
    private lateinit var token: AutocompleteSessionToken
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = window
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

//        navHost
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.my_navigation_host) as NavHostFragment
        val navController = navHostFragment.navController
//        Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        Places.initialize(applicationContext, getString(R.string.app_key))
        placesClient = Places.createClient(this)
        token = AutocompleteSessionToken.newInstance()

        placeClient = Places.createClient(this)

        locationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        binding.bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
//
                    binding.homePage.visibility = View.VISIBLE
                    binding.nav.visibility = View.GONE
                    true
                }

                R.id.nav_map -> {
                    binding.nav.visibility = View.GONE
                    binding.homePage.visibility = View.GONE
                    true
                }

                R.id.nav_notifications -> {
                    navController.navigate(R.id.notificationFragment)
                    binding.nav.visibility = View.VISIBLE
                    binding.homePage.visibility = View.GONE

                    true
                }

                R.id.nav_settings -> {
                    navController.navigate(R.id.settingsFragment)
                    binding.nav.visibility = View.VISIBLE
                    binding.homePage.visibility = View.GONE

                    true
                }

                else -> false
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            searchNearbyPlaces()
        }
        binding.btnGeoder.setOnClickListener {
            try {
                chekedMosque = false
                binding.btnLive.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@setOnClickListener
                }
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    myLocation = LatLng(location.latitude, location.longitude)
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ), 12f
                        )
                    )
                }
                binding.bottomNavigation.selectedItemId = R.id.nav_map
            } catch (e: Exception) {
            }
        }
        binding.btnLive.setOnClickListener {
            if (chekedMosque == false) {
                chekedMosque = true
                binding.btnLive.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))
            } else {
                chekedMosque = false
                binding.btnLive.backgroundTintList =
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))

            }
        }
        binding.enable.visibility = View.VISIBLE
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isNullOrEmpty()) {
                    placeDataAdapter =
                        PlaceDataAdapter(
                            this@MainActivity,
                            sortedList,
                            object : PlaceDataAdapter.RvAction {
                                @SuppressLint("MissingPermission")
                                override fun onClick(
                                    list: ArrayList<PlaceData>,
                                    position: Int
                                ) {
//                                  masjid tanlandi
                                    mosqueChecked(list, position)


                                }
                            })
                    binding.rv.adapter = placeDataAdapter

                } else {

                    searchMosquesByNameInUzbekistan(newText)
                }
                return true
            }
        })

    }

    fun searchMosquesByNameInUzbekistan(query: String) {
        // Define the geographical bounds for Uzbekistan (approximate coordinates)
        val uzbekistanSouthWest = LatLng(37.1722, 56.6246) // Southwest corner of Uzbekistan
        val uzbekistanNorthEast = LatLng(45.5901, 73.1489) // Northeast corner of Uzbekistan

        // Create a location bias based on the bounds of Uzbekistan
        val uzbekistanBounds =
            RectangularBounds.newInstance(uzbekistanSouthWest, uzbekistanNorthEast)

        // Create the request to find mosque predictions within Uzbekistan based on name query
        val mosqueRequest = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setTypeFilter(TypeFilter.ESTABLISHMENT) // Filter by establishments (which include mosques)
            .setQuery(query) // Search query for mosque names
            .setLocationBias(uzbekistanBounds) // Bias the search to Uzbekistan
            .setCountries("UZ") // Restrict results to Uzbekistan
            .build()

        // Perform the autocomplete search request
        placeClient.findAutocompletePredictions(mosqueRequest)
            .addOnSuccessListener { response ->
                val predictions = response.autocompletePredictions
                    .filter { prediction ->
                        val description = prediction.getPrimaryText(null).toString()
                        description.contains(
                            "masjid",
                            ignoreCase = true
                        ) || description.contains("mosque", ignoreCase = true)
                    }

                val adapter =
                    Adapterim(this@MainActivity, predictions, object : Adapterim.rvAction {
                        @SuppressLint("MissingPermission")
                        @RequiresApi(Build.VERSION_CODES.Q)
                        override fun onClick(position: Int, list: List<AutocompletePrediction>) {
                            // Handle item click
                            val placesClient = Places.createClient(this@MainActivity)
                            placesClient.fetchPlace(
                                FetchPlaceRequest.newInstance(
                                    list[position].placeId, listOf(
                                        Place.Field.LAT_LNG
                                    )
                                )
                            )
                                .addOnSuccessListener { fetchResponse ->
                                    val place = fetchResponse.place
                                    binding.bottomNavigation.selectedItemId = R.id.nav_map
                                    mosqueLocation = place.latLng
                                    chekedMosque = true
                                    binding.btnLive.backgroundTintList = ColorStateList.valueOf(
                                        ContextCompat.getColor(
                                            this@MainActivity,
                                            R.color.green
                                        )
                                    )

                                    removeAllMarkers()
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        myLocation = LatLng(location.latitude, location.longitude)


                                    }

                                    for (polyline in starAndfinish) {
                                        polyline.remove()
                                    }
                                    starAndfinish.clear()

                                    val markerOptions1 = MarkerOptions()
                                    markerOptions1.position(myLocation)
                                    val originalBitmap1 = BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.ic_start_marker
                                    )
                                    val resizedBitmap1 =
                                        Bitmap.createScaledBitmap(originalBitmap1, 200, 200, false)
                                    val icon1 = BitmapDescriptorFactory.fromBitmap(resizedBitmap1)
                                    markerOptions1.icon(icon1)
                                    val m1 = map.addMarker(markerOptions1)
                                    starAndfinish.add(m1!!)

                                    val markerOptions2 = MarkerOptions()
                                    markerOptions2.position(mosqueLocation)
                                    val originalBitmap2 = BitmapFactory.decodeResource(
                                        resources,
                                        R.drawable.ic_finish_marker
                                    )
                                    val resizedBitmap2 =
                                        Bitmap.createScaledBitmap(originalBitmap2, 200, 200, false)
                                    val icon2 = BitmapDescriptorFactory.fromBitmap(resizedBitmap2)
                                    markerOptions2.icon(icon2)
                                    val m2 = map.addMarker(markerOptions2)
                                    starAndfinish.add(m2!!)
                                    drawPolyline(myLocation, mosqueLocation)


                                }
                                .addOnFailureListener { exception ->
                                }
                        }
                    })
                binding.rv.adapter = adapter
            }
            .addOnFailureListener { exception ->
            }


    }


    override fun onMapReady(p0: GoogleMap) {
        map = p0
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        map.isMyLocationEnabled = false
        map.isTrafficEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false
        map.uiSettings.isCompassEnabled = false
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        searchNearbyPlaces()
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            myLocation = LatLng(location.latitude, location.longitude)
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        location.latitude,
                        location.longitude
                    ), 12f
                )
            )
        }
    }

    @SuppressLint("MissingPermission", "NewApi")
    private fun mosqueChecked(list: java.util.ArrayList<PlaceData>, position: Int) {
        binding.bottomNavigation.selectedItemId = R.id.nav_map
        val mosque = list[position]
        mosqueLocation = mosque.latLng
        chekedMosque = true
        binding.btnLive.backgroundTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.green))

        removeAllMarkers()
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            myLocation = LatLng(location.latitude, location.longitude)
        }
        for (polyline in starAndfinish) {
            polyline.remove()
        }
        starAndfinish.clear()
//       masjidgacha bo'lgan masofani hisoblash
        val markerOptions1 = MarkerOptions()
        markerOptions1.position(myLocation)
        val originalBitmap1 = BitmapFactory.decodeResource(resources, R.drawable.ic_start_marker)
        val resizedBitmap1 = Bitmap.createScaledBitmap(originalBitmap1, 200, 200, false)
        val icon1 = BitmapDescriptorFactory.fromBitmap(resizedBitmap1)
        markerOptions1.icon(icon1)
        val m1 = map.addMarker(markerOptions1)

        starAndfinish.add(m1!!)

        val markerOptions2 = MarkerOptions()
        markerOptions2.position(mosqueLocation)
        val originalBitmap2 = BitmapFactory.decodeResource(resources, R.drawable.ic_finish_marker)
        val resizedBitmap2 = Bitmap.createScaledBitmap(originalBitmap2, 200, 200, false)
        val icon2 = BitmapDescriptorFactory.fromBitmap(resizedBitmap2)
        markerOptions2.icon(icon2)
        val m2 = map.addMarker(markerOptions2)
        starAndfinish.add(m2!!)

        drawPolyline(myLocation, mosqueLocation)


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun drawPolyline(origin: LatLng, destination: LatLng) {
        destin = destination
        val apiClient = GoogleMapsApiClient(this)
        val apiKey = getString(R.string.app_key)
        val thickerWidth = 20f
        val normalWidth = 10f
        val threshold = 5
        apiClient.getDirections(origin, destination, apiKey) { directions ->
            directions?.let {
                for (path in it) {
                    val polylineOptions = PolylineOptions().addAll(path)
                        .color(ContextCompat.getColor(this, R.color.blue))
                        .width(if (path.size > threshold) thickerWidth else normalWidth)
                    for (polyline in polylineList) {
                        polyline.remove()
                    }
                    polylineList.clear()
                    val polyline = map.addPolyline(polylineOptions)
                    polylineList.add(polyline)
//                    line(path.first(), origin)
                }
            }


        }
    }

    private fun searchNearbyPlaces() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            requestLocationPermissions()
            return
        }

        // Get the last known location
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latitude = location.latitude
                val longitude = location.longitude

                val url =
                    "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                            "location=$latitude,$longitude" +
                            "&radius=50000" +
                            "&type=mosque" +
                            "&key=${getString(R.string.app_key)}"

                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val response =
                            OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
                        val jsonData = response.body()?.string()

                        jsonData?.let { data ->
                            var placeList = parsePlaceDetails(data)
                            placeList = filterPlaceNames(placeList)
                            runOnUiThread {
                                GlobalScope.launch(Dispatchers.IO) {
                                    for (place in placeList) {
                                        val distance = getDirectionsLength(
                                            myLocation,
                                            place.latLng,
                                            getString(R.string.app_key)
                                        )
                                        place.destination = String.format("%.2f", distance)
                                    }


                                    runOnUiThread {

                                        sortedList = placeList.sortedBy {
                                            it.destination.toFloatOrNull() ?: Float.MAX_VALUE
                                        }
                                            .toMutableList()
                                            .let { ArrayList(it) }

                                        placeDataAdapter =
                                            PlaceDataAdapter(
                                                this@MainActivity,
                                                sortedList,
                                                object : PlaceDataAdapter.RvAction {
                                                    @SuppressLint("MissingPermission")
                                                    override fun onClick(
                                                        list: ArrayList<PlaceData>,
                                                        position: Int
                                                    ) {

//                                                    masjid tanlandi
                                                        mosqueChecked(list, position)


                                                    }
                                                })
                                        binding.rv.adapter = placeDataAdapter

                                        binding.gifImageView.visibility = View.GONE
                                        binding.swipeRefreshLayout.isRefreshing = false
                                        showPlaceMarkers(placeList)
                                        binding.enable.visibility = View.GONE

                                    }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        Log.e("MainActivity", "Error fetching nearby places: ${e.message}")
                    }
                }
            }
        }
    }


    private val locationCallback = object : LocationCallback() {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if (::map.isInitialized) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setUserLocationMarker(locationResult.lastLocation!!)
                }
                myLocation = LatLng(
                    locationResult.lastLocation!!.latitude,
                    locationResult.lastLocation!!.longitude
                )
            }

            speedMeter(locationResult.lastLocation)

            if(chekedMosque){
                try {
                    drawPolyline(this@MainActivity.myLocation,this@MainActivity.destin)
                    GlobalScope.launch(Dispatchers.IO) {
                        var string = ""
                        var dis = getDirectionsLength(myLocation, destin,
                            getString(R.string.app_key)
                        )
                        string = (dis * 1000).toInt().toString() + "m"
                        if ((dis * 1000).toInt() > 1000) {
                            dis=String.format("%.2f", dis).toFloat()
                            string = dis.toString() + "km"
                        }

                        runOnUiThread {
                            binding.tvSpeedMeter.text = "Masofa: " + string
                        }
                    }

                }catch (e:Exception){}
            }
        }
    }

    private fun speedMeter(lastLocation: Location?): Int {

        var speedValue = 0
        lastLocation.let { location ->
            val speed = location!!.speed
            val speedKmph = (speed * 3.6)
            val speedKmphFormatted = String.format("%.0f", speedKmph).replace(',', '.')
            speedValue = speedKmphFormatted.toFloat().toInt()

        }


        return speedValue
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setUserLocationMarker(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        if (userLocationMarker == null) {
            //Create a new marker
            val markerOptions = MarkerOptions()
            markerOptions.position(latLng)
            val originalBitmap =
                BitmapFactory.decodeResource(resources, R.drawable.ic_navigator)
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, 200, 200, false)
            val icon = BitmapDescriptorFactory.fromBitmap(resizedBitmap)
            markerOptions.icon(icon)
            markerOptions.rotation(location.bearing)
            markerOptions.anchor(0.5f, 0.5f)
            userLocationMarker = map.addMarker(markerOptions)
        } else {
            userLocationMarker?.position = latLng
            userLocationMarker?.rotation = location.accuracy
        }
        if (chekedMosque) {
            val cameraPosition20 =
                CameraPosition.Builder().target(latLng).zoom(18f).bearing(location.bearing).build()
            val cameraPosition40 =
                CameraPosition.Builder().target(latLng).zoom(17f).bearing(location.bearing).build()
            val cameraPosition60 =
                CameraPosition.Builder().target(latLng).zoom(16.5f).bearing(location.bearing)
                    .build()
            val cameraPosition80 =
                CameraPosition.Builder().target(latLng).zoom(16f).bearing(location.bearing).build()
            val cameraPosition100 =
                CameraPosition.Builder().target(latLng).zoom(15.5f).bearing(location.bearing)
                    .build()
            val cameraPosition200 =
                CameraPosition.Builder().target(latLng).zoom(15f).bearing(location.bearing).build()
//            speed setting
            val speed = speedMeter(location)
            binding.tvSpeed.text = speed.toString()
            if (speed <= 20) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition20))
            } else if (speed <= 40) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition40))
            } else if (speed <= 60) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition60))
            } else if (speed <= 80) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition80))
            } else if (speed <= 100) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition100))
            } else {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition200))
            }

        }

    }


    private fun parsePlaceDetails(jsonData: String): ArrayList<PlaceData> {
        val placeList = ArrayList<PlaceData>()
        val jsonObject = JSONObject(jsonData)
        val resultsArray = jsonObject.getJSONArray("results")

        for (i in 0 until resultsArray.length()) {
            val placeObject = resultsArray.getJSONObject(i)
            val locationObject = placeObject.getJSONObject("geometry").getJSONObject("location")
            val lat = locationObject.getDouble("lat")
            val lng = locationObject.getDouble("lng")
            val placeId = placeObject.getString("place_id")

            // Google Places API dan qo'shimcha ma'lumotlarni olish uchun URL tayyorlash
            val apiKey = getString(R.string.app_key)
            val fields = "name,formatted_address,opening_hours" // Talab etilgan maydonlar
            val detailsUrl =
                "https://maps.googleapis.com/maps/api/place/details/json?place_id=$placeId&fields=$fields&key=$apiKey"

            try {
                val detailsResponse =
                    OkHttpClient().newCall(Request.Builder().url(detailsUrl).build()).execute()
                val detailsJsonData = detailsResponse.body()?.string()

                detailsJsonData?.let { detailsData ->
                    val placeDetails = parsePlaceDetailsResponse(detailsData)
                    val placeName = placeDetails.first
                    val formattedAddress = placeDetails.second
                    val openingHours = placeDetails.third

                    placeList.add(
                        PlaceData(
                            placeName,
                            formattedAddress,
                            LatLng(lat, lng),
                            openingHours,
                            " - km"
                        )
                    )
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "Error fetching place details: ${e.message}")
            }
        }
        return placeList
    }

    private fun parsePlaceDetailsResponse(detailsJsonData: String): Triple<String, String, String> {
        val jsonObject = JSONObject(detailsJsonData)
        val resultObject = jsonObject.getJSONObject("result")
        val placeName = resultObject.getString("name")
        val formattedAddress = resultObject.getString("formatted_address")
        val openingHours = if (resultObject.has("opening_hours")) {
            val openingHoursObject = resultObject.getJSONObject("opening_hours")
            openingHoursObject.getString("open_now")
        } else {
            ""
        }

        return Triple(placeName, formattedAddress, openingHours)
    }

    private fun filterPlaceNames(placeNames: ArrayList<PlaceData>): ArrayList<PlaceData> {
        val filteredPlaceNames = ArrayList<PlaceData>()
        val keywords = listOf(
            "masjid",
            "mosque",
            "masjidi",
            "мечеть",
            "масжид",
            "масжиди"
        )

        for (name in placeNames) {
            val lowercaseName = name.name.toLowerCase()
            if (keywords.any { lowercaseName.contains(it) }) {
                if (!keywords.contains(lowercaseName)) {
                    var nameconvert = convertToLatin(name.name)
                    var updatedName = when {
                        nameconvert.contains(
                            "mosque",
                            ignoreCase = true
                        ) -> nameconvert.replace("mosque", "masjidi", ignoreCase = true)
                            .capitalize()

                        else -> nameconvert.capitalize()
                    }
                    if (updatedName.endsWith("jid")) {
                        updatedName = updatedName.replace("jid", "jidi")
                    }
                    if (updatedName.contains("Central")) {
                        updatedName = updatedName.replace("Central", "markaziy")
                    }
                    var formattedDistance = " - km"


                    filteredPlaceNames.add(
                        PlaceData(
                            updatedName,
                            name.location,
                            name.latLng,
                            name.time,
                            formattedDistance
                        )
                    )

                }
            }
        }
        return filteredPlaceNames
    }

    private fun convertToLatin(kirilText: String): String {
        // Kirill harflari va ularning lotin tiliga o'girilishi
        val conversionMap = mapOf(
            'а' to "a",
            'б' to "b",
            'в' to "v",
            'г' to "g",
            'д' to "d",
            'е' to "e",
            'ё' to "yo",
            'ж' to "j",
            'з' to "z",
            'и' to "i",
            'й' to "y",
            'к' to "k",
            'л' to "l",
            'м' to "m",
            'н' to "n",
            'о' to "o",
            'п' to "p",
            'р' to "r",
            'с' to "s",
            'т' to "t",
            'у' to "u",
            'ф' to "f",
            'х' to "x",
            'ҳ' to "h",
            'ц' to "ts",
            'ч' to "ch",
            'ш' to "sh",
            'щ' to "shch",
            'ъ' to "",
            'ы' to "y",
            'ь' to "",
            'э' to "e",
            'ю' to "yu",
            'я' to "ya",
            "Қu" to "Qo'",
            'Қ' to "Q"
        )

        val latinText = StringBuilder()

        for (char in kirilText) {
            val lowerChar = char.toLowerCase()
            val replacement = conversionMap[lowerChar]
            if (replacement != null) {
                latinText.append(replacement)
            } else {
                latinText.append(char)
            }
        }

        return latinText.toString()
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            REQUEST_LOCATION_PERMISSION
        )
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 100
    }


    private fun showPlaceMarkers(placeList: ArrayList<PlaceData>) {
        runOnUiThread {
            for (place in placeList) {
                val marker = map.addMarker(MarkerOptions().position(place.latLng))
                markerList.add(marker!!)
            }
        }
    }

    private fun removeAllMarkers() {
        runOnUiThread {
            for (marker in markerList) {
                marker.remove()
            }
            markerList.clear()
        }
    }


    private suspend fun getDirectionsLength(
        origin: LatLng,
        destination: LatLng,
        apiKey: String
    ): Float {
        return suspendCoroutine { continuation ->
            val apiClient = GoogleMapsApiClient(this@MainActivity)
            apiClient.getDirections(origin, destination, apiKey) { directions ->
                var pathLengthKm = 0.0f
                directions?.let {
                    for (path in it) {
                        pathLengthKm += (calculatePathLength(path) / 1000.0).toFloat()
                    }
                }
                continuation.resume(pathLengthKm)
            }
        }
    }

    private fun calculatePathLength(path: List<LatLng>): Double {
        var distance = 0.0
        for (i in 0 until path.size - 1) {
            distance += calculateDistance(path[i], path[i + 1])
        }
        return distance
    }

    private fun calculateDistance(point1: LatLng, point2: LatLng): Double {
        val R = 6371000 // Yer radiusi (metrda)
        val lat1 = Math.toRadians(point1.latitude)
        val lon1 = Math.toRadians(point1.longitude)
        val lat2 = Math.toRadians(point2.latitude)
        val lon2 = Math.toRadians(point2.longitude)

        val dLat = lat2 - lat1
        val dLon = lon2 - lon1

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1) * Math.cos(lat2) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}

