package handlers;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import world.Bus;

/**
 * Created by epicstar on 8/3/14.
 */
public class PortAuthorityRealtime {

    public List<Bus> returnBus() {

        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser sp = null;
        try {
            sp = spf.newSAXParser();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        URL url = null;
        try {
            url = new URL(
                    "http://realtime.portauthority.org/bustime/api/v2/getvehicles?key=KiJEdJUDgRFxcG7cpt3ae6xxJ&rt=P1"
            );
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SAXHandler handler = new SAXHandler();
        try {
            try {
                sp.parse(new InputSource(url.openStream()), handler);
            } catch (SAXException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (NullPointerException sax) {
            System.out
                    .println("Bus route is not tracked or all buses on route are in garage.");
            System.exit(0);
        }

        return handler.busList;
    }
}