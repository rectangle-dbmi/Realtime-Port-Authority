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
data class BustimeVehicleResponse(

        @Expose
        var vehicle: ArrayList<Vehicle> = ArrayList<Vehicle>(),

        @Expose
        var error: List<Error> = ArrayList()

) {
    /**
     * Lazily processes the errors into a hashmap since like messages can be transient
     * @return the hashmap of processed errors
     * @since 55
     */
    val processedErrors: HashMap<String, ArrayList<String>> by lazy {
        val processedErrors = HashMap<String, ArrayList<String>>(error.size)
        for (err in error) {
            err.msg
                    ?.let { processedErrors.getOrPut(it) { ArrayList<String>() } }
                    ?.also { list: ArrayList<String> ->
                        err.rt?.let { rt: String -> list.add(rt) }
                    }
        }
        processedErrors
    }
}