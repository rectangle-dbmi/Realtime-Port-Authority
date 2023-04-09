package rectangledbmi.com.pittsburghrealtimetracker.ui

import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast

import com.rectanglel.patstatic.model.PatApiService
import com.rectanglel.patstatic.model.PatApiServiceImpl
import com.rectanglel.pattrack.wrappers.AndroidWifiChecker

import java.io.File
import java.util.Calendar

import io.reactivex.Flowable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import rectangledbmi.com.pittsburghrealtimetracker.BuildConfig
import rectangledbmi.com.pittsburghrealtimetracker.R
import rectangledbmi.com.pittsburghrealtimetracker.selection.NotificationMessage
import rectangledbmi.com.pittsburghrealtimetracker.selection.Route
import rectangledbmi.com.pittsburghrealtimetracker.ui.about.AboutActivity
import rectangledbmi.com.pittsburghrealtimetracker.ui.selection.NavigationDrawerFragment
import rectangledbmi.com.pittsburghrealtimetracker.ui.selection.SelectionFragment
import rectangledbmi.com.pittsburghrealtimetracker.wrappers.AssetManagerStaticData
import timber.log.Timber

/**
 * This is the main activity of the Realtime Tracker...
 */
class MainActivity : AppCompatActivity(), SelectionFragment.BusSelectionInteraction {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private var mNavigationDrawerFragment: NavigationDrawerFragment? = null

    /**
     * Fragment that contains data from navigation drawer
     */
    private var selectionFragment: SelectionFragment? = null

    /**
     * Handles retrieving Port Authority data from the internet
     * @since 78
     */
    override var patApiService: PatApiService? = null
        private set

    /**
     * Reference to the main activity's Layout.
     *
     * @since 55
     */
    private var mainLayout: DrawerLayout? = null

    /**
     * subject to show toast messages.
     * @since 70
     */
    private var toastSubject: PublishSubject<NotificationMessage>? = null


    override val selectedRoutes: Set<String?>?
        get() = mNavigationDrawerFragment?.selectedRoutes

    override val datadirectory: File
        get() = filesDir

    override val selectedRoutesObservable: Flowable<Set<String?>?>?
        get() = mNavigationDrawerFragment?.selectedRoutesObservable

    override val toggledRouteObservable: Flowable<Route>?
        get() = mNavigationDrawerFragment?.toggledRouteObservable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restoreActionBar()


        buildPATAPI()
        setContentView(R.layout.activity_select_transit)
        val fragmentManager = supportFragmentManager
        mNavigationDrawerFragment = fragmentManager.findFragmentById(R.id.navigation_drawer) as NavigationDrawerFragment
        checkSDCardData()
        // Set up the drawer.
        mNavigationDrawerFragment?.setUp(
                R.id.navigation_drawer,
                findViewById<View>(R.id.drawer_layout) as DrawerLayout)

        mainLayout = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        selectionFragment = fragmentManager.findFragmentById(R.id.map) as SelectionFragment

        // create a publish subject for displaying toasts
        toastSubject = PublishSubject.create()
        toastSubject
                ?.subscribeOn(Schedulers.io())
                ?.observeOn(AndroidSchedulers.mainThread())
                ?.subscribe(toastMessageObserver())

