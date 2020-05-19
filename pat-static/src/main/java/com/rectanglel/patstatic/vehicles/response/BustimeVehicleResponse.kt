package com.rectanglel.patstatic.vehicles.response

import com.google.gson.annotations.Expose

import java.util.ArrayList
import java.util.HashMap

import com.rectanglel.patstatic.errors.response.Error

import javax.annotation.Generated

/**
 * bus time response for the the vehicles
 *
 * @author Jeremy Jao
 * @since 46
 */
@Generated("org.jsonschema2pojo")
class BustimeVehicleResponse {

    @Expose
    var vehicle: List<Vehicle> = ArrayList()

    @Expose
    var error: List<Error> = ArrayList()

    val processedErrors: HashMap<String, ArrayList<String>>
        get() = processErrors()

    /**
     * Processes the errors into a hashmap since like messages can be transient
     * @return the hashmap of processed errors
     * @since 55
     */
    private fun processErrors(): HashMap<String, ArrayList<String>> {
        val processedErrors = HashMap<String, ArrayList<String>>(error.size)
        for (err in error) {
            var listOfParams: ArrayList<String>? = processedErrors[err.msg]
            if (listOfParams == null) {
                listOfParams = ArrayList()
                processedErrors[err.msg] = listOfParams
            }
            listOfParams.add(err.rt)
        }
        return processedErrors
    }
}