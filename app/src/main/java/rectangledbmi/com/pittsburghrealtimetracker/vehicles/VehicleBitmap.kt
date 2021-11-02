package rectangledbmi.com.pittsburghrealtimetracker.vehicles

import android.graphics.Bitmap

import com.rectanglel.patstatic.vehicles.response.Vehicle

/**
 * This is a container that contains a [Vehicle] and a [Bitmap]
 * @since 57
 */
data class VehicleBitmap(val vehicle: Vehicle, val bitmap: Bitmap)
