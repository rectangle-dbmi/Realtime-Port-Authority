package rectangledbmi.com.pittsburghrealtimetracker.handlers.extend;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import rectangledbmi.com.pittsburghrealtimetracker.R;

/**
 * Created by epicstar on 12/22/14.
 */
public class ETAWindowAdapter implements GoogleMap.InfoWindowAdapter {

    private LayoutInflater inflater;
    private View infoWindow;

    public ETAWindowAdapter(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null; //should always return null
    }

    @Override
    public View getInfoContents(Marker marker) {
        if(infoWindow == null)
            infoWindow = inflater.inflate(R.layout.map_eta_infowindow, null);

        TextView title = (TextView) infoWindow.findViewById(R.id.title);
        TextView snippet = (TextView) infoWindow.findViewById(R.id.snippet);
        title.setText(marker.getTitle());
        snippet.setText(marker.getSnippet());

        return infoWindow;
    }
}
