package com.rectanglel.pattrack.routes

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.rectanglel.patstatic.model.PatApiService

class RoutesViewModelFactory(private val routeSelectionStateRepository: RouteSelectionStateRepository,
                             private val patApiService: PatApiService) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>) : T {
        if (modelClass.isAssignableFrom(RoutesViewModel::class.java)) {
            return RoutesViewModel(routeSelectionStateRepository, patApiService) as T
        }
        throw IllegalArgumentException("Not recognizable class: $modelClass")
    }

}