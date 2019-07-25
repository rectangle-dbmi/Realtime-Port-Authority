package com.rectanglel.pattrack.routes.ui

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import rectangledbmi.com.pittsburghrealtimetracker.R
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route
import timber.log.Timber

class RouteAdapter(private val onClick : (RouteAdapter, RouteViewHolder, Route) -> Unit) : RecyclerView.Adapter<RouteViewHolder>() {

    var items : List<Route> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val row = LayoutInflater.from(parent.context)
                .inflate(R.layout.bus_route_recycler_item, parent, false)
        return RouteViewHolder(row)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        if (position > items.size) {
            Timber.w("The RouteAdapter most likely has not " +
                     "been setup yet since the position of the RouteViewHolder " +
                     "($position) is greater than the size of the item list (${items.size})")
        }
        val route = items[position]
        holder.route = route
        // TODO: add an onClickListener to the itemView
        holder.itemView.setOnClickListener { onClick(this, holder, route) }
    }

}