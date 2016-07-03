package rectangledbmi.com.pittsburghrealtimetracker.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.ArrayList;

import rectangledbmi.com.pittsburghrealtimetracker.BuildConfig;
import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.ui.adapters.AboutAdapter;


public class AboutActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView recycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recycler = (RecyclerView) findViewById(R.id.recyclerView);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ((TextView) findViewById(R.id.version)).setText(getString(R.string.version_prefix) + BuildConfig.VERSION_NAME);

        findViewById(R.id.email).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.email_link))));
            }
        });

        findViewById(R.id.facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.facebook_link))));
            }
        });

        findViewById(R.id.github).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.github_link))));
            }
        });

        findViewById(R.id.item).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recycler.setPadding(0, findViewById(R.id.item).getHeight(), 0, 0);
                findViewById(R.id.item).getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        ArrayList<AboutAdapter.TextListData> arrayList = new ArrayList<>();

        arrayList.add(new AboutAdapter.TextListData(null, getString(R.string.contrib_prefix), null, null));

        String[] contrib_names = getResources().getStringArray(R.array.contrib_names);
        String[] contrib_descs = getResources().getStringArray(R.array.contrib_descs);
        String[] contrib_urls = getResources().getStringArray(R.array.contrib_urls);
        String[] contrib_images = getResources().getStringArray(R.array.contrib_images);

        for (int i = 0; i < contrib_names.length; i++) {
            arrayList.add(new AboutAdapter.TextListData(contrib_images[i], contrib_names[i], contrib_descs[i], Uri.parse(contrib_urls[i])));
        }

        arrayList.add(new AboutAdapter.TextListData(null, getString(R.string.lib_prefix), null, null));

        String[] lib_names = getResources().getStringArray(R.array.lib_names);
        String[] lib_descs = getResources().getStringArray(R.array.lib_descs);
        String[] lib_urls = getResources().getStringArray(R.array.lib_urls);

        for (int i = 0; i < lib_names.length; i++) {
            arrayList.add(new AboutAdapter.TextListData(null, lib_names[i], lib_descs[i], Uri.parse(lib_urls[i])));
        }

        recycler.setLayoutManager(new GridLayoutManager(this, 1));
        recycler.setAdapter(new AboutAdapter(this, arrayList));
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

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
