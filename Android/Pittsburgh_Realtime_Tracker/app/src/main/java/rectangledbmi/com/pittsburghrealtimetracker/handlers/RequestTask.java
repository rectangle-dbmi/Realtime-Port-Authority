package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import rectangledbmi.com.pittsburghrealtimetracker.SelectTransit;
import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;

/**
 * This is the Asynctask that will update the bus locations...
 *
 * Hopefully I'm doing this correctly!
 */
public class RequestTask extends AsyncTask<Void, Void, List<Bus>> {

    /**
     * The google map fragment
     */
    private GoogleMap mMap;
//
//    /**
//     * The list of buses from the API
//     */
//    private List<Bus> bl;

    /**
     * The list of selected routes from the main activity comma delimited
     * For example, if P1 and P3 was selected... this would be P1 and P3
     */
    private String selectedBuses;

    /**
     * This is to store the bus markers! String will be the bus number (not the route number).
     */
    private Map<Integer, Marker> busMarkers;

    /**
     * Main Activity Context...
     */
    private SelectTransit context;

    private static SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");


    public RequestTask(GoogleMap map, Set<String> buses, Map<Integer, Marker> busMarkers, Context context){
        this.context = (SelectTransit)context;
        while(this.context.isBusTaskRunning()) {}
        mMap = map;
        selectedBuses = selectBuses(buses.toArray(new String[buses.size()]));
        System.out.println(selectedBuses);
//        bl = null;
        this.busMarkers = busMarkers;
    }

    /**
     * Takes the list of buses and returns a comma delimited string of routes
     * @param buses list of buses from the main activity thread
     * @return comma delimited string of buses. ex: buses -> [P1, P3]; return "P1,P3"
     */
    private String selectBuses(String[] buses) {
        StringBuilder string = new StringBuilder();
        int oneLess = buses.length-1;
        for(int i=0;i<buses.length;++i) {
            string.append(buses[i]);
            if(i != oneLess) {
                string.append(",");
            }
        }
        return string.toString();
    }

    /**
     * updates the UI in the background. Does it by feeding the URL of the API key and then running
     * this on the String of selected buses (comma delimited
     * @param void1 Not used
     * @return an ArrayList of Buses
     */
    @Override
    protected List<Bus> doInBackground(Void... void1) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = null;
        List<Bus> bl = null;
        try {
            sp = spf.newSAXParser();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
            URL url = null;
            try {
                url = new URL(
                        "http://realtime.portauthority.org/bustime/api/v2/getvehicles?key=KiJEdJUDgRFxcG7cpt3ae6xxJ&rt=" + selectedBuses
                );
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            BusSaxHandler handler;
            try {
                handler = new BusSaxHandler();
                try {
                    if (sp != null) {
                        sp.parse(new InputSource(url != null ? url.openStream() : null), handler);
                    }

                } catch (SAXException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                bl = handler.getBusList();
            } catch (NullPointerException sax) {
//                System.out.println(sax.getMessage());

                sax.printStackTrace();
                System.err
                        .println("Bus route is not tracked or all buses on route are in garage: " + selectedBuses);
//                System.exit(0);
            }
        return bl;
    }
    //TODO make a HashMap for the bus markers and use that to update the marker location...
    protected void onProgressUpdate(Void... void1) {
    }

    @Override
    protected void onPostExecute(List<Bus> bl) {
        context.setBusTaskRunning(true);
        if(bl != null) {

            Map<Integer, Marker> newBusMarkers = new HashMap<Integer, Marker>(bl.size());
            LatLng latlng;

            if(busMarkers == null) {
                System.out.println("Bus Markers is null");
            }
            else {
                System.out.println("bus markers is not null. Replacing...");
            }

            for (Bus bus : bl) {
                latlng = new LatLng(bus.getLat(), bus.getLon());

                if(busMarkers != null) {

                    updateMarker(bus, latlng, newBusMarkers);
                }
                else {
                    addNewMarker(bus, latlng, newBusMarkers);
                }


            }
            removeOldBuses();
            context.setBusMarkers(newBusMarkers);
        }
        context.setBusTaskRunning(false);
    }

    /**
     * Updates the marker already on the map
     * @param newBusMarkers the new bus list
     * @param bus the bus being updated
     * @param latlng the LatLng of the bus's location
     */
    private void updateMarker(Bus bus, LatLng latlng, Map<Integer, Marker> newBusMarkers) {
        Marker mark = busMarkers.remove(bus.getVid());
        if (mark != null) {
            System.out.println(bus.getVid() + " updated");
            //If the mark is found, update the marker. Add it to the new map, then delete the old reference to the marker
            mark.setTitle(bus.getRt() + "(" + bus.getVid() + ") " + bus.getDes());
            mark.setPosition(latlng);
            mark.setRotation(bus.getHdg());
            mark.setSnippet("Speed: " + bus.getSpd());
            newBusMarkers.put(bus.getVid(), mark);

        }
        else {
            System.out.println(bus.getVid() + " not found...");
            addNewMarker(bus, latlng, newBusMarkers);
        }
    }

    /**
     * Add a new marker to the map
     * @param bus the bus being added
     * @param latlng the location of the bus
     * @param newBusMarkers the new bus list
     */
    private void addNewMarker(Bus bus, LatLng latlng, Map<Integer, Marker> newBusMarkers) {
        MarkerOptions marker = new MarkerOptions()
                .position(latlng)
                .title(bus.getRt() + "(" + bus.getVid() + ") " + bus.getDes())
                .snippet("Speed: " + bus.getSpd())
                .draggable(false)
                .rotation(bus.getHdg())
                .icon(BitmapDescriptorFactory.fromAsset(bus.getRt() + ".png"))
                .flat(true);
        try {
            newBusMarkers.put(bus.getVid(), mMap.addMarker(marker));
            System.out.println(bus.getVid() + " added.");
        } catch(NullPointerException e) {
            System.err.println("mMap or marker is null");
        }
    }

    /**
     * Removes the no longer visible buses to the list.
     */
    private void removeOldBuses() {
        if(busMarkers != null) {
            for(Marker marker : busMarkers.values()) {
                marker.remove();
                System.out.println("removing " + marker.getId());
            }
//            busMarkers.clear();
        }
    }

}