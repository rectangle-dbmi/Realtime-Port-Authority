package rectangledbmi.com.pittsburghrealtimetracker.ui.about

import androidx.fragment.app.Fragment
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import rectangledbmi.com.pittsburghrealtimetracker.R


class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .add(R.id.container, PlaceholderFragment())
                    .commit()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //        int id = item.getItemId();
        //        if (id == R.id.action_settings) {
        //            return true;
        //        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    class PlaceholderFragment : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.fragment_about, container, false)
            val version = view.findViewById<View>(R.id.version) as TextView
            val fbLink = view.findViewById<View>(R.id.fb_link) as TextView
            val gitLink = view.findViewById<View>(R.id.git_link) as TextView
            val emailLink = view.findViewById<View>(R.id.email_link) as TextView
            try {
                fbLink.movementMethod = LinkMovementMethod.getInstance()
                gitLink.movementMethod = LinkMovementMethod.getInstance()
                emailLink.movementMethod = LinkMovementMethod.getInstance()
                val versionText = getString(R.string.version_prefix) + " " + requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0)?.versionName
                version.text = versionText
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }

            return view
        }
    }


}
