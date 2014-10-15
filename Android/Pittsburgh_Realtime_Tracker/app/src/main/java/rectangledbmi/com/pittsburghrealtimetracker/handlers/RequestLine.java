package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by epicstar on 10/14/14.
 */
public class RequestLine extends AsyncTask<Void, Void, List<LatLng>> {

    private GoogleMap mMap;
    private Map<String, Polyline> patterns;
    private String selectedRoute;
    private int color;

    public RequestLine(GoogleMap mMap, Map<String, Polyline> patterns, String selectedRoute, int color) {
        this.mMap = mMap;
        this.patterns = patterns;
        this.selectedRoute = selectedRoute;
        this.color = color;
    }
    @Override
    protected List<LatLng> doInBackground(Void... voids) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = null;
        try {
            sp = spf.newSAXParser();
        } catch(ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        URL url = null;

        try {
            url = new URL(
                    "http://realtime.portauthority.org/bustime/api/v2/getpatterns?key=KiJEdJUDgRFxcG7cpt3ae6xxJ&rt=" + selectedRoute
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        RouteLineSaxHandler handler;
        List<LatLng> points = null;
        try {
            handler = new RouteLineSaxHandler();
            try {
                if(sp != null) {
                    sp.parse(new InputSource(url != null ? url.openStream() : null), handler);
                }
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(handler.getPoints());
            points = handler.getPoints();
        } catch (NullPointerException e) {
            System.out.println("Routes are not being added");
        }


        return points;
    }

    @Override
    protected void onPostExecute(List<LatLng> latLngs) {
        System.out.println("Working?");
        if(latLngs != null) {
            patterns.put(selectedRoute, mMap.addPolyline(new PolylineOptions().addAll(latLngs).color(color).geodesic(true).visible(true)));
        }
    }
}
