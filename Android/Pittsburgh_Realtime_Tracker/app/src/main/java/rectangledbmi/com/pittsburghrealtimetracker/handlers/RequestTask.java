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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;

public class RequestTask extends AsyncTask<Void, Void, List<Bus>> {

    GoogleMap mMap;
    List<Bus> bl;
    String selectedBuses;

    public RequestTask(GoogleMap map, List<String> buses){
        mMap = map;
        selectedBuses = selectBuses(buses);
        bl = null;
    }

    private String selectBuses(List<String> buses) {
        StringBuffer string = new StringBuffer();
        int oneLess = string.length()-1;
        for(int i=0;i<string.length();++i) {
            string.append(buses.get(i));
            if(i != oneLess)
                string.append(",");
        }
        return string.toString();
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
            URL url = null;
            try {
                url = new URL(
                        "http://realtime.portauthority.org/bustime/api/v2/getvehicles?key=KiJEdJUDgRFxcG7cpt3ae6xxJ&rt="
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
                System.err
                        .println("Bus route is not tracked or all buses on route are in garage: " + selectedBuses);
//                System.exit(0);
            }
            bl = handler.busList;
        return bl;
    }
    //TODO make a HashMap for the bus markers and use that to update the marker location...
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