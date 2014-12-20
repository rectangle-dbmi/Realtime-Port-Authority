package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.location.Location;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.containers.RequestLineContainer;
import rectangledbmi.com.pittsburghrealtimetracker.hidden.PortAuthorityAPI;
import rectangledbmi.com.pittsburghrealtimetracker.world.LineInfo;
import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStop;

/**
 * This is the way to add the polylines if it's not present on the map
 * <p/>
 * REQUIRES PortAuthorityAPI class to get the Port Authority URLs
 * <p/>
 * TODO: to get the bus stops... change linkedList<LinkedList<LatLng>> to be a custom wrapper
 *
 * @author Jeremy Jao
 */

public class RequestLine extends AsyncTask<Void, Void, RequestLineContainer> {
    /**
     * The Google Map
     */
    private GoogleMap mMap;

    /**
     * the Map that contains the Polylines by bus route string
     */
    private ConcurrentMap<String, List<Polyline>> patterns;

    /**
     * The route that was selected
     */
    private String selectedRoute;

    private ConcurrentMap<Integer, Marker> busStops;

    private float zoom, zoomVisibility;

    TransitStop transitStop;

    /**
     * The route color
     */
    private int color;

    //TODO: selectedRoute and color have to go out in order to add the polylines to the map...
    public RequestLine(GoogleMap mMap, ConcurrentMap<String, List<Polyline>> patterns,
                       String selectedRoute, ConcurrentMap<Integer, Marker> busStops, int color,
                       float zoomLevel,
                       float zoomVisibility,
                       TransitStop stopMap
    ) {
        this.mMap = mMap;
        this.patterns = patterns;
        this.selectedRoute = selectedRoute;
        this.color = color;
        this.busStops = busStops;
        this.zoom = zoomLevel;
        this.transitStop = stopMap;
        this.zoomVisibility = zoomVisibility;
    }

