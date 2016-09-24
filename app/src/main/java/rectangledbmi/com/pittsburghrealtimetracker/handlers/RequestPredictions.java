package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.containers.ETAContainer;
import rectangledbmi.com.pittsburghrealtimetracker.patapi.PortAuthorityAPI;
import rectangledbmi.com.pittsburghrealtimetracker.world.Prediction;

/**
 * Gets the predictions from a specific marker's title's id, decides if it's a stop or marker,
 * then it will add it to a special dialog.
 * <p/>
 * Created by epicstar on 12/17/14.
 */
public class RequestPredictions extends AsyncTask<String, Void, ETAContainer> {

    //    private Marker marker;
//    private GoogleMap mMap;
    private Marker marker;
    //    private Set<Integer> busIds;
//    private Set<Integer> stopIds;
    private Set<String> selectedBuses;
    //    private FragmentManager fragmentManager;
    private Context context;


    /*    /**
         * Initializes the asynctask
         * @param busIds set of id of buses
         * @param stopIds set of id of bus getStopRenderRequests
         * @param fragmentManager the fragment manager class from the activity
         * @param context the context of the activity
         */
    public RequestPredictions(/*GoogleMap mMap,
                              Marker marker,
                              Set<Integer> busIds, Set<Integer> stopIds,
                              FragmentManager fragmentManager,
                              Set<String> selectedBuses,*/
                              Context context,
                              Marker marker,
                              Set<String> selectedBuses


    ) {
        this.marker = marker;
        this.selectedBuses = selectedBuses;
        this.context = context;
    }

    /**
     * This is the background thread...
     * <p/>
     * Makes sure that:
     * * we are looking at a stop or bus id
     * * get the info of the bus or stop id
     * * return this into an ETAContainer
     *
     * @return an ETA Container that contains the dialog's title and message
     */
    @Override
    protected ETAContainer doInBackground(String... params) {
        String markerTitle = params[0];
        String message = "";

        try {
            URL url = null;
            int id = Integer.parseInt(markerTitle.substring(markerTitle.indexOf("(") + 1, markerTitle.indexOf(")")));
            int sw = 0;
            if (params[0].contains("INBOUND") || params[0].contains("OUTBOUND")) {
                Log.d("stop_id", Integer.toString(id));
                Log.d("selected buses", selectedBuses.toString());

                url = PortAuthorityAPI.getStopPredictions(id, selectedBuses);
                Log.d("url", url.toString());
                Log.d("prediction_type", "stop");

                sw = 1; //looking at a bus id
            } else {
                Log.d("prediction_type", "bus");
                url = PortAuthorityAPI.getBusPredictions(id);
            }

            PredictionsXMLPullParser predictionsXMLPullParser = new PredictionsXMLPullParser(url, context);
            List<Prediction> predictions = predictionsXMLPullParser.createPredictionList();
            StringBuilder st = new StringBuilder();
            LinkedList<String> stopPredictions = new LinkedList<>();

            if (predictions != null) {
                int i = 0;
                for (Prediction prediction : predictions) {
                    Log.d("time", prediction.getPrdtm().split(" ")[1]);
                    SimpleDateFormat date = new SimpleDateFormat("hh:mm a", Locale.US);

                    StringBuilder addString = new StringBuilder(date.format(new SimpleDateFormat("HH:mm", Locale.US).parse(prediction.getPrdtm().split(" ")[1])));

                    if (sw == 0) { // bus dialog that displays getStopRenderRequests
                        stopPredictions.add("(" + prediction.getStpid() + ")" + prediction.getStpnm() + ": " + addString);
                    } else if (sw == 1) { // stop dialog that displays routes
                        Log.d("delayed", prediction.getDly());
                        stopPredictions.add(prediction.getRt() + " (" + prediction.getVid() + "): " + addString + (prediction.getDly().equals("true") ? " - delayed" : ""));
                    }
                    if (++i == 8)
                        break;
                }
                message = createMessage(stopPredictions);
                return new ETAContainer(markerTitle, message);
            }


        } catch (MalformedURLException e) {
            Log.d("HELLO", e.getMessage());
        } catch (XmlPullParserException | IOException e) {
            Log.e("XML_ERROR", e.getMessage());
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * TODO: improve this to be arranged by stop time especially when looking at a bus's ETAs probably will have to use a different structure for that case
     * <p/>
     * takes the message from the Predictions Pull Parser and turns it into a readable message (for now)
     *
     * @param idTimes
     * @param sw
     * @return
     */
    public String createMessage(ConcurrentHashMap<String, StringBuilder> idTimes, int sw) {
        StringBuilder st = new StringBuilder();
        Set<Map.Entry<String, StringBuilder>> idEntries = idTimes.entrySet();
        int i = 0;
        for (Map.Entry<String, StringBuilder> info : idEntries) {
            if (i == 5 && sw == 0) {
                break;
            }
            i++;
            st.append(info.getKey());
            st.append(":\n  ");
            st.append(info.getValue());
            st.append("\n");
        }

        return st.toString();
    }

    public String createMessage(LinkedList<String> stringLinkedList) {
        StringBuilder st = new StringBuilder();

        for (String string : stringLinkedList) {
            st.append(string);
            st.append("\n");
        }

        return st.toString();
    }

    protected void onPostExecute(ETAContainer container) {
        if (container != null) {
            if (!container.getMessage().isEmpty())
                marker.setSnippet(container.getMessage());
            else {
                marker.setSnippet(context.getResources().getString(R.string.predictions_not_available));
            }
            marker.showInfoWindow();
        }

    }
}
