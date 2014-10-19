package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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
import java.util.concurrent.ConcurrentMap;

import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStop;

/**
 * This is the way to add the polylines if it's not present on the map
 * Created by epicstar on 10/14/14.
 */
//public class RequestLine extends AsyncTask<Void, Void, LinkedList<LatLng>> {
public class RequestLine extends AsyncTask<Void, Void, LinkedList<LinkedList<LatLng>>> {
    /**
     * The Google Map
     */
    private GoogleMap mMap;
    /**
     * the Map that contains the Polylines by bus route string
     */
    private ConcurrentMap<String, List<Polyline>> patterns;
//    private ConcurrentMap<String, Polyline> patterns;

    /**
     * The route that was selected
     */
    private String selectedRoute;

    private ConcurrentMap<Integer, Marker> busStops;

    /**
     * The route color
     */
    private int color;
    //TODO: selectedRoute and color have to go out in order to add the polylines to the map...
//    public RequestLine(GoogleMap mMap, ConcurrentMap<String, Polyline> patterns, String selectedRoute, int color) {
    public RequestLine(GoogleMap mMap, ConcurrentMap<String, List<Polyline>> patterns, String selectedRoute, ConcurrentMap<Integer, Marker> busStops, int color) {
        this.mMap = mMap;
        this.patterns = patterns;
        this.selectedRoute = selectedRoute;
        this.color = color;
        this.busStops = busStops;
    }

    /**
     * Pull parser that gets the XML from the background
     * @param voids nothing
     * @return the results to put the PolyLine on the map as a list
     */
    @Override
//    protected LinkedList<LatLng> doInBackground(Void... voids) {
    protected LinkedList<LinkedList<LatLng>> doInBackground(Void... voids) {
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

//        LinkedList<LatLng> points;
        LinkedList<LinkedList<LatLng>> points;
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
//    private LinkedList<LatLng> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
    private LinkedList<LinkedList<LatLng>> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        LinkedList<LatLng> points = new LinkedList<LatLng>();
        LinkedList<LinkedList<LatLng>> allPoints = new LinkedList<LinkedList<LatLng>>();
        int eventType = parser.getEventType();
        double tempLat = 0.0;
        double tempLong = 0.0;
        int seq = 1;
        int tempSeq = 0;
        TransitStop transitStop = null;
        while(eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;

            switch (eventType) {

                case(XmlPullParser.START_TAG) : {
                    name = parser.getName();
                    if("rtdir".equals(name)) {
                        points = new LinkedList<LatLng>();
                        allPoints.add(points);

//                        addPoints(points, tempLat, tempLong, seq, tempSeq, true);
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
///*                    else if("typ".equals(name)) {
//                        String type = parser.nextText();
//                        if("S".equals(type)) {
//                            isStop = true;
//                        }
//                    }*/
                    else if("stpid".equals(name)) {
                        transitStop = new TransitStop(Integer.parseInt(parser.nextText()));
                    }
                    else if("stpnm".equals(name)) {
                        transitStop.setDescription(parser.nextText());
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
//        return putPointsInOrder(allPoints);
        connectPoints(allPoints);
        return allPoints;
    }

    private void connectPoints(LinkedList<LinkedList<LatLng>> allPoints) {
        boolean[] firstconnect = new boolean[allPoints.size()];
        boolean[] lastconnect = new boolean[allPoints.size()];
        int firstindex = 0;
        for(LinkedList<LatLng> firstPoint : allPoints) {
            LatLng tempLatLng = new LatLng(0, 0);
            float min = Float.MAX_VALUE;
            int lastindex = 0;
            int templastindex = Integer.MAX_VALUE;
            if(!firstconnect[firstindex]) {
                for (LinkedList<LatLng> endPoints : allPoints) {
                    if (!lastconnect[lastindex] && !equals(firstPoint, endPoints)) {

                        float tempdistance = distanceBtwn(firstPoint.getFirst(), endPoints.getLast());

                        if (tempdistance == (float)0) {
                            templastindex = lastindex;
                            firstconnect[firstindex] = true;
                            lastconnect[lastindex++] = true;
                            break;
                        } else if (tempdistance < min) {
                            min = tempdistance;
                            tempLatLng = endPoints.getLast();
                            templastindex = lastindex;
                        }
                    }
                    ++lastindex;
                }
                if ((templastindex != Integer.MAX_VALUE &&
                            !firstconnect[firstindex] &&
                            !lastconnect[templastindex]) &&
                        !tempLatLng.equals(new LatLng(0, 0)) &&
                        (min != Float.MAX_VALUE) && min <= (float)6000) {
                    firstPoint.add(0, tempLatLng);
                    firstconnect[firstindex] = true;
                    lastconnect[templastindex] = true;
                }
                ++firstindex;
            }
        }

    }

    private boolean equals(LinkedList<LatLng> firstPoint, LinkedList<LatLng> lastPoint) {
        return (firstPoint.size() == lastPoint.size() &&
                firstPoint.getFirst().equals(lastPoint.getFirst()) &&
                firstPoint.getLast().equals(lastPoint.getLast()));
    }


    private float distanceBtwn(LatLng from, LatLng to) {
        Location A = new Location("A");
        A.setLatitude(from.latitude);
        A.setLongitude(from.longitude);

        Location B = new Location("B");
        B.setLatitude(to.latitude);
        B.setLongitude(to.longitude);
        return A.distanceTo(B);
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
    private int addPoints(LinkedList<LatLng> points, double tempLat, double tempLong, int seq, int tempSeq, boolean loop) {
        if((tempLat != 0.0 || tempLong != 0.0) && (seq == tempSeq)) {
            LatLng templatlong = new LatLng(tempLat, tempLong);
//            if(!points.contains(templatlong)) {
                points.add(templatlong);
                return seq + 1;
//            }
        }
        return seq;
    }

    /**
     * Adds the polyline to the map on the UI thread
     * @param latLngs the results from doInBackground obtained from the XML
     */
    @Override
    protected void onPostExecute(LinkedList<LinkedList<LatLng>> latLngs) {
        List<Polyline> polylines = new LinkedList<Polyline>();
        if(latLngs != null) {
            for(LinkedList<LatLng> points : latLngs) {
                polylines.add(mMap.addPolyline(new PolylineOptions().addAll(points).color(color).geodesic(true).visible(true)));
            }
            patterns.put(selectedRoute, polylines);
        }
    }
//    @Override
//    protected void onPostExecute(LinkedList<LatLng> latLngs) {
//        if(latLngs != null) {
//            patterns.put(selectedRoute, mMap.addPolyline(new PolylineOptions().addAll(latLngs).color(color).geodesic(true).visible(true)));
//        }
//    }
}
