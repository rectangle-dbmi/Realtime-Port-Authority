package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;


/**
 * The Handler for SAX Events.
 */
class SAXHandler extends DefaultHandler {

    List<Bus> busList = new ArrayList<Bus>();
    Bus bus = null;
    String content = null;

    /**
     *
     */
    public SAXHandler() {
        super();
    }

    /**
     *
     * @param ch
     * @param start
     * @param length
     * @throws SAXException
     */
    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        content = String.copyValueOf(ch, start, length).trim();
    }

    /**
     *
     * @param uri
     * description Stuff
     * @param localName
     * description Stuff
     * @param qName
     * description Stuff
     * @throws SAXException
     */
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("vid")) {
            busList.add(bus);
            bus.setVid(content);

            // For all other end tags the bus has to be updated.
        } else if (qName.equals("lat")) {
            bus.setLat(content);

        } else if (qName.equals("lon")) {
            bus.setLon(content);

        } else if (qName.equals("msg")) {
            bus.setMsg(content);

        } else if (qName.equals("tmstmp")) {
            bus.setTmStmp(content);

        } else if (qName.equals("hdg")) {
            bus.setHdg(content);

        } else if (qName.equals("pid")) {
            bus.setPid(content);

        } else if (qName.equals("rt")) {
            bus.setRt(content);

        } else if (qName.equals("des")) {
            bus.setDes(content);

        } else if (qName.equals("pdist")) {
            bus.setPdist(content);

        } else if (qName.equals("spd")) {
            bus.setSpd(content);

        } else if (qName.equals("tablockid")) {
            bus.setTablockid(content);

        } else if (qName.equals("tatripid")) {
            bus.setTatripid(content);

        }

    }

    /**
     *
     * @param uri
     * @param localName
     * @param qName
     * @param attributes
     * @throws SAXException
     */
    @Override
    // Triggered when the start of tag is found.
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        if (qName.equals("vid")) {
            bus = new Bus();
            bus.setVid(content);

        }
    }

}