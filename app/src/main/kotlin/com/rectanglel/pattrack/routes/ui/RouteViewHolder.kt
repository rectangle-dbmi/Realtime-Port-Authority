package com.rectanglel.pattrack.routes.ui

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import com.rectanglel.pattrack.extensions.isLightColor
import kotlinx.android.synthetic.main.bus_route_recycler_item.view.*
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route

class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var route : Route? = null
        /**
         * Set the GUI state of the route view holder
         */
        set(value) {
            val routeIcon = itemView.bus_route_icon
            routeIcon?.text = value?.route
            routeIcon?.setTextColor(if (value?.routeColor?.isLightColor() == true) {
                Color.BLACK
            } else {
                Color.WHITE
            })

            itemView.bus_route_text?.text = value?.routeInfo
            itemView.isActivated = value?.isSelected ?: false
        }
}