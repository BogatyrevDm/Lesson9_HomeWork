package com.example.lesson9_homework

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.lesson9_homework.databinding.FragmentMapsMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.IOException

const val REQUEST_CODE_MAPS = 12345

class MapsFragment : Fragment() {
    private var _binding: FragmentMapsMainBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    private val markers: ArrayList<Marker> = arrayListOf()

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val initialPlace = LatLng(55.75393030000001, 37.620795)
        googleMap.addMarker(
            MarkerOptions().position(initialPlace).title(getString(R.string.marker_start))
        )
        googleMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(initialPlace, 15f)
        )
        googleMap.setOnMapLongClickListener {
            getAddressAsync(it)
            addMarkerToArray(it)
            drawLine()
        }
        activateMyLocation(googleMap)
    }

    private fun drawLine() {
        val last: Int = markers.size - 1
        if (last >= 1) {
            val previous: LatLng = markers[last - 1].position
            val current: LatLng = markers[last].position
            map.addPolyline(PolylineOptions().add(previous, current).color(Color.RED).width(6f))
        }
    }

    private fun addMarkerToArray(latLng: LatLng) {
        val marker = setMarker(latLng, markers.size.toString(), R.drawable.ic_map_pin)
        markers.add(marker)
    }

    private fun setMarker(latLng: LatLng, searchText: String, resourceId: Int): Marker {
        return map.addMarker(
            MarkerOptions().position(latLng).title(searchText)
                .icon(BitmapDescriptorFactory.fromResource(resourceId))
        )
    }

    private fun getAddressAsync(latLng: LatLng) {

        context?.let {
            val geoCoder = Geocoder(it)
            Thread {
                try {
                    val addresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                    binding.adressTv.post {
                        binding.adressTv.text = addresses[0].getAddressLine(0)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun activateMyLocation(googleMap: GoogleMap) {
        context?.let {
            val isPermissionGranted = ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            googleMap.isMyLocationEnabled = isPermissionGranted
            googleMap.uiSettings.isMyLocationButtonEnabled = isPermissionGranted
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapsMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        initSearchByAddress()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE_MAPS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setMap()
                } else {
                    AlertDialog.Builder(requireContext()).setTitle("Доступ к геолокации")
                        .setMessage("Доступ к геолокации не предоставлен!")
                        .setNegativeButton("Закрыть") { dialog, _ ->
                            dialog.dismiss()
                        }.create().show()
                }
                return
            }
        }


    }

    private fun setMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                setMap()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                AlertDialog.Builder(requireContext()).setTitle("Доступ к геолокации")
                    .setMessage("Для корректной работы поиска по картам необходимо предоставить доступ к геолокации")
                    .setPositiveButton("Предоставить доступ") { _, _ ->
                        requestPermission()
                    }.setNegativeButton("Не надо") { dialog, _ ->
                        dialog.dismiss()
                    }.create().show()
            }
            else -> {
                requestPermission()
            }
        }
    }

    private fun requestPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_MAPS)
    }

    private fun initSearchByAddress() {
        binding.buttonSearch.setOnClickListener {
            val geocoder = Geocoder(it.context)
            val searchText = binding.searchAddress.text.toString()
            Thread {
                try {
                    val addresses = geocoder.getFromLocationName(searchText, 1)
                    if (addresses.size > 0) {
                        goToAddress(addresses, it, searchText)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }

    private fun goToAddress(addresses: MutableList<Address>, view: View, searchText: String) {
        val location = LatLng(addresses[0].latitude, addresses[0].longitude)
        view.post {
            setMarker(location, searchText, R.drawable.ic_map_marker)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }
    }

    companion object {
        fun newInstance() = MapsFragment()
    }
}