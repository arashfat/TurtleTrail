package com.example.osm.ui.main

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.osm.model.Point
import com.example.osm.model.ReverseGeoResponse
import com.example.osm.model.RoutingResultModel
import com.example.osm.model.SearchResultModel
import com.example.osm.network.NetworkResult
import com.example.osm.repository.LocationRepository
import com.example.osm.repository.RoutingRepository
import com.example.osm.repository.SearchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.bonuspack.routing.Road
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

    private val _reverseGeoResult = MutableLiveData<ReverseGeoResponse>()
    val reverseGeoResult: LiveData<ReverseGeoResponse> = _reverseGeoResult

    private val _sdkRoute = MutableLiveData<Road>()
    val sdkRoute: LiveData<Road> = _sdkRoute

    private val _locationUpdate = MutableLiveData<Location>()
    val locationUpdate: LiveData<Location> get() = _locationUpdate

    val searchOnClick = MutableLiveData<SearchResultModel>()
    fun searchPlaces(search: String, boundingBox: List<Double>? = null) = viewModelScope.launch {
        val result = searchRepository.getPlaces(search, boundingBox)
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

    fun reverseGeoPoint(point: Point) = viewModelScope.launch {
        searchRepository.reverseGeoCoding(point).collectLatest {
            if (it is NetworkResult.Success) {
                it.data?.let { address -> _reverseGeoResult.value = address }
            }
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