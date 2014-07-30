import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class Bus {

	String vid;
	String lat;
	String lon;

	public float getLat() {
		return Float.parseFloat(lat);
	}

	public float getLon() {
		return Float.parseFloat(lon);
	}

	public int getVid() {
		return Integer.parseInt(vid);
	}

	@Override
	public String toString() {
		return vid + "\t" + lat + "\t"
				+ lon;
	}
}

public class PortAuthorityRealtime {

	public static void main(String[] args) throws Exception {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = spf.newSAXParser();
		URL url = new URL(
				"http://realtime.portauthority.org/bustime/api/v1/getvehicles?key="
						+ args[0] + "&rt=" + args[1]);
		SAXHandler handler = new SAXHandler();
		sp.parse(new InputSource(url.openStream()), handler);

		for (Bus bus : handler.busList) {
			System.out.println(bus.getVid() + "\t" + bus.getLat() + "\t"
					+ bus.getLon());
		}
	}

}

/**
 * The Handler for SAX Events.
 */
class SAXHandler extends DefaultHandler {

	List<Bus> busList = new ArrayList<>();
	Bus bus = null;
	String content = null;

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		content = String.copyValueOf(ch, start, length).trim();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		switch (qName) {
		// Add the employee to list once end tag is found
		case "vid":
			busList.add(bus);
			bus.vid = content;
			break;
		// For all other end tags the employee has to be updated.
		case "lat":
			bus.lat = content;
			break;
		case "lon":
			bus.lon = content;
			break;
		}
	}

	@Override
	// Triggered when the start of tag is found.
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		switch (qName) {
		// Create a new Employee object when the start tag is found
		case "vid":
			bus = new Bus();
			bus.vid = attributes.getValue("id");
			break;
		}
	}

}
