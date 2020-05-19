package com.rectanglel.patstatic.vehicles.response

import javax.annotation.Generated
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * First retrofit object to get vehicles
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
class VehicleResponse {

    @SerializedName("bustime-response")
    @Expose
    var bustimeResponse: BustimeVehicleResponse? = null

}