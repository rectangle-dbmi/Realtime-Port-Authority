package com.rectanglel.patstatic.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rectanglel.patstatic.patterns.PatternDataManager;
import com.rectanglel.patstatic.patterns.response.Pt;
import com.rectanglel.patstatic.patterns.response.Ptr;
import com.rectanglel.patstatic.predictions.response.BustimePredictionResponse;
import com.rectanglel.patstatic.predictions.response.Prd;
import com.rectanglel.patstatic.predictions.response.PredictionResponse;
import com.rectanglel.patstatic.routes.BusRoute;
import com.rectanglel.patstatic.utils.Constants;
import com.rectanglel.patstatic.vehicles.response.VehicleResponse;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

/**
 * Implemented service that retrieves data.
 * Created by epicstar on 9/18/16.
 * @author Jeremy Jao
 * @since 78
 */
public class PatApiServiceImpl implements PatApiService {
    /**
     * This is the date format to parse
     *
     * @since 46
     */
    private final static String DATE_FORMAT_PARSE = "yyyyMMdd HH:mm";

    /**
     * The default date format to parse... The timezone is set as EST
     *
     * @since 46
     */
    @SuppressWarnings("unused")
    private final static SimpleDateFormat DEFAULT_DATE_PARSE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PARSE, Locale.US);

    private final RetrofitPatApi patApiClient;

    private final PatternDataManager patternDataManager;

    public PatApiServiceImpl(String baseUrl,
                             String apiKey,
                             File dataDirectory) {

        patApiClient = createPatApiClient(baseUrl, apiKey);
        patternDataManager = new PatternDataManager(dataDirectory, patApiClient);
    }

    @Override
    public Observable<List<Ptr>> getPatterns(String rt) {
        return patternDataManager.getPatterns(rt)
                .compose(applySchedulers());
    }

    @Override
    public Observable<List<Pt>> getStops(String rt) {
        return null;
    }

    @Override
    public Observable<List<BusRoute>> getRoutes() {
        return null;
    }

    @Override
    public Observable<VehicleResponse> getVehicles(Collection<String> routes) {
        return patApiClient.getVehicles(collectionToString(routes))
                .compose(applySchedulers());
    }

    @Override
    public Single<List<Prd>> getStopPredictions(int stpid, Collection<String> rts) {
        return patApiClient.getStopPredictions(stpid, collectionToString(rts))
                .compose(composePrds());
    }

    @Override
    public Single<List<Prd>> getVehiclePredictions(int vid) {
        return patApiClient.getBusPredictions(vid)
                .compose(composePrds());

    }

    private static Single.Transformer<PredictionResponse, List<Prd>> composePrds() {
        return predictionRespose -> predictionRespose
                .map(PredictionResponse::getBustimeResponse)
                .map(BustimePredictionResponse::getPrd)
                .compose(applySchedulersSingle());
    }

    // region helper methods
    private static RetrofitPatApi createPatApiClient(String baseUrl, String apiKey) {
        // use a date converter
        Gson gson = new GsonBuilder()
                .setDateFormat(Constants.DATE_FORMAT_PARSE)
                .create();

        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .addInterceptor((chain) -> {
                    Request original = chain.request();
                    HttpUrl originalHttpUrl = original.url();

                    HttpUrl url = originalHttpUrl.newBuilder()
                            .addQueryParameter("key", apiKey)
                            .build();

                    // Request customization: add request headers
                    Request.Builder requestBuilder = original.newBuilder()
                            .url(url);

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                })
                .build();
        // build the restadapter
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient)
                .build();

        return retrofit.create(RetrofitPatApi.class);
    }

    /**
     * Sets IO above and computation schedulers below. This is fine because this class will only be mocked
     * in unit tests.
     * @param <T> any value
     * @return a transformer anonymous class
     */
    private static <T> Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation());
    }

    /**
     * Sets IO above and computation schedulers below. This is fine because this class will only be mocked
     * in unit tests.
     * @param <T> any value
     * @return a transformer anonymous class
     */
    private static <T> Single.Transformer<T, T> applySchedulersSingle() {
        return single -> single
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation());
    }

    /**
     * @param data - the data in a collection to add
     * @param <T>  - Any Object that extends {@link Object}
     * @return a comma-delim strings of data
     * @since 46
     */
    private <T> String collectionToString(Collection<T> data) {
        int size = data.size();
        int i = 0;
        StringBuilder buf = new StringBuilder();
        for (T datum : data) {
            buf.append(datum);
            if (++i < size)
                buf.append(',');
        }
        return buf.toString();
    }
    // endregion
}
