package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import rectangledbmi.com.pittsburghrealtimetracker.BusInformationDialog;
import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.containers.ETAContainer;
import rectangledbmi.com.pittsburghrealtimetracker.hidden.PortAuthorityAPI;
import rectangledbmi.com.pittsburghrealtimetracker.world.Prediction;

/**
 * Gets the predictions from a specific marker's title's id, decides if it's a stop or marker,
 * then it will add it to a special dialog.
 *
 * Created by epicstar on 12/17/14.
 */
public class RequestPredictions extends AsyncTask<String, Void, ETAContainer> {

//    private Marker marker;
    private Set<Integer> busIds;
    private Set<Integer> stopIds;
    private FragmentManager fragmentManager;
    private Context context;


    /**
     * Initializes the asynctask
     * @param busIds set of id of buses
     * @param stopIds set of id of bus stops
     * @param fragmentManager the fragment manager class from the activity
     * @param context the context of the activity
     */
    public RequestPredictions(Set<Integer> busIds, Set<Integer> stopIds, FragmentManager fragmentManager, Context context) {
//        this.marker = marker;
        this.busIds = busIds;
        this.stopIds = stopIds;
        this.fragmentManager = fragmentManager;
        this.context = context;
    }

    /**
     * This is the background thread...
     *
     * Makes sure that:
     * * we are looking at a stop or bus id
     * * get the info of the bus or stop id
     * * return this into an ETAContainer
     *
     * @param marker the title of the marker
     * @return an ETA Container that contains the dialog's title and message
     */
    @Override
    protected ETAContainer doInBackground(String... marker) {
//        System.out.println("marker title: " + marker[0]);
        String markerTitle = marker[0];
        String message = "";
//        String snippet = null;
        try {
            URL url = null;
            int id = Integer.parseInt(markerTitle.substring(markerTitle.indexOf("(") + 1, markerTitle.indexOf(")")));
            int sw = -1;
            if (busIds.contains(id)) {
                System.out.println("bus");
                url = PortAuthorityAPI.getBusPredictions(id);

                sw = 0; //looking at a bus id
            } else if (stopIds.contains(id)) {
                System.out.println("stop");
                url = PortAuthorityAPI.getStopPredictions(id);
                sw = 1; // we are looking at a stopID
            }
//            System.out.println(url);
//            Log.i("predictions_url", url.toString());
            if(url != null || sw != -1) {
                PredictionsXMLPullParser predictionsXMLPullParser = new PredictionsXMLPullParser(url, context);
                List<Prediction> predictions = predictionsXMLPullParser.createPredictionList();
                StringBuilder st = new StringBuilder();
                ConcurrentHashMap<String, StringBuilder> idTimes = new ConcurrentHashMap<>();
                if(predictions != null) {
                    for(Prediction prediction : predictions) {
                        StringBuilder addString = new StringBuilder(prediction.getPrdtm().split(" ")[1]);
                        System.out.println(addString);
                        int i = 0;
                        if(sw == 0) {
                            StringBuilder tempString = idTimes.putIfAbsent(prediction.getStpnm(), addString);
                            if(tempString != null) {
                                tempString.append("\t");
                                tempString.append(addString);
                            }
                            message = createMessage(idTimes, sw);
                            System.out.println(idTimes.get(prediction.getStpid()));
                        } else if (sw == 1) {
                            StringBuilder tempString = idTimes.putIfAbsent(prediction.getRt(), addString);
                            if(tempString != null) {
                                tempString.append("\t");
                                tempString.append(addString);
                                System.out.println(idTimes.get(prediction.getVid()));
                            }
                            message = createMessage(idTimes, sw);
                        }
                    }
                    System.out.println(message);
                    return new ETAContainer(markerTitle, message);
                }
            }
            

        } catch(MalformedURLException e) {
            Log.i("HELLO", e.getMessage());
        } catch (XmlPullParserException | IOException e) {
            Log.e("XML_ERROR", e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * TODO: improve this to be arranged by stop time especially when looking at a bus's ETAs probably will have to use a different structure for that case
     *
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
        for(Map.Entry<String, StringBuilder> info : idEntries) {
            if(i == 5 && sw == 0){ break; }
            i++;
            System.out.println("key: " + info.getKey());
            System.out.println("value " + info.getValue());
            st.append(info.getKey());
            st.append(":\n  ");
            st.append(info.getValue());
            st.append("\n");
        }

        return st.toString();
    }

    protected void onPostExecute(ETAContainer container) {
        if(container != null) {
            showDialog(container.getMessage(), container.getTitle());
//        if(snippet != null && snippet.length() > 0)
//            marker.setSnippet(snippet);
        }

    }

    public void showDialog(String message, String title) {
        BusInformationDialog busInfoDialog = new BusInformationDialog();
        busInfoDialog.setMessage(message);
        busInfoDialog.setTitle(title);
        busInfoDialog.setStyle(R.style.Base_Theme_AppCompat_Light_Dialog, 0);
        busInfoDialog.setCancelable(true);
        busInfoDialog.show(fragmentManager, "ETAs");
//        busInfoDialog.show();
    }
}
