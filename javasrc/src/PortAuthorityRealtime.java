import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
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
	String msg;
	String tmpstmp;
	String hdg;
	String pid;
	String rt;
	String des;
	String pdist;
	String spd;
	String tablockid;
	String tatripid;

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
		return vid + "\t" + lat + "\t" + lon + "\t" + tmpstmp + "\t" + hdg
				+ "\t" + pid + "\t" + rt + "\t" + des + "\t" + pdist + "\t"
				+ spd + "\t" + tablockid + "\t" + tatripid;
	}
}

public class PortAuthorityRealtime extends TimerTask {

	public static String[] argums;

	public static void main(String[] args) throws Exception {
		PortAuthorityRealtime.argums = args;
		Timer timer = new Timer();
		timer.schedule(new PortAuthorityRealtime(), 0, 10000);
	}

	@Override
	public void run() {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		SAXParser sp = null;
		try {
			sp = spf.newSAXParser();
		} catch (ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		URL url = null;
		try {
			url = new URL(
					"http://realtime.portauthority.org/bustime/api/v1/getvehicles?key="
							+ argums[0] + "&rt=" + argums[1]);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SAXHandler handler = new SAXHandler();
		try {
			try {
				sp.parse(new InputSource(url.openStream()), handler);
			} catch (SAXException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (NullPointerException sax) {
			System.out
			.println("Bus route is not tracked or all buses on route are in garage.");
			System.exit(0);
		}

		for (Bus bus : handler.busList) {
			System.out.println(bus.toString());
		}
		System.out.println();
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
		// Add the bus to list once end tag is found
		case "vid":
			busList.add(bus);
			bus.vid = content;
			break;
			// For all other end tags the bus has to be updated.
		case "lat":
			bus.lat = content;
			break;
		case "lon":
			bus.lon = content;
			break;
		case "msg":
			bus.msg = content;
			break;
		case "tmpstmp":
			bus.tmpstmp = content;
			break;
		case "hdg":
			bus.hdg = content;
			break;
		case "pid":
			bus.pid = content;
			break;
		case "rt":
			bus.rt = content;
			break;
		case "des":
			bus.des = content;
			break;
		case "pdist":
			bus.pdist = content;
			break;
		case "spd":
			bus.spd = content;
			break;
		case "tablockid":
			bus.tablockid = content;
			break;
		case "tatripid":
			bus.tatripid = content;
			break;
		}
	}

	@Override
	// Triggered when the start of tag is found.
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		switch (qName) {
		// Create a new Bus object when the start tag is found
		case "vid":
			bus = new Bus();
			bus.vid = attributes.getValue("id");
			break;
		}
	}

}
