package com.rectanglel.patstatic.buildutils

import com.rectanglel.patstatic.wrappers.WifiChecker

/**
 * Stub for always returning true to check for wifi. Used during the build step of the Android app.
 *
 * Created by epicstar on 8/19/17.
 * @author Jeremy Jao
 */
class StubWifiDataChecker : WifiChecker {
    override fun isWifiOn(): Boolean = true
}