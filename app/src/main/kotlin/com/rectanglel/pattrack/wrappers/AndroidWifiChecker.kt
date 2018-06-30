package com.rectanglel.pattrack.wrappers

import android.content.Context
import android.net.ConnectivityManager
import com.rectanglel.patstatic.wrappers.WifiChecker

/**
 * Android implementation of checking to see if wifi is on.
 *
 * Created by epicstar on 8/13/17.
 *
 * @author Jeremy Jao
 * @since 160080002
 */
class AndroidWifiChecker constructor(context: Context): WifiChecker {

    private val connectivityManager : ConnectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun isWifiOn(): Boolean {
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo.type == ConnectivityManager.TYPE_WIFI
    }

}