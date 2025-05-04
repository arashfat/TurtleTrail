package com.example.osm.ui.main

import android.content.Context
import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osm.model.RoutingResultModel
import com.example.osm.model.SearchResultModel
import com.example.osm.network.NetworkResult
import com.example.osm.repository.LocationRepository
import com.example.osm.repository.RoutingRepository
import com.example.osm.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val searchRepository: SearchRepository,
    private val routingRepository: RoutingRepository,
    private val locationRepository: LocationRepository
): ViewModel() {
    private val _places = MutableLiveData<List<SearchResultModel>>()
    val places: LiveData<List<SearchResultModel>> = _places

    private val _route = MutableLiveData<RoutingResultModel>()
    val route: LiveData<RoutingResultModel> = _route

    private val _sdkRoute = MutableLiveData<Road>()
    val sdkRoute: LiveData<Road> = _sdkRoute

    private val _locationUpdate = MutableLiveData<Location>()
    val locationUpdate: LiveData<Location> get() = _locationUpdate

    val searchOnClick = MutableLiveData<SearchResultModel>()
    fun searchPlaces(search: String) = viewModelScope.launch {
        val result = searchRepository.getPlaces(search)
         result.collect {
             if (it is NetworkResult.Success) {
                 it.data?.let { data ->
                     _places.value = data
                 }
             }
         }
    }

    fun routing(startLocation: List<Double>, endLocation: List<Double>) = viewModelScope.launch {
        val result = routingRepository.getRoute(startLocation, endLocation)
        result.collect {
            if (it is NetworkResult.Success) {
                it.data?.let { route -> _route.value = route }
            }
        }
    }

    fun sdkRouting(wayPoints: ArrayList<GeoPoint>, context: Context) = viewModelScope.launch {
        var road: Road? = null
        withContext(Dispatchers.IO) {
            val roadManager = OSRMRoadManager(context, "")
            road = roadManager.getRoad(wayPoints)
        }
         road?.let {
            _sdkRoute.value = it
        }
    }

    fun requestLocationUpdate() = viewModelScope.launch {
        locationRepository.locationUpdate().collectLatest { location ->
            _locationUpdate.value = location
        }
    }

    fun onSearchResultClick(search: SearchResultModel) {
        searchOnClick.value = search
    }
}