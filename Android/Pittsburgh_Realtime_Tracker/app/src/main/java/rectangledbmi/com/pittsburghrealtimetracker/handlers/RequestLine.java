package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.location.Location;
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
import java.util.concurrent.ConcurrentMap;

/**
 * This is the way to add the polylines if it's not present on the map
 * Created by epicstar on 10/14/14.
 */
public class RequestLine extends AsyncTask<Void, Void, LinkedList<LatLng>> {
//public class RequestLine extends AsyncTask<Void, Void, LinkedList<LinkedList<LatLng>>> {
    /**
     * The Google Map
     */
    private GoogleMap mMap;
    /**
     * the Map that contains the Polylines by bus route string
     */
//    private ConcurrentMap<String, List<Polyline>> patterns;
    private ConcurrentMap<String, Polyline> patterns;

    /**
     * The route that was selected
     */
    private String selectedRoute;

    /**
     * The route color
     */
    private int color;
    //TODO: selectedRoute and color have to go out in order to add the polylines to the map...
    public RequestLine(GoogleMap mMap, ConcurrentMap<String, Polyline> patterns, String selectedRoute, int color) {
//    public RequestLine(GoogleMap mMap, ConcurrentMap<String, List<Polyline>> patterns, String selectedRoute, int color) {
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
    protected LinkedList<LatLng> doInBackground(Void... voids) {
//    protected LinkedList<LinkedList<LatLng>> doInBackground(Void... voids) {
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

        LinkedList<LatLng> points;
//        LinkedList<LinkedList<LatLng>> points;
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
    private synchronized LinkedList<LatLng> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
//    private synchronized LinkedList<LinkedList<LatLng>> parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
        LinkedList<LatLng> points = new LinkedList<LatLng>();
        LinkedList<LinkedList<LatLng>> allPoints = new LinkedList<LinkedList<LatLng>>();
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
                }
                case(XmlPullParser.END_TAG) : {
                    if("pt".equals(name)) {
                        seq = addPoints(points, tempLat, tempLong, seq, tempSeq, false);
                    }
                }
            }
            eventType = parser.next();
        }
        return putPointsInOrder(allPoints);
//        return allPoints;
    }

    private synchronized LinkedList<LatLng> putPointsInOrder(LinkedList<LinkedList<LatLng>> allPoints) {
        LinkedList<LatLng> finalList = new LinkedList<LatLng>();
        finalList.addAll(allPoints.removeFirst());
        LatLng connectFrom = finalList.getLast();

        while(allPoints.size() > 1) {
            float min = Float.MAX_VALUE;
            int index = 0;
            int tempIndex = index;
            for(LinkedList<LatLng> currentPoly : allPoints) {
                float tempDistance = distanceBtwn(connectFrom, currentPoly.getFirst());
                if(tempDistance < min) {
                    min = tempDistance;
                    tempIndex = index;
                }
                ++index;
            }
            finalList.addAll(allPoints.remove(tempIndex));
            connectFrom = finalList.getLast();
        }
        if(!allPoints.isEmpty() ) {
            System.out.println(selectedRoute);
            LinkedList<LatLng> lastPoints = allPoints.getLast();
            System.out.println(distanceBtwn(finalList.getLast(), lastPoints.getFirst()) + " first vs. last");
            System.out.println(distanceBtwn(finalList.getFirst(), lastPoints.getLast()) + " last vs. first");

            float firstToLast = distanceBtwn(finalList.getLast(), lastPoints.getFirst());
            float lastToFirst = distanceBtwn(finalList.getFirst(), lastPoints.getLast());
            if((firstToLast < lastToFirst) && (firstToLast <= (float)700)) {
                System.out.println("last added to end");
                finalList.addAll(lastPoints);
            }
            else if(lastToFirst <= (float)700){
                lastPoints.addAll(finalList);
                finalList = lastPoints;
                System.out.println("Last added to end of first");
            }
            System.out.println("Final... " + distanceBtwn(finalList.getFirst(), finalList.getLast()));
            if(distanceBtwn(finalList.getFirst(), finalList.getLast()) <= (float)700) {
                finalList.add(new LatLng(finalList.getFirst().latitude, finalList.getFirst().longitude));
            }

        }

        return finalList;
    }

    private synchronized float distanceBtwn(LatLng from, LatLng to) {
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
    private synchronized int addPoints(LinkedList<LatLng> points, double tempLat, double tempLong, int seq, int tempSeq, boolean loop) {
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
     *//*
    @Override
    protected void onPostExecute(LinkedList<LinkedList<LatLng>> latLngs) {
        List<Polyline> polylines = new LinkedList<Polyline>();
        if(latLngs != null) {
            for(LinkedList<LatLng> points : latLngs) {
                polylines.add(mMap.addPolyline(new PolylineOptions().addAll(points).color(color).geodesic(true).visible(true)));
            }
            patterns.put(selectedRoute, polylines);
        }
    }*/
    @Override
    protected void onPostExecute(LinkedList<LatLng> latLngs) {
        if(latLngs != null) {
            patterns.put(selectedRoute, mMap.addPolyline(new PolylineOptions().addAll(latLngs).color(color).geodesic(true).visible(true)));
        }
    }
}
