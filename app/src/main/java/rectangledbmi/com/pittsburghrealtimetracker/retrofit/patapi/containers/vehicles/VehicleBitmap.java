package rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.containers.vehicles;

import android.graphics.Bitmap;

import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Vehicle;

/**
 * This is a container that contains a {@link Vehicle} and a {@link Bitmap}
 * @since 57
 */
public class VehicleBitmap {

    private Vehicle vehicle;
    private Bitmap bitmap;

    public VehicleBitmap(Vehicle vehicle, Bitmap bitmap) {
        this.vehicle = vehicle;
        this.bitmap = bitmap;
    }

    /**
     *
     * @return the vehicle object
     */
    public Vehicle getVehicle() {
        return vehicle;
    }

    /**
     *
     * @return the bitmap object
     */
    public Bitmap getBitmap() {
        return bitmap;
    }
}