    /**
     * Pull parser that gets the XML from the background
     *
     * @param voids nothing
     * @return the results to put the PolyLine on the map as a list
     */
    @Override
    protected RequestLineContainer doInBackground(Void... voids) {
        RequestLineContainer points;
        XmlPullParserFactory pullParserFactory;
        try {
            points = null;
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            URL url = PortAuthorityAPI.getPatterns(selectedRoute);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            InputStream in = conn.getInputStream();
            if (in != null) {
                parser.setInput(conn.getInputStream(), null);
                // get the list...
                points = parseXML(parser);
            }

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
     *
     * @param parser the XMLPullParser that opens the URL
     * @return the list of points for the Polyline to be added
     * @throws XmlPullParserException
     * @throws IOException
     */
    private RequestLineContainer parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {

        LinkedList<LatLng> points = new LinkedList<>();
        LinkedList<LinkedList<LatLng>> allPoints = new LinkedList<>();
        LinkedList<LineInfo> busStopInfos = new LinkedList<>();
        int eventType = parser.getEventType();
        double tempLat = 0.0;
        double tempLong = 0.0;
        int seq = 1;
        int tempSeq = 0;

        String rtdir = null;

        String stpnm = null;
        String stpid = null;
        boolean isBusStop = false;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name;

            try {
                switch (eventType) {

                    case (XmlPullParser.START_TAG): {
                        name = parser.getName();
                        switch (name) {
                            case "rtdir":
                                points = new LinkedList<>();
                                allPoints.add(points);
                                rtdir = parser.nextText();
                                seq = 1;
                                break;
                            case "lat":
                                tempLat = Double.parseDouble(parser.nextText());
                                break;
                            case "lon":
                                tempLong = Double.parseDouble(parser.nextText());
                                break;
                            case "seq":
                                tempSeq = Integer.parseInt(parser.nextText());
                                break;
                            case "typ":
                                if ("S".equals(parser.nextText())) {
                                    isBusStop = true;
                                }
                                break;
                            case "stpid":
                                stpid = parser.nextText();
                                break;
                            case "stpnm":
                                stpnm = parser.nextText();
                                break;
                        }

                        break;
                    }
                    case (XmlPullParser.END_TAG): {
                        name = parser.getName();
                        if ("pt".equals(name)) {
                            seq = addPoints(points, tempLat, tempLong, seq, tempSeq, false);
                            if (isBusStop) {
                                busStopInfos.add(new LineInfo(stpid, stpnm, rtdir, tempLat, tempLong));
                                isBusStop = false;
                                stpid = null;
                                stpnm = null;
                            }
                        }

                        break;
                    }
                }

            } catch (NullPointerException e) {

            }
            eventType = parser.next();
        }
        connectPoints(allPoints);

        return new RequestLineContainer(allPoints, busStopInfos);
    }


    private void connectPoints(LinkedList<LinkedList<LatLng>> allPoints) {
        boolean[] firstconnect = new boolean[allPoints.size()];
        boolean[] lastconnect = new boolean[allPoints.size()];
        int firstindex = 0;
        for (LinkedList<LatLng> firstPoint : allPoints) {
            LatLng tempLatLng = new LatLng(0, 0);
            float min = Float.MAX_VALUE;
            int lastindex = 0;
            int templastindex = Integer.MAX_VALUE;
            if (!firstconnect[firstindex]) {
                for (LinkedList<LatLng> endPoints : allPoints) {
                    if (!lastconnect[lastindex] && !equals(firstPoint, endPoints)) {

                        float tempdistance = distanceBtwn(firstPoint.getFirst(), endPoints.getLast());

                        if (tempdistance == (float) 0) {
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
                        (min != Float.MAX_VALUE) && min <= (float) 700) {
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
     *
     * @param points   the list of points for the Polyline
     * @param tempLat  the temporary latitude
     * @param tempLong the temporary longitude
     * @param seq      the sequence counter
     * @param tempSeq  What the XML says its sequence is
     * @param loop     whether or not the this is a looparound add (TODO: broken here)
     * @return the sequence incremented if successful. else the same sequence...
     */
    private int addPoints(LinkedList<LatLng> points, double tempLat, double tempLong, int seq, int tempSeq, boolean loop) {
        if ((tempLat != 0.0 || tempLong != 0.0) && (seq == tempSeq)) {
            LatLng templatlong = new LatLng(tempLat, tempLong);
            points.add(templatlong);
            return seq + 1;
        }
        return seq;
    }

    /**
     * Adds the polyline to the map on the UI thread
     *
     */
    @Override
    protected void onPostExecute(RequestLineContainer container) {
        LinkedList<LinkedList<LatLng>> latLngs = container.getPolylinesInfo();
        LinkedList<LineInfo> busStopInfos = container.getBusStopInfos();
        List<Polyline> polylines = new LinkedList<>();
        if (latLngs != null) {
            for (LinkedList<LatLng> points : latLngs) {
                polylines.add(mMap.addPolyline(new PolylineOptions().addAll(points).color(color).geodesic(true).visible(true)));
            }
            patterns.put(selectedRoute, polylines);
        }

        if (busStopInfos != null) {

            for (LineInfo busStopInfo : busStopInfos) {
                if (!transitStop.addRouteToMarker(busStopInfo.getStpid(), selectedRoute, zoom, zoomVisibility)) {
                    Marker marker = mMap.addMarker(new MarkerOptions().
                                    position(busStopInfo.getLatLng()).
                                    alpha(.65f).
                                    flat(false).
                                    title("(" + busStopInfo.getStpid() + ") " + busStopInfo.getStpnm()).
                                    snippet(busStopInfo.getRtdir()).
                                    draggable(false).
                                    icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop)).
                                    anchor(.5f, .5f)
                                    .visible(false)
                    );
                    transitStop.addMarkerToRoute(marker, busStopInfo.getStpid(), selectedRoute, zoom, zoomVisibility);
                }
            }
        }
    }
}
