package com.example.fyp

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.fyp.viewmodel.PublicTransportViewModel
import com.example.fyp.viewmodel.RestaurantViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import com.example.fyp.adapter.PublicTransport
import com.example.fyp.adapter.Restaurant

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Trip_map.newInstance] factory method to
 * create an instance of this fragment.
 */
class Trip_map : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private lateinit var mMap: GoogleMap
    private var currentMarker: Marker? = null
    var userData = 0
    //    val db = FirebaseFirestore.getInstance()
    var city = "Kuala Lumpur"
    private var startLatLng: LatLng? = null
    private var endLatLng: LatLng? = null
    private lateinit var model: PublicTransportViewModel
    private lateinit var models: RestaurantViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trip_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        model = ViewModelProvider(requireActivity()).get(PublicTransportViewModel::class.java)
        models = ViewModelProvider(requireActivity()).get(RestaurantViewModel::class.java)

        val searchEditText = view.findViewById<EditText>(R.id.searchEditText)
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(searchEditText.text.toString(), "Start Location") { latLng ->
                    startLatLng = latLng // Save start location
                    // If end location is already set, calculate route
                    if (endLatLng != null) {
                        calculateRoute(startLatLng!!, endLatLng!!)
                    }
                }
                true
            } else {
                false
            }
        }

