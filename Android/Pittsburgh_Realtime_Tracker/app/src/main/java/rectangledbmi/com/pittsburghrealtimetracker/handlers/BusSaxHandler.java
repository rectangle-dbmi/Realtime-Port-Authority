package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;

/**
 * @author epicstar
 * Created by epicstar on 9/5/14.
 */
public class BusSaxHandler extends TransitSAXHandler {

    private List<Bus> busList;
    private String rt = null;
    private Bus bus = null;

    public BusSaxHandler() {
        super();
        busList = new ArrayList<Bus>();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        try {
            if (qName.equals("vid")) {
                busList.add(bus);
                bus.setVid(content);

            } else if (qName.equals("lat")) {
                bus.setLat(content);

            } else if (qName.equals("lon")) {
                bus.setLon(content);

            } else if (qName.equals("tmstmp")) {
                bus.setTmStmp(content);

            } else if (qName.equals("hdg")) {
                bus.setHdg(content);

            } else if (qName.equals("pid")) {
                bus.setPid(content);

            } else if (qName.equals("rt")) {
                if(bus != null)
                    bus.setRt(content);
                rt = content;

            } else if (qName.equals("msg")) {
                getMessage(content);

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

            } else if (qName.equals("dly")) {
                bus.setDly(content);
            }
        } catch(NullPointerException e) {
            System.err.println(e.getMessage());
        }
    }

    public void startElement(String uri, String localName, String qName,
                 Attributes attributes) throws SAXException {
        try {
            if (qName.equals("vid")) {
                bus = new Bus();
                bus.setVid(content);

            }
        } catch(NullPointerException e) {
            System.err.println(e.getMessage());
        }
    }

    protected void getMessage(String content) throws NullPointerException {
        if(content.equals("No data found for parameter"))
            System.out.println(rt + " is not being tracked");
        else
            bus.setMsg(content);
    }

    public List<Bus> getBusList() {
        return busList;
    }
}
