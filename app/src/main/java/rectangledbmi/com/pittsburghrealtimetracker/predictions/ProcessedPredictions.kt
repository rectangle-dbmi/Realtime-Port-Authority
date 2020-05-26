package rectangledbmi.com.pittsburghrealtimetracker.predictions

import com.google.android.gms.maps.model.Marker
import com.rectanglel.patstatic.patterns.response.Pt
import com.rectanglel.patstatic.predictions.PredictionsType
import com.rectanglel.patstatic.predictions.PredictionsView
import com.rectanglel.patstatic.predictions.response.Prd
import com.rectanglel.patstatic.vehicles.response.Vehicle
import java.text.SimpleDateFormat
import java.util.*
import kotlin.IllegalArgumentException

/**
 *
 * Holds prediction info for the [PredictionsView].
 *
 * Created by epicstar on 10/14/16.
 * @author Jeremy Jao
 * @since 78
 */

data class ProcessedPredictions(val marker: Marker, private val predictionsType: PredictionsType, private val predictionList: List<Prd>) {
    val predictions: String by lazy {
        val dateFormatPrint = "hh:mm a"
        val dateFormat = SimpleDateFormat(dateFormatPrint, Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("America/New_York")
        val str = predictionList.joinToString(separator = "\n", transform = { prd ->
            when (predictionsType) {
                is Pt -> "${prd.rt} (${prd.vid}): ${dateFormat.format(prd.prdtm)}"
                is Vehicle -> "(${prd.stpid}) ${prd.stpnm}: ${dateFormat.format(prd.prdtm)}"
                else -> throw IllegalArgumentException() // this should never happen
            }
        })
        if (str.isNotEmpty()) str else "No predictions available."
    } // predictionsType is already in the marker but there's an IllegalStateException if not on main thread when running marker.getTag()...
}
