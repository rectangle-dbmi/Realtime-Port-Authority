package com.rectanglel.patstatic.model;

import com.rectanglel.patstatic.patterns.response.Pt;
import com.rectanglel.patstatic.patterns.response.Ptr;
import com.rectanglel.patstatic.predictions.response.Prd;
import com.rectanglel.patstatic.routes.BusRoute;
import com.rectanglel.patstatic.vehicles.response.VehicleResponse;

import java.util.Collection;
import java.util.List;

import rx.Observable;
import rx.Single;

/**
 * <p>Service interface to get data from the Port Authority Trueime API and process it to be useable.</p>
 * <p>Created by epicstar on 9/18/16.</p>
 * @author Jeremy Jao
 * @since 78
 */
public interface PatApiService {
    Observable<VehicleResponse> getVehicles(Collection<String> rts);

    Single<List<Prd>> getVehiclePredictions(int id);

    Single<List<Prd>> getStopPredictions(int id, Collection<String> rts);

    Observable<List<Ptr>> getPatterns(String rt);

    @SuppressWarnings("unused")
    Observable<List<Pt>> getStops(String rt);

    Observable<List<BusRoute>> getRoutes();
}
