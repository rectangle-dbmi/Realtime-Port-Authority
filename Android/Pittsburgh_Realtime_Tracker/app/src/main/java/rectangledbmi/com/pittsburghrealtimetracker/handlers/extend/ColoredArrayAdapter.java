package rectangledbmi.com.pittsburghrealtimetracker.handlers.extend;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;

/**
 * Created by epicstar on 9/15/14.
 */
public class ColoredArrayAdapter extends ArrayAdapter<Route> {
    private Context context;
    private List<Route> objects;

    public ColoredArrayAdapter(Context context, int resource, List<Route> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.row_list, parent, false);
        TextView description = (TextView) view.findViewById(R.id.bus_description);
        TextView route = (TextView) view.findViewById(R.id.bus_route);
        GradientDrawable icon = (GradientDrawable) route.getBackground();
        description.setText(objects.get(position).getRouteInfo());
        route.setText(objects.get(position).getRoute());
        icon.setColor(objects.get(position).getRouteColor());

        return view;
    }
}
