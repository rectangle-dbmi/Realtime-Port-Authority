package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;

/**
 * XML PullParser version to get the buses and put it in a list after execution;
 * Created by epicstar on 11/10/14.
 */
public class BusXMLPullParser {

    private List<Bus> busList;
    private URL url;
    XmlPullParser parser;

    public BusXMLPullParser(URL url) throws XmlPullParserException {

        this.url = url;
        XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
        parser = pullParserFactory.newPullParser();
        busList = new LinkedList<>();
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
        parser.setInput(conn.getInputStream(), null);
        parseXML();
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
                            if ("vid".equals(name)) {
                                bus.setVid(parser.nextText());
                            } else if ("tmstmp".equals(name)) {
                                bus.setTmStmp(parser.nextText());
                            } else if ("lat".equals(name)) {
                                bus.setLat(parser.nextText());
                            } else if ("lon".equals(name)) {
                                bus.setLon(parser.nextText());
                            } else if ("hdg".equals(name)) {
                                bus.setHdg(parser.nextText());
                            } else if ("pid".equals(name)) {
                                bus.setPid(parser.nextText());
                            } else if ("rt".equals(name)) {
                                bus.setRt(parser.nextText());
                            } else if ("des".equals(name)) {
                                bus.setDes(parser.nextText());
                            } else if ("pdist".equals(name)) {
                                bus.setPdist(parser.nextText());
                            } else if ("dly".equals(name)) {
                                bus.setDly(parser.nextText());
                            } else if ("spd".equals(name)) {
                                bus.setSpd(parser.nextText());
                            } else if ("tablockid".equals(name)) {
                                bus.setTablockid(parser.nextText());
                            } else if ("tatripid".equals(name)) {
                                bus.setTatripid(parser.nextText());
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
