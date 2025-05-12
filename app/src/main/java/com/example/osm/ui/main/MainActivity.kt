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
import com.example.osm.R
import com.example.osm.databinding.ActivityMainBinding
import com.example.osm.model.RoutingResultModel
import com.example.osm.model.Summary
import com.example.osm.ui.mapUtils.getBoundingBox
import com.example.osm.ui.routing.RoutingFragment
import com.example.osm.ui.routing.RoutingViewModel
import com.example.osm.ui.searcResult.SearchResultFragment
import com.example.osm.ui.summary.SummaryFragment
import com.example.osm.utils.RoutingHelper.distanceLeftInStep
import com.example.osm.utils.RoutingHelper.findClosestStep
import com.example.osm.utils.RoutingHelper.formatDistance
import com.example.osm.utils.RoutingHelper.getManeuverImage
import com.example.osm.utils.dpToPx
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.concurrent.formatDuration
import org.osmdroid.api.IMapController
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
    private var controller: IMapController? = null
    private var mMyLocationOverlay: MyLocationNewOverlay? = null
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()
    private val routingViewModel: RoutingViewModel by viewModels()

    private val searchResultFragment = SearchResultFragment()
    private var liveLocation: Location? = null
    private var isCentring = true

    //Routing Data
    private var isRouting = false
    private var lastIndex = 0
    private var rout: RoutingResultModel? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        requestLocationPermission()
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

            override fun longPressHelper(p: GeoPoint?): Boolean {
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

//                viewModel.sdkRouting(waypoints, this)
                viewModel.routing(
                    arrayListOf(it.longitude, it.latitude),
                    arrayListOf(search.longitude!!, search.latitude!!)
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
                location.speed?.let {
                    binding.tvSpeed.visibility = View.VISIBLE
                    binding.tvSpeed.text = getString(R.string.speed, it.toInt())
                }
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
                        step?.let {
                            val remaining =
                                distanceLeftInStep(data.geometry.coordinates, step, lastIndex)
                            binding.tvDistance.text = formatDistance(remaining)
                        }
                    }
                }
            }

            mMyLocationOverlay =
                MyLocationNewOverlay(GpsMyLocationProvider(this@MainActivity), mMap)
            mMyLocationOverlay?.enableMyLocation()
            mMap?.overlays?.add(mMyLocationOverlay)
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
                LOCATION_PERMISSION_REQUEST_CODE
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

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.requestLocationUpdate()
            }
        }
    }

    private fun setupBottomSheet() {
        val behavior = BottomSheetBehavior.from(binding.bottomSheetContainer)
        behavior.setPeekHeight(this.dpToPx(115), false)
    }
}