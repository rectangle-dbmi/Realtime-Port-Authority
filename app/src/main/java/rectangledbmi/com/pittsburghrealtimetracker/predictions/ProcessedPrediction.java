package rectangledbmi.com.pittsburghrealtimetracker.predictions;

import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Prd;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Pt;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Vehicle;

/**
 * <p>Holds prediction info.</p>
 * <p>Created by epicstar on 10/14/16.</p>
 * @author Jeremy Jao
 */

class ProcessedPrediction {
    private final String title;
    private final String predictions;

    /**
     * This is the date format to print
     *
     * @since 46
     */
    private final static String DATE_FORMAT_PRINT = "hh:mm a";

    /**
     * The default date format to parse... The timezone is set as EST in
     * {@link rectangledbmi.com.pittsburghrealtimetracker.SelectTransit#onCreate(Bundle)}
     * @since 46
     */
    private final static SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PRINT, Locale.US);

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("EST"));
    }

    static ProcessedPrediction create(PredictionType predictionType, List<Prd> predictions) {
        return new ProcessedPrediction(predictionType, predictions);
    }

    private ProcessedPrediction(PredictionType predictionType, List<Prd> predictions) {
        this.title = predictionType.getTitle();
        this.predictions = processPrds(predictionType, predictions);
    }

    private static String processPrds(PredictionType predictionType, List<Prd> prds) {
        StringBuilder st = new StringBuilder();
        boolean isFirst = true;
        for (Prd prd : prds) {
            if (isFirst) {
                isFirst = false;
            } else {
                st.append("\n");
            }
            if (predictionType instanceof Pt) {
                st.append(String.format("%s (%s): %s",
                    prd.getRt(), prd.getVid(), dateFormat.format(prd.getPrdtm()))
                );
            } else if (predictionType instanceof Vehicle) {
                st.append(String.format("(%s) %s: %s",
                        prd.getStpid(), prd.getStpnm(), dateFormat.format(prd.getPrdtm()))
                );
            }
        }
        return st.toString().length() > 0 ? st.toString() : "No predictions available.";
    }

    public String getTitle() {
        return title;
    }

    public String getPredictions() {
        return predictions;
    }
}
