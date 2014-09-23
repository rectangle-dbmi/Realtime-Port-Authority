package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;

public class RequestTask extends AsyncTask<Void, Void, List<Bus>> {

    /**
     * The google map fragment
     */
    GoogleMap mMap;

    /**
     * The list of buses from the API
     */
    List<Bus> bl;

    /**
     * The list of selected routes from the main activity comma delimited
     * For example, if P1 and P3 was selected... this would be P1 and P3
     */
    String selectedBuses;


    public RequestTask(GoogleMap map, List<String> buses){
        mMap = map;
        selectedBuses = selectBuses(buses);
        bl = null;
    }

    /**
     * Takes the list of buses and returns a comma delimited string of routes
     * @param buses list of buses from the main activity thread
     * @return comma delimited string of buses. ex: buses -> [P1, P3]; return "P1,P3"
     */
    private String selectBuses(List<String> buses) {
        StringBuilder string = new StringBuilder();
        int oneLess = buses.size()-1;
        for(int i=0;i<buses.size();++i) {
            string.append(buses.get(i));
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
    protected void onPostExecute(List<Bus> list) {

        if(bl != null) {
            //TODO want to update points as opposed to clearing the map, consider storing markers in hashmap
            for (Bus bus : bl) {
                LatLng latlng = new LatLng(bus.getLat(), bus.getLon());

                MarkerOptions marker = new MarkerOptions()
                        .position(latlng)
                        .title(bus.getRt() + "(" + bus.getVid() + ") " + bus.getDes())
                        .snippet("Speed: " + bus.getSpd())
                        .draggable(false)
                        .rotation(bus.getHdg())
                        .icon(BitmapDescriptorFactory.fromAsset(bus.getRt() + ".png"))
                        .flat(true);
                try {
                    mMap.addMarker(marker);
                } catch(NullPointerException e) {
                    System.err.println("mMap or marker is null");
                }


            }
        }
    }
}