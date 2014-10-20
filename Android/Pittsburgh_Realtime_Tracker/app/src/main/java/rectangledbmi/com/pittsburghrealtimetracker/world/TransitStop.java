package rectangledbmi.com.pittsburghrealtimetracker.world;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by epicstar on 9/5/14.
 */
public class TransitStop {

    private LatLng location;
    private int id;
    private String description;
    private String direction;
    private Marker marker;
    private int pointers;

    public TransitStop(int id) {
        location = null;
        this.id = id;
        description = "";
        direction = "";
        marker = null;
        pointers = 0;
    }

    public void setLocation(double latitude, double longitude) {
        location = new LatLng(latitude, longitude);
    }

    public LatLng getLocation() {
        return location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getDirection() {
        return direction;
    }

    public int getId() {
        return id;
    }
    public void addMarker(Marker marker) {
        if(marker != null) {
            if(this.marker == null) {
                this.marker = marker;
            }
            if(this.marker.equals(marker)) {
                marker.setVisible(true);
                ++pointers;
            }
        }
    }

    public Marker removeMarker() {
        if(marker == null) {
            return null;
        }
        if(pointers <= 0) {
            marker.setVisible(false);
        }
        else if(pointers > 0) {
            --pointers;

        }
        return marker;
    }

    public boolean setMarkerVisible() {
        if(marker != null) {
            if (pointers <= 0) {
                marker.setVisible(false);
            } else {
                marker.setVisible(true);
            }
            return isMarkerVisible();
        }
        return false;
    }

    public Marker getMarker() {
        return marker;
    }

    public boolean isMarkerVisible() {
        if(marker != null) {
            return marker.isVisible();
        }
        return false;
    }



}
