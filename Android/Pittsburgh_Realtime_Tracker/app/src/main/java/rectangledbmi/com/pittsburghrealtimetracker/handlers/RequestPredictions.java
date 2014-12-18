package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import rectangledbmi.com.pittsburghrealtimetracker.hidden.PortAuthorityAPI;

/**
 * Created by epicstar on 12/17/14.
 */
public class RequestPredictions extends AsyncTask<Void, Void, String> {

    private Marker marker;
    private Set<Integer> busIds;
    private Set<Integer> stopIds;

    public RequestPredictions(Marker marker, Set<Integer> busIds, Set<Integer> stopIds) {
        this.marker = marker;
        this.busIds = busIds;
        this.stopIds = stopIds;
    }

    @Override
    protected String doInBackground(Void... params) {
        String markerTitle = marker.getTitle();
//        Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(markerTitle);
        String snippet = null;
        try {
            URL url = null;
            int id = Integer.parseInt(markerTitle.substring(markerTitle.indexOf("(") + 1, markerTitle.indexOf(")")));
            int sw = -1;
            if (busIds.contains(id)) {
                System.out.println("bus");
                url = PortAuthorityAPI.getBusPredictions(id);
                sw = 0;
            } else if (stopIds.contains(id)) {
                System.out.println("stop");
                url = PortAuthorityAPI.getStopPredictions(id);
                sw = 1;
            }

            

        } catch(MalformedURLException e) {
            Log.i("HELLO", e.getMessage());
        }


        return snippet;
    }

    protected void onPostExecute(String snippet) {
        if(snippet != null && snippet.length() > 0)
            marker.setSnippet(snippet);
    }
}
