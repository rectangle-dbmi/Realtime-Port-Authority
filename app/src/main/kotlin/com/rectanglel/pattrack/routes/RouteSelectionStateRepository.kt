package com.rectanglel.pattrack.routes

interface RouteSelectionStateRepository {

    /**
     * Save route selection by route number.
     * @param selection the selection to save
     */
    fun saveSelection(selection : Set<String>)

    /**
     * Check if selected
     */
    fun isSelected(routeNumber : String) : Boolean

    /**
     * Save selected items
     */
    val selectedRoutes : Set<String>

    /**
     * Change the active state of the [routeNumber]
     * @param routeNumber the route number of the transit medium
     * @return the new active state of the [routeNumber]
     */
    fun toggleSelection(routeNumber: String) : Boolean
}