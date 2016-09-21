package rectangledbmi.com.pittsburghrealtimetracker.model;

import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import rectangledbmi.com.pittsburghrealtimetracker.handlers.Constants;
import rectangledbmi.com.pittsburghrealtimetracker.retrofit.patapi.PATAPI;
import rectangledbmi.com.pittsburghrealtimetracker.world.Prediction;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Pt;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Ptr;
import rectangledbmi.com.pittsburghrealtimetracker.world.jsonpojo.Vehicle;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Implemented service that retrieves data.
 * Created by epicstar on 9/18/16.
 * @author Jeremy Jao
 * @since 77
 */
public class PatApiServiceImpl implements PatApiService {
    /**
     * This is the date format to parse
     *
     * @since 46
     */
    private final static String DATE_FORMAT_PARSE = "yyyyMMdd HH:mm";

    /**
     * The default date format to parse... The timezone is set as EST in
     * {@link rectangledbmi.com.pittsburghrealtimetracker.SelectTransit#onCreate(Bundle)}
     *
     * @since 46
     */
    @SuppressWarnings("unused")
    private final static SimpleDateFormat DEFAULT_DATE_PARSE_FORMAT = new SimpleDateFormat(DATE_FORMAT_PARSE, Locale.US);

    private final PATAPI patApiClient;

    private final PolylineDataManager polylineDataManager;

    public PatApiServiceImpl(String baseUrl,
                             String apiKey,
                             File dataDirectory) {

        patApiClient = createPatApiClient(baseUrl, apiKey);
        polylineDataManager = new PolylineDataManager(dataDirectory, patApiClient);
    }

    @Override
    public Observable<List<Ptr>> getPatterns(String rt) {
        return polylineDataManager.getPatterns(rt);
    }

    @Override
    public Observable<List<Pt>> getStops(String rt) {
        return null;
    }

    @Override
    public Observable<Vehicle> getVehicles(Iterable<String> routes) {
        return null;
    }

    @Override
    public Observable<Prediction> getStopPredictions(int stpid, String... rts) {
        return null;
    }

    @Override
    public Observable<Prediction> getVehiclePredictions(int vid) {
        return null;
    }

    public PATAPI getPatApiClient() {
        return patApiClient;
    }

    // region helper methods
    private static PATAPI createPatApiClient(String baseUrl, String apiKey) {
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

        return retrofit.create(PATAPI.class);
    }
    // endregion
}
