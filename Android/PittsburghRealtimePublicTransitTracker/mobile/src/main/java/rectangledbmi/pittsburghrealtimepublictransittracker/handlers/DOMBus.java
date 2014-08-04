package rectangledbmi.pittsburghrealtimepublictransittracker.handlers;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import world.Bus;

/**
 * Created by epicstar on 8/3/14.
 */
public class DOMBus {

    public DOMBus() {

    }

    public List<Bus> setVehicles(String URLel, String route) {

        List<Bus> vehicles = null;
        URL url;
        try {
            url = new URL(URLel + route);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(url.openStream()));
            Node vehicle = doc.getDocumentElement().getFirstChild().getNextSibling();
            Node vehicleinfo = null;
            Bus currentBus = null;
            int index = 0;
            vehicles = new ArrayList<Bus>();


            while(vehicle != null) {
                System.out.println(vehicle.getNodeName() + "\n");
                vehicleinfo = vehicle.getFirstChild().getNextSibling();

                System.out.println(index);
                currentBus = new Bus();
                while(vehicleinfo != null) {
                    addToVehicle(vehicleinfo, currentBus);
                    vehicleinfo = vehicleinfo.getNextSibling().getNextSibling();
                }
                ++index;
                vehicles.add(currentBus);
                vehicle = vehicle.getNextSibling().getNextSibling();
            }


        } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return vehicles;
    }

    public void addToVehicle(Node vehicleinfo, Bus bus) {
        String add = vehicleinfo.getTextContent();
        System.out.println(vehicleinfo.getNodeName() + ": " + add);

        String s = vehicleinfo.getNodeName();
        if (s.equals("vid")) {
            bus.setVid(add);
        } else if (s.equals("tmstmp")) {
            bus.setTmStmp(add);
        } else if (s.equals("lat")) {
            bus.setLat(add);
        } else if (s.equals("lon")) {
            bus.setLon(add);
        } else if (s.equals("hdg")) {
            bus.setHdg(add);
        } else if (s.equals("pid")) {
            bus.setPid(add);
        } else if (s.equals("rt")) {
            bus.setRt(add);
        } else if (s.equals("des")) {
            bus.setDes(add);
        } else if (s.equals("pdist")) {
            bus.setPdist(add);
        } else if (s.equals("dly")) {
            bus.setDly(add);
        } else if (s.equals("spd")) {
            bus.setSpd(add);
        } else if (s.equals("tablockid")) {
            bus.setTablockid(add);
        } else if (s.equals("tatripid")) {
            bus.setTatripid(add);
        }
    }

//    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
//        DOMBus doc = new DOMBus();
//
//        List<Bus> bus = doc.setVehicles("http://realtime.portauthority.org/bustime/api/v2/getvehicles?key=KiJEdJUDgRFxcG7cpt3ae6xxJ&rt=", "P1");
//        System.out.println(bus);
//
//    }
}

