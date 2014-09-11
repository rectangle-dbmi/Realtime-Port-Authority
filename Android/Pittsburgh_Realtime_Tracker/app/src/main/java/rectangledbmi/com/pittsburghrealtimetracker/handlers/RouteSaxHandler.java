package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

import rectangledbmi.com.pittsburghrealtimetracker.world.Route;

/**
 * Created by epicstar on 9/5/14.
 */
public class RouteSaxHandler extends TransitSAXHandler {
    private HashMap<String, Route> routes;
    private Route route;

    public RouteSaxHandler() {
        super();
        routes = new HashMap<String, Route>();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        try {
            if(qName.equals("rt")) {
                routes.put(route.getRoute(), route);
                route.setRoute(content);
            }
            else if(qName.equals("rtnm")) {
                route.setRouteInfo(content);
            }
            else if(qName.equals("rtclr")) {
                route.setRouteColor(content);
            }
            else if(qName.equals("msg")) {
                getMessage(content);
            }
        } catch(NullPointerException e) {

        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            if(qName.equals("rt")) {
                route = new Route(content);
            }
        } catch(NullPointerException e) {

        }
    }

    @Override
    protected void getMessage(String content) throws NullPointerException {
        System.out.println("Message: " + content);
    }

    public Map<String, Route> getRoutes() {
        return routes;
    }
}
