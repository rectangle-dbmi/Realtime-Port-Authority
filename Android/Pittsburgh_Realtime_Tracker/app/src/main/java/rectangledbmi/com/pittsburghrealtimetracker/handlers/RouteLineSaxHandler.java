package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import com.google.android.gms.maps.model.LatLng;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by epicstar on 10/14/14.
 */
public class RouteLineSaxHandler extends TransitSAXHandler {

    private List<LatLng> points;
    private double tempLat;
    private double tempLong;

    public RouteLineSaxHandler() {
        points = new LinkedList<LatLng>();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if (qName.equals("lat"))
                tempLat = Double.parseDouble(content);
            else if (qName.equals("lon"))
                tempLong = Double.parseDouble(content);
            /*else if(qName.equals("typ")) {
                if(content.equals("S")) {

                }
                else if(qName.equals("W")) {

                }
            }*/
            else if (qName.equals("pt")) {
                System.out.println("("+tempLat + ", " + tempLong + ")");
                points.add(new LatLng(tempLat, tempLong));
            }
        } catch(NullPointerException e) {
            System.err.println("Somehow a waypoint wasn't added...");
        }

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

    }

    @Override
    protected void getMessage(String content) throws NullPointerException {

    }

    public List<LatLng> getPoints() {
        return points;
    }
}
