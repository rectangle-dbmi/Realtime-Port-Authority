package com.rectanglel.pattrack.routes

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

class SharedPrefsSelectionStateRepository(context : Context): RouteSelectionStateRepository {

    companion object {
        const val BUS_SELECT_STATE = "busesSelected"
    }

    private val sharedPreferences : SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    override val selectedRoutes : Set<String>
        get() = HashSet(sharedPreferences.getStringSet(BUS_SELECT_STATE, HashSet()))

    override fun saveSelection(selection: Set<String>) {
       sharedPreferences.edit()
               .putStringSet(BUS_SELECT_STATE, selectedRoutes)
               .apply()
    }

    override fun toggleSelection(routeNumber: String): Boolean {
        val selection = selectedRoutes as HashSet
        val isAddedToSelection = selection.add(routeNumber)
        if (!isAddedToSelection) {
            selection.remove(routeNumber)
        }
        saveSelection(selection)
        return isAddedToSelection
    }

    override fun isSelected(routeNumber: String) : Boolean =
        selectedRoutes.contains(routeNumber)
}