package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.world.Bus;


/**
 * The Abstract Handler for SAX Events for our transit app.
 */

public abstract class TransitSAXHandler extends DefaultHandler {
    //TODO want to update points as opposed to clearing the map, consider hashmap
    protected String content = null;

//    Context context;

    /**
     *
     */
    public TransitSAXHandler() {
        super();
    }

/*    public SAXHandler(Context context) {
        super();
        this.context = context;
    }*/
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

    protected String getContent() {
        return content;
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
    public abstract void endElement(String uri, String localName, String qName)
            throws SAXException;

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
    public abstract void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException;

    /**
     * if qName == msg, use this to get the message contents
     * @param content the content of the message
     * @throws java.lang.NullPointerException
     */
    protected abstract void getMessage(String content) throws NullPointerException;

}