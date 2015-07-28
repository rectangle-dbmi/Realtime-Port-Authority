package rectangledbmi.com.pittsburghrealtimetracker.world;

import android.graphics.Bitmap;

/**
 * Bus icon container that contains a route and bitmap icon
 *
 * @since 47
 * @author Jeremy Jao
 */
public class BusIconContainer {

    /**
     * The bus route
     */
    private String route;

    /**
     * The bitmap icon
     */
    private Bitmap bitmap;

    public BusIconContainer(String route, Bitmap bitmap) {
        this.route = route;
        this.bitmap = bitmap;
    }

    /**
     *
     * @return the route
     */
    public String getRoute() {
        return route;
    }

    /**
     *
     * @return the icon for the route
     */
    public Bitmap getBitmap() {
        return bitmap;
    }
}
