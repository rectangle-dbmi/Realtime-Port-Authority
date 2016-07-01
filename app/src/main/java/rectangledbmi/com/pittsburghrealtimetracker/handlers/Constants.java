package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Locale;

import rectangledbmi.com.pittsburghrealtimetracker.ui.activities.SelectTransit;

/**
 * Global constants available to everyone
 *
 * @author Jeremy Jao
 * @since 46
 */
public class Constants {

    /**
     * This is the date format to print
     *
     * @since 46
     */
    public final static String DATE_FORMAT_PRINT = "hh:mm a";

    /**
     * This is the date format to parse
     *
     * @since 46
     */
    public final static String DATE_FORMAT_PARSE = "yyyyMMdd HH:mm";

    /**
     * The default date format to parse... The timezone is set as EST in
     * {@link SelectTransit#onCreate(Bundle)}
     * @since 46
     */
    public final static SimpleDateFormat DEFAULT_DATE_PARSE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PARSE, Locale.US);


}
