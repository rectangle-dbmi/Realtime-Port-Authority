package com.rectanglel.patstatic.model

import com.rectanglel.patstatic.patterns.response.Pt
import com.rectanglel.patstatic.patterns.response.Ptr
import com.rectanglel.patstatic.predictions.response.Prd
import com.rectanglel.patstatic.routes.BusRoute
import com.rectanglel.patstatic.vehicles.response.VehicleResponse

import io.reactivex.Flowable
import io.reactivex.Single

/**
 *
 * Service interface to get data from the Port Authority Trueime API and process it to be useable.
 *
 * Created by epicstar on 9/18/16.
 * @author Jeremy Jao
 * @since 78
 */
interface PatApiService {

    val routes: Single<List<BusRoute>>
    fun getVehicles(routes: Collection<String>): Flowable<VehicleResponse>

    fun getVehiclePredictions(vid: Int): Single<List<Prd>>

    fun getStopPredictions(stpid: Int, rts: Collection<String>): Single<List<Prd>>

    fun getPatterns(route: BusRoute): Flowable<List<Ptr>>

    fun getStops(rt: String): Flowable<List<Pt>>
}
