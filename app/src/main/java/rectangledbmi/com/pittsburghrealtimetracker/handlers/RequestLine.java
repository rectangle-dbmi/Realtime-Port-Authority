package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.http.HttpResponseCache;
import android.os.AsyncTask;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.util.Log;

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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.containers.RequestLineContainer;
import rectangledbmi.com.pittsburghrealtimetracker.world.LineInfo;
import rectangledbmi.com.pittsburghrealtimetracker.world.TransitStopCollection;

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

    private float zoom, zoomVisibility;

    TransitStopCollection transitStopCollection;

    /**
     * The route color
     */
    private int color;

    private Context context;

    //TODO: selectedRoute and color have to go out in order to add the polylines to the map...
    public RequestLine(GoogleMap mMap, ConcurrentMap<String, List<Polyline>> patterns,
                       String selectedRoute, int color,
                       float zoomLevel,
                       float zoomVisibility,
                       TransitStopCollection stopMap,
                       Context context
    ) {
        this.mMap = mMap;
        this.patterns = patterns;
        this.selectedRoute = selectedRoute;
        this.color = color;
        this.zoom = zoomLevel;
        this.transitStopCollection = stopMap;
        this.zoomVisibility = zoomVisibility;
        this.context = context;
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
            checkSD();
            pullParserFactory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = pullParserFactory.newPullParser();
            HttpResponseCache cache = HttpResponseCache.getInstalled();
            if(cache != null) {
                Log.d("cache_info_lines", selectedRoute + ": " + Integer.toString(cache.getHitCount()));
            } else {
                Log.d("cache_info", selectedRoute + ": " + "cache is empty");
            }
            InputStream in = new FileInputStream(context.getFilesDir() + "/lineinfo/" + selectedRoute + ".xml");

            parser.setInput(in, null);
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

    private void checkSD() {
        File sd = new File(context.getFilesDir() + "/lineinfo");
        if(!sd.exists()) {
            sd.mkdirs();
        }
        File routeFile = new File(sd.getName() + "/" + selectedRoute + ".xml");
        if(!routeFile.exists()) {
            InputSave save = new InputSave(context);
            save.saveFile(selectedRoute);
        }

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
        ArrayList<LinkedList<LatLng>> allPoints = new ArrayList<>();
        ArrayList<LineInfo> busStopInfos = new ArrayList<>();
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
                            seq = addPoints(points, tempLat, tempLong, seq, tempSeq);
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
                Log.e("requestline_error", e.getMessage());
            }
            eventType = parser.next();
        }
        connectPoints(allPoints);

        return new RequestLineContainer(allPoints, busStopInfos);
    }


    private void connectPoints(ArrayList<LinkedList<LatLng>> allPoints) {
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
     * @return the sequence incremented if successful. else the same sequence...
     */
    private int addPoints(LinkedList<LatLng> points, double tempLat, double tempLong, int seq, int tempSeq) {
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
        if(mMap != null && container != null) {
            ArrayList<LinkedList<LatLng>> latLngs = container.getPolylinesInfo();
            ArrayList<LineInfo> busStopInfos = container.getBusStopInfos();
            if (latLngs != null) {
                List<Polyline> polylines = new ArrayList<>(latLngs.size());
                for (LinkedList<LatLng> points : latLngs) {
                    polylines.add(mMap.addPolyline(new PolylineOptions().addAll(points).color(color).geodesic(true).visible(true)));
                }
                patterns.put(selectedRoute, polylines);
            }

            if (busStopInfos != null) {

                for (LineInfo busStopInfo : busStopInfos) {
                    if (!transitStopCollection.addRouteToMarker(busStopInfo.getStpid(), selectedRoute, zoom, zoomVisibility)) {
                        Drawable drawable = VectorDrawableCompat.create(context.getResources(), R.drawable.bus_stop, context.getTheme());
                        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

                        Canvas canvas = new Canvas(bitmap);
                        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                        drawable.draw(canvas);

                        Marker marker = mMap.addMarker(new MarkerOptions().
                                        position(busStopInfo.getLatLng()).
                                        alpha(.65f).
                                        flat(false).
                                        title("(" + busStopInfo.getStpid() + ") " + busStopInfo.getStpnm() + " " + busStopInfo.getRtdir()).
                                        snippet(busStopInfo.getRtdir()).
                                        draggable(false).
                                        icon(BitmapDescriptorFactory.fromBitmap(bitmap)).
                                        anchor(.5f, .5f)
                                        .visible(false)
                        );
                        transitStopCollection.addMarkerToRoute(marker, busStopInfo.getStpid(), selectedRoute, zoom, zoomVisibility);
                    }
                }
            }
        }
    }
}
