package rectangledbmi.com.pittsburghrealtimetracker.model;

import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.world.Prediction;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Pt;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Ptr;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Vehicle;
import rx.Observable;

/**
 * <p>Service interface to get data from the Port Authority Trueime API and process it to be useable.</p>
 * <p>Created by epicstar on 9/18/16.</p>
 * @author Jeremy Jao
 * @since 77
 */
public interface PatApiService {
    @SuppressWarnings("unused")
    Observable<Vehicle> getVehicles(Iterable<String> rts);

    @SuppressWarnings("unused")
    Observable<Prediction> getVehiclePredictions(int id);

    @SuppressWarnings("unused")
    Observable<Prediction> getStopPredictions(int id, String... rts);

    Observable<List<Ptr>> getPatterns(String rt);

    Observable<List<Pt>> getStops(String rt);
}
