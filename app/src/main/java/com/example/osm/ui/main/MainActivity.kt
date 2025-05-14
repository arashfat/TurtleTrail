package com.example.osm.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.osm.R
import com.example.osm.databinding.ActivityMainBinding
import com.example.osm.model.Point
import com.example.osm.model.RoutingResultModel
import com.example.osm.model.Summary
import com.example.osm.ui.main.adapter.SearchPlaceAdapter
import com.example.osm.ui.main.adapter.SearchPlaceModel
import com.example.osm.ui.mapUtils.getBoundingBox
import com.example.osm.ui.routing.RoutingFragment
import com.example.osm.ui.routing.RoutingViewModel
import com.example.osm.ui.searcResult.SearchResultFragment
import com.example.osm.ui.summary.SummaryFragment
import com.example.osm.utils.Constants
import com.example.osm.utils.RoutingHelper
import com.example.osm.utils.RoutingHelper.distanceLeftInStep
import com.example.osm.utils.RoutingHelper.findClosestStep
import com.example.osm.utils.RoutingHelper.formatDistance
import com.example.osm.utils.RoutingHelper.getManeuverImage
import com.example.osm.utils.dpToPx
import com.example.osm.utils.marker.CustomInfoMarker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private var mMap: MapView? = null
    private var mMyLocationOverlay: MyLocationNewOverlay? = null
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()
    private val routingViewModel: RoutingViewModel by viewModels()

    private var liveLocation: Location? = null
    private var isCentring = true

    //Routing Data
    private var isRouting = false
    private var lastIndex = 0
    private var rout: RoutingResultModel? = null
    private var activeMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        requestLocationPermission()
        setupRecyclerSearchPlaces()
    }

    override fun onResume() {
        super.onResume()
        collectors()
        setupBottomSheet()
        mMap = binding.osmMap
        mMap?.controller?.setZoom(16.0)

        mMap?.addMapListener(object : MapListener {
            override fun onScroll(event: ScrollEvent?): Boolean {
                isCentring = false
                return true
            }

            override fun onZoom(event: ZoomEvent?): Boolean {
                return true
            }
        })

        mMap?.overlays?.add(MapEventsOverlay(object : MapEventsReceiver {
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                return false
            }

            override fun longPressHelper(point: GeoPoint?): Boolean {
                activeMarker?.let {
                    it.closeInfoWindow()
                    mMap?.overlays?.remove(it)
                    mMap?.invalidate()
                    activeMarker = null
                }

                point?.let {
                    viewModel.reverseGeoPoint(
                        Point(
                            point.latitude,
                            point.longitude
                        )
                    )
                }
                return false
            }

        }))

        binding.edSearch.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(binding.mainContainer.id, SearchResultFragment(), "search").commit()
        }
    }

    private fun collectors() {
        viewModel.searchOnClick.observe(this) { search ->
            liveLocation?.let {
                val searchFragment = supportFragmentManager.findFragmentByTag("search")
                searchFragment?.let {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }

                val waypoints = ArrayList<GeoPoint>()
                waypoints.add(GeoPoint(it.latitude, it.longitude))
                waypoints.add(GeoPoint(search.latitude!!, search.longitude!!))

                viewModel.routing(
                    arrayListOf(it.longitude, it.latitude),
                    arrayListOf(search.longitude, search.latitude)
                )
            }
        }

        viewModel.route.observe(this) {
            isCentring = false
            rout = it
            it.features.getOrNull(0)?.geometry?.coordinates?.let { points ->
                val geoPoints = arrayListOf<GeoPoint>()
                for (point in points) {
                    geoPoints.add(GeoPoint(point[1], point[0]))
                }
                geoPoints.lastOrNull()?.let { destination ->
                    val marker = Marker(mMap)
                    marker.position = destination
                    mMap?.overlays?.add(marker)
                }

                val box = getBoundingBox(geoPoints.first(), geoPoints.last())

                val road = Road(geoPoints)
                val roadOverlay = RoadManager.buildRoadOverlay(road, Color.BLUE, 10.0f)
                mMap?.overlays?.add(roadOverlay)
                mMap?.zoomToBoundingBox(box, true, 100)
                showSummary(it.features.getOrNull(0)?.properties?.summary)

                it.features.getOrNull(0)?.properties?.segments?.getOrNull(0)?.steps?.let { steps ->
                    for (step in steps) {
                        val marker = Marker(mMap)
                        it.features[0].geometry.coordinates.getOrNull(step.wayPoints[1].toInt())
                            ?.let { position ->
                                marker.position = GeoPoint(position.get(1), position.get(0))
                                mMap?.overlays?.add(marker)
                            }
                    }
                }
            }
        }

        viewModel.sdkRoute.observe(this) {
            mMap?.overlays?.add(RoadManager.buildRoadOverlay(it))
            mMap?.zoomToBoundingBox(it?.mBoundingBox, true, 100)
        }

        viewModel.locationUpdate.observe(this) { location ->
            liveLocation = location
            if (isCentring) {
                mMap?.controller?.animateTo(
                    GeoPoint(location.latitude, location.longitude)
                )
            }

            if (isRouting) {
                mMap?.mapOrientation = -location.bearing
                mMap?.controller?.animateTo(
                    GeoPoint(location.latitude, location.longitude), 18.0, 1000, -location.bearing
                )

                binding.tvSpeed.visibility = View.VISIBLE
                binding.tvSpeed.text = getString(R.string.speed, location.speed.toInt())

                rout?.features?.firstOrNull()?.let { data ->

                    val (stepIndex, index) = findClosestStep(
                        location, data.geometry.coordinates,
                        data.properties.segments[0].steps, lastIndex
                    )
                    val allSteps = data.properties.segments.getOrNull(0)?.steps ?: arrayListOf()

                    if (allSteps.isNotEmpty()) {
                        val step = allSteps[stepIndex]

                        lastIndex = index

                        if (!step.isShown) {

                            //check if step is passed update routing state
                            if (stepIndex > 0) {
                                routingViewModel.updateRoutingState(
                                    allSteps[stepIndex - 1]
                                )
                            }

                            data.properties.segments.getOrNull(0)?.steps?.find { it == step }?.isShown =
                                true
                            binding.tvStep.text = step.instruction
                            step.type.let {
                                binding.ivStepIcon.setImageResource(getManeuverImage(it.toInt()))
                            }
                        }

                        val remaining =
                            distanceLeftInStep(data.geometry.coordinates, step, lastIndex)
                        binding.tvDistance.text = formatDistance(remaining)
                    }
                }
            }

            mMyLocationOverlay =
                MyLocationNewOverlay(GpsMyLocationProvider(this@MainActivity), mMap)
            mMyLocationOverlay?.enableMyLocation()
            mMap?.overlays?.add(mMyLocationOverlay)
        }

        viewModel.reverseGeoResult.observe(this) {
            mMap?.let { mMap ->
                val marker = Marker(mMap).apply {
                    position = GeoPoint(it.lat, it.lon)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    subDescription = it.name
                    infoWindow = CustomInfoMarker(mMap) {

                    }
                }
                mMap.overlays.add(marker)
                marker.showInfoWindow()
                activeMarker = marker
            }
        }
    }

    private fun showSummary(summary: Summary?) {
        summary?.let {
            val summaryFragment = SummaryFragment()
            summaryFragment.setData(summary) {
                mMap?.scaleX = 2f
                isRouting = true
                binding.edSearch.visibility = View.GONE
                binding.layoutRouting.visibility = View.VISIBLE

                rout?.let {
                    routingViewModel.setData(it)
                    supportFragmentManager.beginTransaction().replace(
                        R.id.bottom_sheet_container, RoutingFragment()
                    ).commit()
                }
            }
            summaryFragment.show(supportFragmentManager, "summary")
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                Constants.LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            viewModel.requestLocationUpdate()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.requestLocationUpdate()
            }
        }
    }

    private fun setupBottomSheet() {
        val behavior = BottomSheetBehavior.from(binding.layoutBottomSheetBehavior)
        behavior.setPeekHeight(this.dpToPx(200), false)
    }

    private fun setupRecyclerSearchPlaces() {
        binding.rcSearchFilters.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

        val adapter = SearchPlaceAdapter {
            mMap?.let { map ->
                viewModel.searchPlaces(it, RoutingHelper.createBondingBox(map))
            }
        }

        adapter.updateAdapter(
            arrayListOf(
                SearchPlaceModel("Fuel", R.drawable.ic_fuel, "fuel station"),
                SearchPlaceModel("Restaurant", R.drawable.ic_restaurant, "restaurant"),
                SearchPlaceModel("Park", R.drawable.ic_park, "park")
            )
        )
        binding.rcSearchFilters.adapter = adapter
    }
}