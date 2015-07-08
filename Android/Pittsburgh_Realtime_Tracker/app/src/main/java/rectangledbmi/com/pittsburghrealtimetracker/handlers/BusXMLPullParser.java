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
import java.util.LinkedList;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;

/**
 * XML PullParser version to get the buses and put it in a list after execution;
 * Created by epicstar on 11/10/14.
 *
 * @author Jeremy Jao
 */
public class BusXMLPullParser {

    private List<Bus> busList;
    private URL url;
    XmlPullParser parser;
    Context context; //Application context passed in

    public BusXMLPullParser(URL url, Context context) throws XmlPullParserException {

        this.url = url;
        XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
        parser = pullParserFactory.newPullParser();
        busList = new LinkedList<>();
        this.context = context;
    }

    /**
     * Creates the bus list by starting the XMLPullParser
     * @return the list of buses
     * @throws IOException
     * @throws XmlPullParserException
     */
    public List<Bus> createBusList() throws IOException, XmlPullParserException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setUseCaches(false);
        InputStream in = conn.getInputStream();
        if(in != null) {
            parser.setInput(conn.getInputStream(), null);
            parseXML();
        } else {
            Toast.makeText(context, "Connection Timeout, Internet problem", Toast.LENGTH_LONG).show();
        }

        return getBusList();
    }

    /**
     *
     * @return the list of buses
     */
    public List<Bus> getBusList() {
        return busList;
    }

    /**
     * XMLPullParser implementation of getting buses from the Port Authority API
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void parseXML() throws XmlPullParserException, IOException {

        int eventType = parser.getEventType();
        Bus bus = new Bus();
        while(eventType != XmlPullParser.END_DOCUMENT) {
            String name = parser.getName();

            try {
                switch (eventType) {

                    case (XmlPullParser.START_TAG): {
                        if ("vehicle".equals(name)) { //new vehicle seen and make sure nothing else is there
                            bus = new Bus();
                        } else {
                            //below is to add a new vehicle
                            switch (name) {
                                case "vid":
                                    bus.setVid(parser.nextText());
                                    break;
                                case "tmstmp":
                                    bus.setTmStmp(parser.nextText());
                                    break;
                                case "lat":
                                    bus.setLat(parser.nextText());
                                    break;
                                case "lon":
                                    bus.setLon(parser.nextText());
                                    break;
                                case "hdg":
                                    bus.setHdg(parser.nextText());
                                    break;
                                case "pid":
                                    bus.setPid(parser.nextText());
                                    break;
                                case "rt":
                                    bus.setRt(parser.nextText());
                                    break;
                                case "des":
                                    bus.setDes(parser.nextText());
                                    break;
                                case "pdist":
                                    bus.setPdist(parser.nextText());
                                    break;
                                case "dly":
                                    bus.setDly(parser.nextText());
                                    break;
                                case "spd":
                                    bus.setSpd(parser.nextText());
                                    break;
                                case "tablockid":
                                    bus.setTablockid(parser.nextText());
                                    break;
                                case "tatripid":
                                    bus.setTatripid(parser.nextText());
                                    break;
                            }
                        }
                        break;
                    }
                    case (XmlPullParser.END_TAG): { //adds to new vehicle
                        if("vehicle".equals(name)) {
                            busList.add(bus);
                        }
                        break;
                    }

                }
            } catch(NullPointerException e) {
                System.err.println("Bus error'd...");
            }
            eventType = parser.next();
        }
    }

}
