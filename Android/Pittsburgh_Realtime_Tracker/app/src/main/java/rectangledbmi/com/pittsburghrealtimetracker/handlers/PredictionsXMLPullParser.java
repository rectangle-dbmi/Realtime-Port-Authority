package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.world.Prediction;

/**
 * Created by Ritwik Gupta on 12/17/14.
 */
public class PredictionsXMLPullParser {

    private List<Prediction> predictionList;
    private URL url;
    XmlPullParser parser;
    Context context; //Application context passed in

    public PredictionsXMLPullParser(URL url, Context context) throws XmlPullParserException {

        this.url = url;
        XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
        parser = pullParserFactory.newPullParser();
        this.context = context;
        predictionList = null;
    }

    /**
     * Creates the bus list by starting the XMLPullParser
     *
     * @return the list of predictions
     * @throws java.io.IOException
     * @throws XmlPullParserException
     */
    public List<Prediction> createPredictionList() throws IOException, XmlPullParserException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setConnectTimeout(5000);
        InputStream in = conn.getInputStream();
        if(in != null) {
            predictionList = new LinkedList<>();
            parser.setInput(conn.getInputStream(), null);
            parseXML();
        } else {
            Toast.makeText(context, "Connection Timeout, Internet problem", Toast.LENGTH_LONG).show();
        }
        return getPredictionList();
    }

    /**
     * @return the list of buses
     */
    public List<Prediction> getPredictionList() {
        return predictionList;
    }

    /**
     * XMLPullParser implementation of getting predictions from the Port Authority API
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void parseXML() throws XmlPullParserException, IOException {

        int eventType = parser.getEventType();
        Prediction prediction = new Prediction();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();

            try {
                switch (eventType) {

                    case (XmlPullParser.START_TAG): {
//                        if ("stpid".equals(name)) { //new vehicle seen and make sure nothing else is there
//                            prediction = new Prediction();
//                            prediction.setStpid(parser.nextText());
//                        } else {
                            //below is to add a new vehicle
                        Log.i("predictions_xml_start_tag", name);
                            switch (name) {
                                case "prd":
                                    prediction = new Prediction();
                                    break;
                                case "vid":
                                    prediction.setVid(parser.nextText());
                                    break;
                                case "tmstmp":
                                    prediction.setTmpstmp(parser.nextText());
                                    break;
                                case "rt":
                                    prediction.setRt(parser.nextText());
                                    break;
                                case "des":
                                    prediction.setDes(parser.nextText());
                                    break;
                                case "dly":
                                    prediction.setDly(parser.nextText());
                                    break;
                                case "tablockid":
                                    prediction.setTablockid(parser.nextText());
                                    break;
                                case "tatripid":
                                    prediction.setTatripid(parser.nextText());
                                    break;
                                case "typ":
                                    prediction.setTyp(parser.nextText());
                                    break;
                                case "stpnm":
                                    prediction.setStpnm(parser.nextText());
                                    break;
                                case "stpid":
                                    prediction.setStpid(parser.nextText());
                                    break;
                                case "dstp":
                                    prediction.setDstp(parser.nextText());
                                    break;
                                case "rtdir":
                                    prediction.setRtdir(parser.nextText());
                                    break;
                                case "prdtm":
                                    prediction.setPrdtm(parser.nextText());
                                    break;
                                case "prtctdn":
                                    prediction.setPrdctdn(parser.nextText());
                                    break;
//                            }
                        }
                        break;
                    }
                    case (XmlPullParser.END_TAG): { //adds to new vehicle
                        if ("prd".equals(name)) {
                            Log.i("prediction_object", prediction.toString());
                            predictionList.add(prediction);
                        }
                        break;
                    }

                }
            } catch (NullPointerException e) {
                Log.e("nullpointer_xml", e.getMessage());
            }
            eventType = parser.next();
        }
    }

}