        enableHttpResponseCache()
    }

    /**
     * In addition to destroying the [MainActivity], it will also complete the toast subject.
     * @since 70
     */
    override fun onDestroy() {
        toastSubject?.onComplete()
        super.onDestroy()
    }

    /**
     * Builds the PAT API Client from a Rest Adapter
     *
     * @since 46
     */
    private fun buildPATAPI() {
        patApiService = PatApiServiceImpl(
                BuildConfig.PAT_API_KEY,
                datadirectory,
                AssetManagerStaticData(assets),
                AndroidWifiChecker(applicationContext)
        )
    }

    /**
     * Checks if the stored patternSelections directory is present and clears if we hit a friday or if the
     * saved day of the week is higher than the current day of the week.
     *
     * @since 32
     */
    private fun checkSDCardData() {
        val data = filesDir
        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val lastUpdated = sp.getLong(LINES_LAST_UPDATED, -1)
        Timber.d(data.name)
        if (data.mkdirs())
            Timber.d("Created data storage")
        val lineInfo = File(data, "/lineinfo")
        if (data.mkdirs())
            Timber.d("created line info folder in storage")
        Timber.d(lastUpdated.toString())
        if (lastUpdated != (-1).toLong() && (System.currentTimeMillis() - lastUpdated) / 1000 / 60 / 60 > 24) {
            if (lineInfo.exists()) {
                val c = Calendar.getInstance()
                val day = c.get(Calendar.DAY_OF_WEEK)
                val o = Calendar.getInstance()
                o.timeInMillis = lastUpdated
                val oldDay = o.get(Calendar.DAY_OF_WEEK)
                if (day == Calendar.FRIDAY || oldDay >= day) {
                    val files = lineInfo.listFiles()
                    sp.edit().putLong(LINES_LAST_UPDATED, System.currentTimeMillis()).apply()
                    if (files != null) {
                        for (file in files) {
                            if (file.delete())
                                Timber.d("%s deleted", file.name)
                        }
                    }
                }
            }
        }

        if (lineInfo.listFiles() == null || lineInfo.listFiles().isEmpty()) {
            sp.edit().putLong(LINES_LAST_UPDATED, System.currentTimeMillis()).apply()
        }
    }

    private fun enableHttpResponseCache() {
        try {
            val httpCacheSize: Long = 10485760 // 10 MiB
            val fetch = externalCacheDir ?: cacheDir
            val httpCacheDir = File(fetch, "http")
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File::class.java, Long::class.javaPrimitiveType)
                    .invoke(null, httpCacheDir, httpCacheSize)
        } catch (httpResponseCacheNotAvailable: Exception) {
            Timber.e("HTTP response cache is unavailable.")
        }

    }

    private fun restoreActionBar() {
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar?
        if (toolbar != null) {
            try {
                setSupportActionBar(toolbar)
            } catch (e: Throwable) {
                mainLayout?.let {mainLayout ->
                    Snackbar.make(mainLayout,
                        "Material Design bugged out on your device. Please report this to the " + "Play Store Email if this pops up.", Snackbar.LENGTH_SHORT).show()
                }
            }

        }
        try {
            val t = supportActionBar
            t?.setDisplayHomeAsUpEnabled(true)
        } catch (e: NullPointerException) {
            mainLayout?.let { mainLayout ->
                Snackbar.make(mainLayout,
                        "Material Design bugged out on your device. Please report this to the " + "Play Store Email if this pops up.", Snackbar.LENGTH_SHORT).show()
            }
        }

    }

    //dunno...
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (mNavigationDrawerFragment?.isDrawerOpen == false) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            menuInflater.inflate(R.menu.select_transit, menu)
            //            restoreActionBar();
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    /**
     * Handles action bar item clicks. The action bar will automatically handle clicks on the
     * home/up button so long as you specify a parent activity in the AndroidManifest.xml.
     *
     * @param item the menu item clicked
     * @return true if the option is found?
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Timber.d("Running MainActivity's onOptionsItemSelected")
        // TODO: Perhaps we should be using onClick events in the XML like onClickAppDetails()
        if (mNavigationDrawerFragment?.actionBarDrawerToggle?.onOptionsItemSelected(item) == true) {
            Timber.d("Hamburger menu selected - will either close or open drawer")
            return true
        }
        val id = item.itemId
        if (id == R.id.action_select_buses && mNavigationDrawerFragment != null) {
            Timber.d("Select Buses in Menu Dropdown clicked - opens the drawer")
            mNavigationDrawerFragment?.openDrawer()
            return true
        } else if (id == R.id.action_about) {
            Timber.d("About Button Clicked. Will open the about page")
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
            return true
        } else if (id == R.id.action_clear) {
            clearSelection()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun clearSelection() {
        mNavigationDrawerFragment?.clearSelection()
        selectionFragment?.clearSelection()
    }

    /**
     *
     * Clears out route cache.
     * @since 79
     */
    private fun clearCache() {
        val lineInfo = File(filesDir, "/lineinfo")
        Timber.d("cleared files: %s", lineInfo.absolutePath)
        if (lineInfo.exists()) {
            val files = lineInfo.listFiles()
            if (files != null) {
                for (file in files)

                    file.delete()
            }
        }
        showToast("Cleared route cache. Restart app to take effect.", Toast.LENGTH_SHORT)

    }


    /**
     * Show a toast message.
     * @since 47
     * @param message the message to show as a toast
     * @param length the duration that the message shows
     */
    override fun showToast(message: String, length: Int) {
        if (toastSubject == null) {
            return
        }
        toastSubject?.onNext(NotificationMessage(message, length))
    }

    /**
     * Workaround to show a toast from [.toastMessageObserver]
     * @param message the message to show
     * @param duration the duration of the message
     */
    private fun showToastInternal(message: String?, duration: Int) {
        Toast.makeText(this, message, duration).show()
    }

    /**
     * [Observer] to show a toast using Notification Messages.
     *
     * @since 70
     */
    private fun toastMessageObserver(): Observer<NotificationMessage> {
        return object : Observer<NotificationMessage> {
            override fun onError(e: Throwable) {
                Timber.e(e, "Toast Observable encountered an error")
            }

            override fun onComplete() {
                Timber.i("Completed toast observer")
            }

            override fun onSubscribe(d: Disposable) {}

            override fun onNext(notificationMessage: NotificationMessage) {
                Timber.d("printing toast message: %s", notificationMessage.message)
                // android linter goes crazy if passing a normal int into
                // Toast.makeText in the line below...... (Why) must use
                // showToastInternal() to generate a toast as a workaround.
                showToastInternal(notificationMessage.message, notificationMessage.length)
            }
        }
    }


    override fun makeSnackbar(message: String,
                              length: Int,
                              action: String,
                              listener: View.OnClickListener) {
        if (message.isEmpty()) return
        mainLayout?.let {mainLayout ->
            Snackbar.make(mainLayout, message, length)
                    .setAction(action, listener)
                    .show()
        }
    }


    override fun onBackPressed() {
        if (mNavigationDrawerFragment?.closeDrawer() == false)
            super.onBackPressed()
    }

    override fun showOkDialog(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show()
    }

    override fun getSelectedRoute(routeNumber: String): Route? {
        return mNavigationDrawerFragment?.getSelectedRoute(routeNumber)
    }

    override fun openPermissionsPage() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }


    /**
     * Click Event for the
     * [rectangledbmi.com.pittsburghrealtimetracker.R.menu.select_transit]'s Detour
     * Information.
     *
     *
     * Maybe we should be moving item item events in [.onOptionsItemSelected] to
     * an onClick like this method.
     *
     * @param item the application details item
     */
    @Suppress("UNUSED_PARAMETER")
    fun onClickDetourInfo(item: MenuItem) {
        val browserIntent = Intent(Intent.ACTION_VIEW,
                Uri.parse(getString(R.string.detour_url)))
        startActivity(browserIntent)
    }

    /**
     * Click Event for the
     * [rectangledbmi.com.pittsburghrealtimetracker.R.menu.select_transit]'s Application
     * Details.
     *
     *
     * Maybe we should be moving item item events in [.onOptionsItemSelected] to an
     * onClick like this method.
     *
     * @param item the application details item
     */
    @Suppress("UNUSED_PARAMETER")
    fun onClickAppDetails(item: MenuItem) {
        openPermissionsPage()
    }

    /**
     * Click event for the
     * [rectangledbmi.com.pittsburghrealtimetracker.R.menu.select_transit] to clear cache.
     * @param item the menu item.
     */
    @Suppress("UNUSED_PARAMETER")
    fun onClearCache(item: MenuItem) {
        clearCache()
    }

    override fun restoreSelection() {
        mNavigationDrawerFragment?.reselectRoutes()
    }

    private fun openNoBusesPage() {
        val url = Uri.parse("https://github.com/rectangle-dbmi/Realtime-Port-Authority/wiki/Port-Authority-Server-Downtimes")
        val internetBrowser = Intent(Intent.ACTION_VIEW, url)
        startActivity(internetBrowser)
    }

    @Suppress("UNUSED_PARAMETER")
    fun onClickNoBuses(item: MenuItem) {
        openNoBusesPage()
    }

    companion object {

        private const val LINES_LAST_UPDATED = "lines_last_updated"
    }
}
