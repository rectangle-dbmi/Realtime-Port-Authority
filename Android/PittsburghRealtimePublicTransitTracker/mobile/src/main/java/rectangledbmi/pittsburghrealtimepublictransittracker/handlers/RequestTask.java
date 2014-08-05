package rectangledbmi.pittsburghrealtimepublictransittracker.handlers;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
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

import world.Bus;

public class RequestTask extends AsyncTask<Void, Void, List<Bus>> {

    GoogleMap mMap;
    List<Bus> bl = null;

    public RequestTask(GoogleMap map){
        mMap = map;
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
                    "http://realtime.portauthority.org/bustime/api/v2/getvehicles?key=KiJEdJUDgRFxcG7cpt3ae6xxJ&rt=P1"
            );
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SAXHandler handler = new SAXHandler();
        try {
            try {
                sp.parse(new InputSource(url.openStream()), handler);
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
        bl = handler.busList;
        return bl;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(List<Bus>... list) {
        for(Bus bus : bl) {
            mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(bus.getLat(), bus.getLon()))
                            .title(bus.getVid() + " " + bus.getDes())
                            .draggable(false)
            );
        }
    }
}