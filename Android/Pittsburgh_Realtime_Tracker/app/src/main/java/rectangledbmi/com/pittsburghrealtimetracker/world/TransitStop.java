package rectangledbmi.com.pittsburghrealtimetracker.world;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.Marker;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is supposed to be class that maps the stop markers to the route numbers. In this way,
 * and somehow get them to be able to
 *
 * Created by epicstar on 9/5/14.
 */
public class TransitStop {
    private ConcurrentHashMap<Marker, Set<String>> markerSet;
    private LinkedList<Marker> marker;

    public TransitStop() {
        markerSet = new ConcurrentHashMap<>(200);
    }

    public boolean add(Marker marker, String route) {
        Set<String> routes = markerSet.get(marker);
        if(routes == null) {
            routes = Collections.synchronizedSet(new HashSet<String>());
        }
        return routes.add(route);
    }

//    public boolean remove(String route) {
//        MultiMap
//    }
}
