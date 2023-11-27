package com.example.astha

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.astha.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        for(place in Places.places){
            println("Current Date is: ${place.date}")
            println("Current Time is: ${place.time}")
            println("Current lat is: ${place.lat}")
            println("Current lng is: ${place.lng}")

            addMarker(place.lat,place.lng,place.time)
        }

    }
    private fun addMarker(lat:Double, long:Double, title:String){
        mMap.addMarker(MarkerOptions().position(LatLng(lat,long)).title(title))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat,long)))
    }
}