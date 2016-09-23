package rectangledbmi.com.pittsburghrealtimetracker.patapi;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import rectangledbmi.com.pittsburghrealtimetracker.BuildConfig;

/**
 * This is our first way of using the Port Authority API before we actually started using RetroFit.
 * This has a lot of static methods to get URLs of the Port Authority API. Eventually, in the future,
 * we will only be using RetroFit and will stop using this.
 * @author Jeremy Jao
 */
public class PortAuthorityAPI {

    private static final String API_KEY = BuildConfig.PAT_API_KEY;

    private static final String MAIN_LINK = "http://truetime.portauthority.org/bustime/api/v2/";
    private static final String keyEquals = "?key=";
    private static final String routeEquals = "&rt=";
    private static final String stopEquals = "&stpid=";
    private static final String busEquals = "&vid=";

    /**
     * Gets the patterns for a bus stop and more importantly the patterns using getpatterns
     *
     * @param route the selected route
     * @return the URL for the bustops and routes
     * @throws MalformedURLException
     */
    public static URL getPatterns(String route) throws MalformedURLException {
        return new URL(
                MAIN_LINK +
                        "getpatterns" +
                        keyEquals +
                        API_KEY +
                        routeEquals +
                        route
        );
    }

    /**
     * Gets the vehicles (buses, trains) using getpatterns
     *
     * @param routes the routes used to
     * @return the URL to get the buses
     * @throws MalformedURLException
     */
    public static URL getVehicles(String routes) throws MalformedURLException {
        return new URL(
                MAIN_LINK +
                        "getvehicles" +
                        keyEquals +
                        API_KEY +
                        routeEquals +
                        routes
        );
    }

    /**
     * Gets the vehicles (buses, trains) using getpatterns from a list of vehicles
     * @param routes array of routes
     * @return URL to get the buses
     * @throws MalformedURLException
     */
    public static URL getVehicles(String[] routes) throws MalformedURLException {
        return getVehicles(arrayToString(routes));
    }

    /**
     * @param data data to turn an array to a string
     * @return a comma-delim string of data
     */
    private static String arrayToString(String[] data) {
        StringBuilder string = new StringBuilder();
        int oneLess = data.length-1;
        for(int i=0;i<data.length;++i) {
            string.append(data[i]);
            if(i != oneLess) {
                string.append(",");
            }
        }
        return string.toString();
    }

    /**
     * gets all routes currently on the list
     *
     * @return the URL to get the routes
     * @throws MalformedURLException
     */
    public static URL getRoutes() throws MalformedURLException {
        return new URL("http://realtime.portauthority.org/bustime/api/v2/getroutes?key=" + API_KEY);
    }

    /**
     * Gets predictions for the buses to stopRenderInfos
     * @param vid the bus id
     * @return the URL to get the predictions for the specific bus
     * @throws MalformedURLException
     */
    public static URL getBusPredictions(Integer vid) throws MalformedURLException {
        return new URL(
                MAIN_LINK +
                        "getpredictions" +
                        keyEquals +
                        API_KEY +
                        busEquals +
                        Integer.toString(vid)
        );
    }

    /**
     * Gets all predictions for the specific stop
     * @param stpid the stop's id
     * @return the URL to get the predictions for the specific stop
     * @throws MalformedURLException
     */
    public static URL getStopPredictions(int stpid) throws MalformedURLException {
        return new URL(
                MAIN_LINK +
                        "getpredictions" +
                        keyEquals +
                        API_KEY +
                        stopEquals +
                        Integer.toString(stpid)
        );
    }
    /**
     * Gets all predictions for the specific stop
     * @param stpid the stop's id
     * @return the URL to get the predictions for the specific stop
     * @throws MalformedURLException
     */
    public static URL getStopPredictions(int stpid, Set<String> selectedBuses) throws MalformedURLException {
        return new URL(
                MAIN_LINK +
                        "getpredictions" +
                        keyEquals +
                        API_KEY +
                        stopEquals +
                        Integer.toString(stpid) +
                        routeEquals +
                        arrayToString(selectedBuses.toArray(new String[selectedBuses.size()]))
        );
    }

}
