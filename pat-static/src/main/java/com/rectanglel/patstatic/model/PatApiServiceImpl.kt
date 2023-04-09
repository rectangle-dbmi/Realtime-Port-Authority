package com.rectanglel.patstatic.model

import com.google.gson.GsonBuilder
import com.rectanglel.patstatic.patterns.PatternDataManager
import com.rectanglel.patstatic.patterns.response.Pt
import com.rectanglel.patstatic.patterns.response.Ptr
import com.rectanglel.patstatic.predictions.response.Prd
import com.rectanglel.patstatic.predictions.response.PredictionResponse
import com.rectanglel.patstatic.routes.BusRoute
import com.rectanglel.patstatic.routes.RoutesDataManager
import com.rectanglel.patstatic.utils.Constants
import com.rectanglel.patstatic.vehicles.response.VehicleResponse
import com.rectanglel.patstatic.wrappers.WifiChecker
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Implemented service that retrieves data.
 * Created by epicstar on 9/18/16.
 * @author Jeremy Jao
 * @since 78
 */
class PatApiServiceImpl(apiKey: String,
                        dataDirectory: File,
                        staticData: StaticData,
                        wifiChecker: WifiChecker) : PatApiService {

    private val patApiClient: RetrofitPatApi

    private val patternDataManager: PatternDataManager

    private val routesDataManager: RoutesDataManager

    override val routes: Single<List<BusRoute>>
        get() = routesDataManager.routes

    init {

        patApiClient = createPatApiClient(apiKey)
        patternDataManager = PatternDataManager(dataDirectory, patApiClient, staticData, wifiChecker)
        routesDataManager = RoutesDataManager(dataDirectory, patApiClient, staticData)
    }

    override fun getPatterns(rt: String): Flowable<List<Ptr>> {
        return patternDataManager.getPatterns(rt)
                .compose(applySchedulers())
    }

    override fun getStops(rt: String): Flowable<List<Pt>> {
        throw NotImplementedError()
    }

    override fun getVehicles(routes: Collection<String>): Flowable<VehicleResponse> {
        return patApiClient.getVehicles(routes.joinToString(","))
                .compose(applySchedulers())
    }

    override fun getStopPredictions(stpid: Int, rts: Collection<String>): Single<List<Prd>> {
        return patApiClient.getStopPredictions(stpid, rts.joinToString(","))
                .compose(composePrds())
    }

    override fun getVehiclePredictions(vid: Int): Single<List<Prd>> {
        return patApiClient.getBusPredictions(vid)
                .compose(composePrds())

    }

    companion object {

        private const val baseUrl = "https://truetime.portauthority.org/bustime/api/v3/"

        /**
         * This is the date format to parse
         *
         * @since 46
         */
        private const val DATE_FORMAT_PARSE = "yyyyMMdd HH:mm"

        /**
         * The default date format to parse... The timezone is set as EST
         *
         * @since 46
         */
        private val DEFAULT_DATE_PARSE_FORMAT = SimpleDateFormat(DATE_FORMAT_PARSE, Locale.US)

        private fun composePrds(): (Single<PredictionResponse>) -> Single<List<Prd>> {
            return { predictionResponse ->
                predictionResponse
                        .map { it.bustimeResponse }
                        .map { it.prd }
                        .compose(applySchedulersSingle<List<Prd>>())
            }
        }

        // region helper methods
        private fun createPatApiClient(apiKey: String): RetrofitPatApi {
            // use a date converter
            val gson = GsonBuilder()
                    .setDateFormat(Constants.DATE_FORMAT_PARSE)
                    .create()

            val okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(5, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val originalHttpUrl = original.url()

                        val url = originalHttpUrl.newBuilder()
                                .addQueryParameter("key", apiKey)
                                .build()

                        // Request customization: add request headers
                        val requestBuilder = original.newBuilder()
                                .url(url)

                        val request = requestBuilder.build()
                        chain.proceed(request)
                    }
                    .build()
            // build the restadapter
            val retrofit = Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(okHttpClient)
                    .build()

            return retrofit.create(RetrofitPatApi::class.java)
        }

        /**
         * Sets IO above and computation schedulers below. This is fine because this class will only be mocked
         * in unit tests.
         * @param <T> any value
         * @return a transformer anonymous class
        </T> */
        private fun <T> applySchedulers(): (Flowable<T>) -> Flowable<T> {
            return { observable ->
                observable
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
            }
        }

        /**
         * Sets IO above and computation schedulers below. This is fine because this class will only be mocked
         * in unit tests.
         * @param <T> any value
         * @return a transformer anonymous class
        </T> */
        private fun <T> applySchedulersSingle(): (Single<T>) -> Single<T> {
            return { single ->
                single
                        .subscribeOn(Schedulers.io())
                        .observeOn(Schedulers.computation())
            }
        }
    }
    // endregion
}
