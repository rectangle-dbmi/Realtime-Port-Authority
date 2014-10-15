package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is the way to add the polylines if it's not present on the map
 * Created by epicstar on 10/14/14.
 */
public class RequestLine extends AsyncTask<Void, Void, List<LatLng>> {
    /**
     * The Google Map
     */
    private GoogleMap mMap;
    /**
     * the Map that contains the Polylines by bus route string
     */
    private Map<String, Polyline> patterns;

    /**
     * The route that was selected
     */
    private String selectedRoute;

    /**
     * The route color
     */
    private int color;
    //TODO: selectedRoute and color have to go out in order to add the polylines to the map...
    public RequestLine(GoogleMap mMap, Map<String, Polyline> patterns, String selectedRoute, int color) {
        this.mMap = mMap;
        this.patterns = patterns;
        this.selectedRoute = selectedRoute;
        this.color = color;
    }

    /**
     * Pull parser that gets the XML from the background
     * @param voids nothing
     * @return the results to put the PolyLine on the map as a list
     */
    @Override
    protected List<LatLng> doInBackground(Void... voids) {
//        SAXParserFactory spf = SAXParserFactory.newInstance();
//        SAXParser sp = null;
//        try {
//            sp = spf.newSAXParser();
//        } catch(ParserConfigurationException e) {
//            e.printStackTrace();
//        } catch (SAXException e) {
//            e.printStackTrace();
//        }
//
//        URL url = null;
//
//        try {
//            url = new URL(
//                    "http://realtime.portauthority.org/bustime/api/v2/getpatterns?key=KiJEdJUDgRFxcG7cpt3ae6xxJ&rt=" + selectedRoute
//            );
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//
//        RouteLineSaxHandler handler;
//        List<LatLng> points = null;
//        try {
//            handler = new RouteLineSaxHandler();
//            try {
//                if(sp != null) {
//                    sp.parse(new InputSource(url != null ? url.openStream() : null), handler);
//                }
//            } catch (SAXException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            System.out.println(handler.getPoints());
//            points = handler.getPoints();
//        } catch (NullPointerException e) {
//            System.out.println("Routes are not being added");
//        }

        List<LatLng> points = null;
        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            URL url = new URL(
                    "http://realtime.portauthority.org/bustime/api/v2/getpatterns?key=KiJEdJUDgRFxcG7cpt3ae6xxJ&rt=" + selectedRoute
            );

            parser.setInput(url.openStream(), null);
            // get the list...
            points = parseXML(parser);
            System.out.println(points);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;

        }
        return points;

    }

    /**
     * This is the pull parser method to get the polyline points for the specific route
     * @param parser the XMLPullParser that opens the URL
     * @return the list of points for the Polyline to be added
     * @throws XmlPullParserException
     * @throws IOException
     */
    private synchronized List<LatLng> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<LatLng> points = new LinkedList<LatLng>();
        int eventType = parser.getEventType();
        double tempLat = 0.0;
        double tempLong = 0.0;
        int seq = 1;
        int tempSeq = 0;
        while(eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;

            switch (eventType) {

                case(XmlPullParser.START_TAG) : {
                    name = parser.getName();
                    if("rtdir".equals(name)) {
                        addPoints(points, tempLat, tempLong, seq, tempSeq, true);
                        seq = 1;
                    }

                    else if("lat".equals(name)) {
                        tempLat = Double.parseDouble(parser.nextText());
//                        System.out.println("Lat: " + tempLat);
                    }
                    else if("lon".equals(name)) {
                        tempLong = Double.parseDouble(parser.nextText());
//                        System.out.println("Lon: " + tempLong);
                    }
                    else if("seq".equals(name)) {
                        tempSeq = Integer.parseInt(parser.nextText());
                    }
                }
                case(XmlPullParser.END_TAG) : {
                    if("pt".equals(name)) {
                        seq = addPoints(points, tempLat, tempLong, seq, tempSeq, false);
                    }
                }
            }
            eventType = parser.next();
        }
        return points;
    }

    /**
     * Adds points to the points list (the list of points for the PolyLine). adds if the sequence is correct
     * and the tempLat or tempLong isn't the initialized value (Africa -> 0.0, 0.0)
     * @param points the list of points for the Polyline
     * @param tempLat the temporary latitude
     * @param tempLong the temporary longitude
     * @param seq the sequence counter
     * @param tempSeq What the XML says its sequence is
     * @param loop whether or not the this is a looparound add (TODO: broken here)
     * @return the sequence incremented if successful. else the same sequence...
     */
    private synchronized int addPoints(List<LatLng> points, double tempLat, double tempLong, int seq, int tempSeq, boolean loop) {
        if((tempLat != 0.0 || tempLong != 0.0) && (loop || seq == tempSeq)) {
            points.add(new LatLng(tempLat, tempLong));
            return seq + 1;
        }
        return seq;
    }

    /**
     * Adds the polyline to the map on the UI thread
     * @param latLngs the results from doInBackground obtained from the XML
     */
    @Override
    protected void onPostExecute(List<LatLng> latLngs) {
        System.out.println("Working?");
        if(latLngs != null) {
            patterns.put(selectedRoute, mMap.addPolyline(new PolylineOptions().addAll(latLngs).color(color).geodesic(true).visible(true)));
        }
    }
}
