package com.example.osm.ui.routing

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.osm.model.RoutingResultModel
import com.example.osm.model.Step
import javax.inject.Inject

class RoutingViewModel @Inject constructor(): ViewModel() {
    private val _updateRoutingState = MutableLiveData<Step>()
    val updateRoutingState: LiveData<Step> get() = _updateRoutingState
    var route: RoutingResultModel? = null
    fun setData(newRoute: RoutingResultModel) {
        route = newRoute
    }

    fun updateRoutingState(step: Step) {
        _updateRoutingState.value = step
    }
}