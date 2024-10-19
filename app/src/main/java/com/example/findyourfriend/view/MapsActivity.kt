package com.example.findyourfriend.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.example.findyourfriend.viewmodel.FirestoreViewModel
import com.example.findyourfriend.R
import com.example.findyourfriend.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var firestoreViewModel: FirestoreViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestoreViewModel = ViewModelProvider(this).get(FirestoreViewModel::class.java)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnZoomIn.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomIn())
        }
        binding.btnZoomOut.setOnClickListener {
            mMap.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        firestoreViewModel.getAllUsers(this) { users ->
            for (user in users) {
                val userLocation = user.location

                // Check if the location is valid before parsing
                if (userLocation.isEmpty() || userLocation == "Don't found any location yet" || userLocation == "Location not available") {
                    // Optionally, set a default location or skip adding a marker
                    continue // Skip this user if the location is not available
                }

                // Parse the valid location
                val latLng = parseLocation(userLocation)

                // Add marker for valid user location
                val markerOptions = MarkerOptions().position(latLng).title(user.displayName)
                googleMap.addMarker(markerOptions)

                // Move camera to the last valid location
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15f)
                googleMap.animateCamera(cameraUpdate)
            }
        }
    }

    private fun parseLocation(location: String): LatLng {
        val latLngSplit = location.split(", ")
        val latitude = latLngSplit[0].substringAfter("Lat: ").toDoubleOrNull()
            ?: 0.0 // Provide a default value if parsing fails
        val longitude = latLngSplit[1].substringAfter("Long: ").toDoubleOrNull()
            ?: 0.0 // Provide a default value if parsing fails
        return LatLng(latitude, longitude)
    }
}