package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.content.Context;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
                        if ("stpid".equals(name)) { //new vehicle seen and make sure nothing else is there
                            prediction = new Prediction();
                            prediction.setStpid(parser.nextText());
                        } else {
                            //below is to add a new vehicle
                            if ("vid".equals(name)) {
                                prediction.setVid(parser.nextText());
                            } else if ("tmstmp".equals(name)) {
                                prediction.setTmpstmp(parser.nextText());
                            } else if ("rt".equals(name)) {
                                prediction.setRt(parser.nextText());
                            } else if ("des".equals(name)) {
                                prediction.setDes(parser.nextText());
                            } else if ("dly".equals(name)) {
                                prediction.setDly(parser.nextText());
                            } else if ("tablockid".equals(name)) {
                                prediction.setTablockid(parser.nextText());
                            } else if ("tatripid".equals(name)) {
                                prediction.setTatripid(parser.nextText());
                            } else if ("typ".equals(name)) {
                                prediction.setTyp(parser.nextText());
                            } else if ("stpnm".equals(name)) {
                                prediction.setStpnm(parser.nextText());
                            } else if ("dstp".equals(name)) {
                                prediction.setDstp(parser.nextText());
                            } else if ("rtdir".equals(name)) {
                                prediction.setRtdir(parser.nextText());
                            } else if ("prdtm".equals(name)) {
                                prediction.setPrdtm(parser.nextText());
                            } else if ("prtctdn".equals(name)) {
                                prediction.setPrdctdn(parser.nextText());
                            }
                        }
                        break;
                    }
                    case (XmlPullParser.END_TAG): { //adds to new vehicle
                        if ("stpid".equals(name)) {
                            predictionList.add(prediction);
                        }
                        break;
                    }

                }
            } catch (NullPointerException e) {
                System.err.println("Prediction get error.");
            }
            eventType = parser.next();
        }
    }

}

