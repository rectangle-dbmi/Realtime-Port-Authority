package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.os.AsyncTask;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import rectangledbmi.com.pittsburghrealtimetracker.hidden.PortAuthorityAPI;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;

/**
 * Created by epicstar on 9/5/14.
 */
public class RequestRoutes extends AsyncTask<Void, Void, Map<String, Route>> {

    private Map<String, Route> routes;

    public RequestRoutes() {
        routes = null;
    }

    @Override
    protected Map<String, Route> doInBackground(Void... voids) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = null;
        try {
            sp = spf.newSAXParser();
        } catch(ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }

        URL url = null;

        try {
            url = PortAuthorityAPI.getRoutes();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        RouteSaxHandler handler;
        try {
            handler = new RouteSaxHandler();
            try {
                if(sp != null) {
                    sp.parse(new InputSource(url != null ? url.openStream() : null), handler);
                }
            } catch (SAXException | IOException e) {
                e.printStackTrace();
            }
            routes = handler.getRoutes();
        } catch (NullPointerException e) {
            System.out.println("Routes are not being added");
        }

        return routes;
    }
}
