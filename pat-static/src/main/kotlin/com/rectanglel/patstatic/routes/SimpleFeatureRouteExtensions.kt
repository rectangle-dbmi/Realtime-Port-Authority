package com.rectanglel.patstatic.routes

import org.opengis.feature.simple.SimpleFeature
import java.net.URL

private val routeNumberKey = "ROUTE"
/**
 * @return the route number
 */
fun SimpleFeature?.getRouteNumber() : String {
    return this?.getAttribute(com.rectanglel.patstatic.routes.routeNumberKey) as String
}

private val routeNameKey = "Route_Name"
/**
 * @return the route name
 */
fun SimpleFeature?.getRouteName() : String? {
    return this?.getAttribute(com.rectanglel.patstatic.routes.routeNameKey) as String
}

private val routeTypeKey = "Mode"
/**
 * @return the route type
 */
fun SimpleFeature?.getRouteType() : String? {
    return this?.getAttribute(com.rectanglel.patstatic.routes.routeTypeKey) as String
}

private val weekendServiceKey = "WeekendSer"
fun SimpleFeature?.hasWeekendService() : Boolean? {
    return this?.getAttribute(weekendServiceKey) != "None"
}


private val averageWeeklyRidersKey = "AvgWkdy_FY"
/**
 * @return the percentage that the route is on time
 */
fun SimpleFeature?.getAverageWeeklyRiders() : Double? {
    return this?.getAttribute(averageWeeklyRidersKey) as Double?
}


private val timeTableKey = "TimeTable_"
fun SimpleFeature?.getTimeTablePdf() : URL? {
    return URL(this?.getAttribute(timeTableKey) as String?)
}
