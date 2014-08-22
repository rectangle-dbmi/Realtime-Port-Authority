package rectangledbmi.com.pittsburghrealtimetracker.handlers;

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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;

public class RequestTask extends AsyncTask<Void, Void, List<Bus>> {

    GoogleMap mMap;
    List<Bus> bl;
    List<String> selectedBuses;

    public RequestTask(GoogleMap map, List<String> buses){
        mMap = map;
        selectedBuses = buses;
        bl = new ArrayList<Bus>(8);
    }

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
        for(String selectedBus : selectedBuses) {
            URL url = null;
            try {
                url = new URL(
                        "http://realtime.portauthority.org/bustime/api/v2/getvehicles?key=KiJEdJUDgRFxcG7cpt3ae6xxJ&rt=" + selectedBus
                );
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            SAXHandler handler = new SAXHandler();
            try {
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
            } catch (NullPointerException sax) {
                System.out
                        .println("Bus route is not tracked or all buses on route are in garage.");
                System.exit(0);
            }
//            bl.add(handler.busList);
            addBuses(handler.busList);
        }
        return bl;
    }

    private void addBuses(List<Bus> busList) {
        for(Bus bus : busList)
            bl.add(bus);
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(List<Bus> list) {
        for(Bus bus : bl) {
            LatLng latlng = new LatLng(bus.getLat(), bus.getLon());
            MarkerOptions marker = new MarkerOptions()
                    .position(latlng)
                    .title(bus.getRt() + "(" + bus.getVid() + ") " + bus.getDes())
                    .snippet("Speed: " + bus.getSpd())
                    .draggable(false)
                    .rotation(bus.getHdg())
                    .icon(BitmapDescriptorFactory.fromAsset("arrow"))
                    .flat(true);
            mMap.addMarker(marker);
        }
    }
}