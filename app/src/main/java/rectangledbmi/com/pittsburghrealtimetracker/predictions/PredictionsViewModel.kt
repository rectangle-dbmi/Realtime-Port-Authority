package rectangledbmi.com.pittsburghrealtimetracker.predictions

import com.google.android.gms.maps.model.Marker

import java.util.HashSet
import java.util.concurrent.TimeUnit

import com.rectanglel.patstatic.model.PatApiService
import com.rectanglel.patstatic.patterns.response.Pt
import com.rectanglel.patstatic.predictions.PredictionsType
import com.rectanglel.patstatic.vehicles.response.Vehicle

import io.reactivex.Single
import timber.log.Timber

/**
 *
 * ViewModel for getting predictions.
 *
 * Created by epicstar on 10/13/16.
 * @author Jeremy Jao
 * @since 78
 */

class PredictionsViewModel(private val patApiService: PatApiService, private val delay: Int) {

    init {
        Timber.d("Initializing PredictionsViewModel")
    }

    /**
     * Creates a single emission assuming that a marker and selected routes are fed
     * @param marker the marker
     * @param selectedRoutes the selected routes
     * @return a single emission to get predictions
     */
    fun getPredictions(marker: Marker?, selectedRoutes: HashSet<String>?): Single<ProcessedPredictions>? {
        if (marker == null || selectedRoutes == null) {
            Timber.w("No prediction info available")
            return null
        }
        val predictionsType = marker.tag as PredictionsType?
        val id = predictionsType!!.id

        return when (predictionsType) {
            is Vehicle -> {
                Timber.d("Getting vehicle predictions")
                patApiService.getVehiclePredictions(id)
                        .map { prds -> ProcessedPredictions(marker, predictionsType, prds) }
                        .delay(delay.toLong(), TimeUnit.MILLISECONDS)
            }
            is Pt -> {
                Timber.d("Getting stop predictions")
                patApiService.getStopPredictions(id, selectedRoutes)
                        .map { prds -> ProcessedPredictions(marker, predictionsType, prds) }
                        .delay(delay.toLong(), TimeUnit.MILLISECONDS)
            }
            else -> {
                Timber.w("Not getting predictions because of unknown prediction info")
                null
            }
        }
    }
}