// Usage with EditText for destination
        val destination = view.findViewById<EditText>(R.id.destination)
        destination.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(destination.text.toString(), "End Location") { latLng ->
                    endLatLng = latLng // Save end location
                    // If start location is already set, calculate route
                    if (startLatLng != null) {
                        calculateRoute(startLatLng!!, endLatLng!!)
                    }
                }
                true
            } else {
                false
            }
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Trip_map.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Trip_map().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    val restaurantList = mutableListOf<String>()

    fun findRestaurantsNearby(latitude: Double, longitude: Double, onComplete: (List<Restaurant>) -> Unit) {
        val placesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=$latitude,$longitude" +
                "&radius=500" +  // Search within 500 meters of the waypoint
                "&type=restaurant" +
                "&key=AIzaSyC5yymY6tx1MPCx3Kg-9yHmuqAW-zkdyJ4"

        val client = OkHttpClient()
        val request = Request.Builder().url(placesUrl).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle error
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                var photoUrl = ""
                if (responseData != null) {
                    val jsonObject = JSONObject(responseData)
                    val results = jsonObject.getJSONArray("results")
                    val restaurantsList = mutableListOf<Restaurant>()

                    // Iterate over results and extract restaurant details
                    for (i in 0 until results.length()) {
                        val restaurant = results.getJSONObject(i)
                        val name = restaurant.getString("name")
                        val address = restaurant.getString("vicinity")
                        val rating = if (restaurant.has("rating")) {
                            restaurant.getDouble("rating")
                        } else {
                            null // Or a default value
                        }

                        val openNow = if (restaurant.has("opening_hours")) {
                            restaurant.getJSONObject("opening_hours").getBoolean("open_now")
                        } else {
                            null // Or a default value
                        }

                        val photoReference = if (restaurant.has("photos")) {
                            restaurant.getJSONArray("photos").getJSONObject(0).getString("photo_reference")
                        } else {
                            null
                        }



                        // If there's a photo, construct its URL and fetch it
                        photoReference?.let {
                             photoUrl = "https://maps.googleapis.com/maps/api/place/photo" +
                                    "?maxwidth=400" +
                                    "&photoreference=$it" +
                                    "&key=AIzaSyC5yymY6tx1MPCx3Kg-9yHmuqAW-zkdyJ4" // Replace with your API key




                        }
                        val restaurants = Restaurant(name,address,rating,openNow,photoUrl)
                        val fetchedRestaurants = mutableListOf<Restaurant>()
                        restaurantsList.add(restaurants)

                    }

                    onComplete(restaurantsList)
                }
                println("restaurantList")
            }
        })
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val kl = LatLng(3.1319, 101.6841)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(kl, 10f))

        mMap.setOnMapLongClickListener { latLng ->
            if (startLatLng == null) {
                startLatLng = latLng
                placeMarkerAndRetrieveLocation(latLng, "Start Location")
            } else if (endLatLng == null) {
                endLatLng = latLng
                placeMarkerAndRetrieveLocation(latLng, "End Location")
                calculateRoute(startLatLng!!, endLatLng!!)
            } else {
                // Reset everything for new selection
                mMap.clear()
                startLatLng = null
                endLatLng = null
            }
        }
    }

    val transitList = mutableListOf<String>()

    private fun calculateRoute(start: LatLng, end: LatLng) {
        val mode = "transit"  // or "transit"
        val apiKey = "AIzaSyC5yymY6tx1MPCx3Kg-9yHmuqAW-zkdyJ4"
        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${start.latitude},${start.longitude}&destination=${end.latitude},${end.longitude}&mode=$mode&key=$apiKey"

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle error
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    val jsonResponse = JSONObject(it.string())
                    val routes = jsonResponse.getJSONArray("routes")
                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val legs = route.getJSONArray("legs")
                        var totalDurationValue = 0  // To accumulate the total duration
                        var totalDistanceValue = 0

                        // To accumulate transit details

                        for (i in 0 until legs.length()) {
                            val leg = legs.getJSONObject(i)
                            val steps = leg.getJSONArray("steps")
                            val distance = leg.getJSONObject("distance")
                            val duration = leg.getJSONObject("duration")
                            totalDistanceValue += distance.getInt("value")
                            totalDurationValue += duration.getInt("value")// Add distance value
                            for (j in 0 until steps.length()) {
                                val step = steps.getJSONObject(j)
                                val startLocation = step.getJSONObject("start_location")
                                val endLocation = step.getJSONObject("end_location")
                                val waypointStart = LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"))
                                val waypointEnd = LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"))
                                val waypoints = mutableListOf<LatLng>()
                                val allFetchedRestaurants = mutableListOf<Restaurant>()
                                var waypointsProcessed = 0

                                val travelMode = step.getString("travel_mode")
                                if (travelMode == "TRANSIT") {
                                    val transitDetails = step.getJSONObject("transit_details")
                                    val line = transitDetails.getJSONObject("line")
                                    val vehicle = line.getJSONObject("vehicle")
                                    val vehicleType = vehicle.getString("type")
                                    val vehicleName = vehicle.getString("name")
                                    val shortName = line.optString("short_name")


                                    val currentTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kuala_Lumpur"))
                                    val initialTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(currentTime.time)
                                    currentTime.add(Calendar.SECOND, totalDurationValue)
                                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                                    val hours = totalDurationValue / 3600
                                    val minutes = (totalDurationValue % 3600) / 60
                                    val formattedTime = String.format("%02d:%02d", hours, minutes)
                                    println("Formatted Time: $formattedTime")
                                    val eta = timeFormat.format(currentTime.time)
//                                    for (waypoint in waypoints) {
//                                        findRestaurantsNearby(waypoint.latitude, waypoint.longitude) { fetchedRestaurants ->
//                                            allFetchedRestaurants.addAll(fetchedRestaurants)
//                                            waypointsProcessed++
//                                            println(waypointsProcessed)
//                                            if (waypointsProcessed == waypoints.size) {
//                                                // All waypoints have been processed, update ViewModel
//                                                println("hi")
//                                                models.setRestaurants(allFetchedRestaurants)
//                                            }
//                                        }
//                                    }
                                    findRestaurantsNearby(waypointStart.latitude, waypointStart.longitude) { fetchedRestaurants ->
                                        // This code block will be executed after restaurants are fetched
                                        activity?.runOnUiThread {
                                            // Update ViewModel here
                                            models.setRestaurants(fetchedRestaurants)
                                        }
                                    }
                                    transitList.add("$vehicleType,$shortName,  $hours, $eta, $minutes")

                                }
                            }
                        }

                       sendDataToOtherFragment()
                        val polyline = route.getJSONObject("overview_polyline").getString("points")
                        val decodedPath = PolyUtil.decode(polyline)
                        activity?.runOnUiThread  {
                            mMap.addPolyline(PolylineOptions().addAll(decodedPath))
                        }
                    }
                }
            }
        })
    }

    private fun sendDataToOtherFragment() {
        val publicTransportList: List<PublicTransport> = transitList.mapNotNull { str ->
            val parts = str.split(',').map { it.trim() } // Trim parts to remove any leading/trailing whitespace
            if (parts.size == 5) { // Ensure exactly 4 parts are present
                try {

                    // Assuming parts[3] is a time or something that could be formatted differently,
                    // you may need additional parsing/validation
                    PublicTransport(
                        transport = parts[0],
                        ETA = parts[3],
                        transportName = parts[1],
                        estimatedTime = parts[2],
                        minute = parts[4]
                    )
                } catch (e: Exception) {
                    // Log the exception or handle the error as necessary
                    null
                }
            } else {
                // Log an error or handle cases where the data does not match the expected format
                null
            }
        }
        // Use runOnUiThread to modify the LiveData on the main thread
        activity?.runOnUiThread {
            println("sent1")
            model.setTransitDetails(publicTransportList)
        }

        val RestaurantList: List<Restaurant> = restaurantList.mapNotNull { str ->
            val parts = str.split(',').map { it.trim() } // Trim parts to remove any leading/trailing whitespace
            if (parts.size == 5) { // Ensure exactly 4 parts are present
                try {

                    // Assuming parts[3] is a time or something that could be formatted differently,
                    // you may need additional parsing/validation
                    Restaurant(
                        name = parts[0],
                        address = parts[1],
                        rating = parts[2].toDouble(),
                        photoUrl = parts[4],
                        openNow = parts[3].toBoolean()
                    )
                } catch (e: Exception) {
                    // Log the exception or handle the error as necessary
                    null
                }
            } else {
                // Log an error or handle cases where the data does not match the expected format
                null
            }
        }
        // Use runOnUiThread to modify the LiveData on the main thread
        activity?.runOnUiThread {
            println("sent")
            models.setRestaurants(RestaurantList)
        }
    }

    private fun placeMarkerAndRetrieveLocation(latLng: LatLng, title: String) {
        // Clear existing markers
//        mMap.clear()

        // Add marker on the chosen location with the given title
        currentMarker = mMap.addMarker(MarkerOptions().position(latLng).title(title))
        if (title == "End Location") {
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )
        }


        // Optionally: retrieve address from latLng
        fetchAddress(latLng)
    }

    private fun fetchAddress(latLng: LatLng) {
        val geocoder = Geocoder(requireContext())
        try {
            val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    city = addresses[0].locality ?: return
                    Toast.makeText(requireContext(), "Selected city: $city", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(requireContext(), "Address not found!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error fetching address. Please try again.", Toast.LENGTH_SHORT)
                .show()
//            Toast.makeText(this, "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}", Toast.LENGTH_SHORT).show()

        }
    }



    private fun searchLocation(location: String, title: String, onLocationFound: (LatLng) -> Unit) {
        val geocoder = Geocoder(requireContext())
        try {
            val addressList = geocoder.getFromLocationName(location, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                placeMarkerAndRetrieveLocation(latLng, title)
                onLocationFound(latLng)
            } else {
                Toast.makeText(requireContext(), "Location not found!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error searching location. Please try again.", Toast.LENGTH_SHORT)
                .show()
        }
    }

//    fun hideKeyboard(view: View) {
//        val inputMethodManager =
//            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
//    }

}


