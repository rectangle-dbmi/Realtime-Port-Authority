package rectangledbmi.com.pittsburghrealtimetracker.handlers.containers;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.LinkedList;

import rectangledbmi.com.pittsburghrealtimetracker.world.LineInfo;

/**
 * Container class for the the RequestLine Asynctask that gets the polylines and bus stop info.
 *
 * Will use a LinkedList<LinkedList<LatLng>> and BusStopInfo setter and getter
 *
 * Created by epicstar on 12/7/14.
 */
public class RequestLineContainer {

    private ArrayList<LinkedList<LatLng>> polylinesInfo;
    private ArrayList<LineInfo> busStopInfos;

    /**
     * General class to set the RequestLineContainer
     *
     * @param polylinesInfo LinkedList of a LinkedList of latlngs for the polylines
     * @param busStopInfos the bus stop info of the line
     */
    public RequestLineContainer(ArrayList<LinkedList<LatLng>> polylinesInfo,
                                ArrayList<LineInfo> busStopInfos) {
        setPolylinesInfo(polylinesInfo);
        setBusStopInfos(busStopInfos);
    }

    public void setPolylinesInfo(ArrayList<LinkedList<LatLng>> polylinesInfo) {
        this.polylinesInfo = polylinesInfo;
    }

    public void setBusStopInfos(ArrayList<LineInfo> busStopInfos) {
        this.busStopInfos = busStopInfos;
    }

    public ArrayList<LinkedList<LatLng>> getPolylinesInfo() {
        return polylinesInfo;
    }

    public ArrayList<LineInfo> getBusStopInfos() {
        return busStopInfos;
    }
}
