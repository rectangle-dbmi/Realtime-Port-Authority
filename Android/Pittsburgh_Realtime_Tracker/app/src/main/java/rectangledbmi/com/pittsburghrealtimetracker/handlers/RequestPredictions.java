package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.app.FragmentManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
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
    private GoogleMap mMap;
    private Marker marker;
    private Set<Integer> busIds;
    private Set<Integer> stopIds;
    private Set<String> selectedBuses;
    private FragmentManager fragmentManager;
    private Context context;


    /**
     * Initializes the asynctask
     * @param busIds set of id of buses
     * @param stopIds set of id of bus stops
     * @param fragmentManager the fragment manager class from the activity
     * @param context the context of the activity
     */
    public RequestPredictions(GoogleMap mMap,
                              Marker marker,
                              Set<Integer> busIds, Set<Integer> stopIds,
                              FragmentManager fragmentManager,
                              Set<String> selectedBuses,
                              Context context) {
        this.marker = marker;
        this.mMap = mMap;
        this.busIds = busIds;
        this.stopIds = stopIds;
        this.selectedBuses = selectedBuses;
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
     * @return an ETA Container that contains the dialog's title and message
     */
    @Override
    protected ETAContainer doInBackground(String... params) {
//        System.out.println("marker title: " + marker[0]);
        String markerTitle = params[0];
        String message = "";
//        String snippet = null;
        try {
            URL url = null;
            int id = Integer.parseInt(markerTitle.substring(markerTitle.indexOf("(") + 1, markerTitle.indexOf(")")));
            int sw = -1;
            if (busIds.contains(id)) {
                Log.i("prediction_type", "bus");
                url = PortAuthorityAPI.getBusPredictions(id);

                sw = 0; //looking at a bus id
            } else if (stopIds.contains(id)) {
                Log.i("prediction_type", "stop");
                url = PortAuthorityAPI.getStopPredictions(id, selectedBuses);
                sw = 1; // we are looking at a stopID
            }
//            System.out.println(url);
//            Log.i("predictions_url", url.toString());
            if(url != null || sw != -1) {
                PredictionsXMLPullParser predictionsXMLPullParser = new PredictionsXMLPullParser(url, context);
                List<Prediction> predictions = predictionsXMLPullParser.createPredictionList();
                StringBuilder st = new StringBuilder();
                ConcurrentHashMap<String, StringBuilder> busPredictions = new ConcurrentHashMap<>();
                LinkedList<String> stopPredictions = new LinkedList<>();

                if(predictions != null) {
                    int i = 0;
                    for(Prediction prediction : predictions) {
                        Log.i("time", prediction.getPrdtm().split(" ")[1]);
                        SimpleDateFormat date = new SimpleDateFormat("hh:mm a");

                        StringBuilder addString = new StringBuilder(date.format(new SimpleDateFormat("HH:mm").parse(prediction.getPrdtm().split(" ")[1])));
//                        System.out.println(addString);

                        if(sw == 0) { // bus dialog that displays stops
                          /*  StringBuilder tempString = busPredictions.putIfAbsent(prediction.getStpnm(), addString);
                            if(tempString != null) {
                                tempString.append("\t");
                                tempString.append(addString);
                            }*/
                            stopPredictions.add("(" + prediction.getStpid() + ")" + prediction.getStpnm() + ":\t" + addString);
                            if(++i == 5)
                                break;
//                            message = createMessage(busPredictions, sw);
//                            System.out.println(busPredictions.get(prediction.getStpid()));
                        } else if (sw == 1) { // stop dialog that displays routes
                            StringBuilder tempString = busPredictions.putIfAbsent(prediction.getRt(), addString);
                            if(tempString != null) {
                                tempString.append("\t");
                                tempString.append(addString);
//                                System.out.println(busPredictions.get(prediction.getVid()));
                            }
//                            message = createMessage(busPredictions, sw);
                        }
                    }
                    if(sw == 0) {
                        message = createMessage(stopPredictions);
                    } else if(sw == 1) {
                        message = createMessage(busPredictions, sw);
                    }
//                    System.out.println(message);
                    return new ETAContainer(markerTitle, message);
                }
            }
            

        } catch(MalformedURLException e) {
            Log.i("HELLO", e.getMessage());
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
//            System.out.println("key: " + info.getKey());
//            System.out.println("value " + info.getValue());
            st.append(info.getKey());
            st.append(":\n  ");
            st.append(info.getValue());
            st.append("\n");
        }

        return st.toString();
    }

    public String createMessage(LinkedList<String> stringLinkedList) {
        StringBuilder st = new StringBuilder();

        for(String string : stringLinkedList) {
            st.append(string);
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
        busInfoDialog.setStyle(R.style.Base_Theme_AppCompat_Light_Dialog, 10);
        busInfoDialog.setCancelable(true);
        busInfoDialog.show(fragmentManager, "ETAs");
//        busInfoDialog.show();
    }
}
