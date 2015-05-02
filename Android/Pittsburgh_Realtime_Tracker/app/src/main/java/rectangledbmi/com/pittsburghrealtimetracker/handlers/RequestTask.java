package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import rectangledbmi.com.pittsburghrealtimetracker.SelectTransit;
import rectangledbmi.com.pittsburghrealtimetracker.hidden.PortAuthorityAPI;
import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;

/**
 * This is the Asynctask that will update the bus locations...
 * <p/>
 * Hopefully I'm doing this correctly!
 * <p/>
 * REQUIRES PortAuthorityAPI class to get the Port Authority URLs
 */
public class RequestTask extends AsyncTask<Void, Void, List<Bus>> {

    /**
     * The google map fragment
     */
    private GoogleMap mMap;

    /**
     * The list of selected routes from the main activity comma delimited
     * For example, if P1 and P3 was selected... this would be P1 and P3
     */
    private Set<String> selectedBuses;

    /**
     * This is to store the bus markers! String will be the bus number (not the route number).
     */
    private ConcurrentMap<Integer, Marker> busMarkers;

    /**
     * Main Activity Context...
     */
    private SelectTransit context;


    public RequestTask(GoogleMap map, Set<String> buses, ConcurrentMap<Integer, Marker> busMarkers, Context context) {
        this.context = (SelectTransit) context;
        mMap = map;
        selectedBuses = buses;
        this.busMarkers = busMarkers;
    }

    /**
     * updates the UI in the background. Does it by feeding the URL of the API key and then running
     * this on the String of selected buses (comma delimited
     *
     * @param void1 Not used
     * @return an ArrayList of Buses
     */
    @Override
    protected List<Bus> doInBackground(Void... void1) {
        if (!context.isBusTaskRunning()) {
            List<Bus> bl = null;
            try {
                URL url = PortAuthorityAPI.getVehicles(selectedBuses.toArray(new String[selectedBuses.size()]));
                BusXMLPullParser buses = new BusXMLPullParser(url, context.getApplicationContext());
                bl = buses.createBusList();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.e("doInBackground NPE", "Bus route is not tracked or all buses on route are in garage: " + selectedBuses);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }
            return bl;
        }
        Log.e("doInBackground ERROR", "Task is already running...");
        return null;
    }

    @Override
    protected void onPostExecute(List<Bus> bl) {
        if (!context.isBusTaskRunning()) {
            context.setBusTaskRunning(true);

            Log.d("onPostExecute_task_st", "Task starting after thread ran");

            if(bl == null || bl.isEmpty()) {
                Toast.makeText(context, "Routes not found. Either there is no route information, no internet connection, or the API Call limit has been exceeded.", Toast.LENGTH_LONG).show();

            } else {

                ConcurrentMap<Integer, Marker> newBusMarkers = new ConcurrentHashMap<>(bl.size());
                LatLng latlng;

                for (Bus bus : bl) {
//                    Log.d("bus_info", bus.toString());
                    latlng = new LatLng(bus.getLat(), bus.getLon());
//                    if (busMarkers != null) {
                    updateMarker(bus, latlng, newBusMarkers);
//                    } else {
//                        addNewMarker(bus, latlng, newBusMarkers);
//                    }
                }
                removeOldBuses();
                context.setBusMarkers(newBusMarkers);
            }
            Log.d("onPostExecute task_stop", "Task has stopped running");
            context.setBusTaskRunning(false);
        }
    }

    /**
     * Updates the marker already on the map
     *
     * @param newBusMarkers the new bus list
     * @param bus           the bus being updated
     * @param latlng        the LatLng of the bus's location
     */
    private synchronized void updateMarker(Bus bus, LatLng latlng, ConcurrentMap<Integer, Marker> newBusMarkers) {
        try {
            Log.d("up_mark_enter", "entered_marker");
            Marker mark = null;
            if(busMarkers != null)
                mark = busMarkers.remove(bus.getVid());
            if (mark != null) {
                Log.d("marker_update", "updating_pointer");
                mark.setTitle(bus.getRt() + "(" + bus.getVid() + ") " + bus.getDes() + (bus.isDly() ? " - Delayed" : "" ));
                mark.setPosition(latlng);
                mark.setRotation(bus.getHdg());
                mark.setSnippet("Speed: " + bus.getSpd());
                newBusMarkers.putIfAbsent(bus.getVid(), mark);

            } else {
                addNewMarker(bus, latlng, newBusMarkers);
            }
        } catch (NullPointerException e) {
            Log.e("bus_nullpointer", "Something went wrong while updating...");
        } catch (IllegalArgumentException e) {
            Log.e("bus_illegalargument", "Somehow the marker is missing");

        }
    }

    /**
     * Add a new marker to the map
     *
     * @param bus           the bus being added
     * @param latlng        the location of the bus
     * @param newBusMarkers the new bus list
     */
    private synchronized void addNewMarker(Bus bus, LatLng latlng, ConcurrentMap<Integer, Marker> newBusMarkers) {
//        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.bus_template);
//        Paint paint = new Paint();
//        ColorFilter colorFilter = new LightingColorFilter(Color.RED, 0);
//        paint.setColorFilter(colorFilter);
//        Canvas canvas = new Canvas();
//        bitmap = canvas.drawBitmap(bitmap,);
        Log.d("marker_add", "adding_marker");
        MarkerOptions marker = new MarkerOptions()
                .position(latlng)
                .title(bus.getRt() + "(" + bus.getVid() + ") " + bus.getDes() + (bus.isDly() ? " - Delayed" : ""))
                .snippet("Speed: " + bus.getSpd())
                .draggable(false)
                .rotation(bus.getHdg())
                .icon(BitmapDescriptorFactory.fromResource(getDrawable(bus.getRt())))
                .anchor((float) 0.453125, (float) 0.25)
                .flat(true);
        try {
            Log.d("new_bus", newBusMarkers.toString());
            Log.d("new_markeroptions", marker.getPosition().toString());
            Log.d("new_markeroptions", marker.getTitle());
            Log.d("new_markeroptions", marker.getSnippet());
            Log.d("new_markeroptions", Float.toString(marker.getRotation()));
            Log.d("new_mMap", mMap.toString());
//            Marker mapMarker = mMap.addMarker(marker);
//            mapMarker.setIcon(BitmapDescriptorFactory.fromAsset(bus.getRt() + ".png"));
            newBusMarkers.put(bus.getVid(), mMap.addMarker(marker));
        } catch (NullPointerException e) {
            Log.e("null_new_bus", "mMap or marker is null");
        }
    }

    /**
     * Removes the no longer visible buses to the list.
     */
    private synchronized void removeOldBuses() {
        if (busMarkers != null) {
            for (Marker marker : busMarkers.values()) {
                marker.remove();
            }
        }
    }

    private int getDrawable(String route) {
        return context.getResources().getIdentifier("bus_" + route.toLowerCase(), "drawable", context.getPackageName());
//        return context.getResources().getDrawable(resourceId);
    }

}