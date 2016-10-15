package rectangledbmi.com.pittsburghrealtimetracker.ui.about;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import rectangledbmi.com.pittsburghrealtimetracker.R;


public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_about, container, false);
            TextView version = (TextView) view.findViewById(R.id.version);
            TextView fbLink = (TextView) view.findViewById(R.id.fb_link);
            TextView gitLink = (TextView) view.findViewById(R.id.git_link);
            TextView emailLink = (TextView) view.findViewById(R.id.email_link);
            try {
                fbLink.setMovementMethod(LinkMovementMethod.getInstance());
                gitLink.setMovementMethod(LinkMovementMethod.getInstance());
                emailLink.setMovementMethod(LinkMovementMethod.getInstance());
                String versionText = getString(R.string.version_prefix) + " " + getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
                version.setText(versionText);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            return view;
        }
    }


}
