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
import android.widget.CheckedTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.R;
import rectangledbmi.com.pittsburghrealtimetracker.world.Route;

/**
 * @author epicstar
 *
 * This is the custom adapter of the bus list in the navigation drawer.
 * Uses a CheckableRelativeLayout for the rows. Two CheckTextViews
 * - one is a description of the bus route
 * - other is the bus route number (background is an xml GradientDrawable)
 *
 * This will then add them from left to right
 *
 * Created by epicstar on 9/15/14.
 */
public class ColoredArrayAdapter extends ArrayAdapter<Route> {

    /**
     * The activity/fragment that this is being called
     */
    private Context context;

    /**
     * This is the reference to all the routes that will be added to the ListView Drawer
     */
    private List<Route> objects;

    /**
     * Initializes the adapter
     * @param context the activity/fragment this is coming from
     * @param resource is the xml of the row (the RelativeLayout)
     * @param objects is all the possible routes
     */
    public ColoredArrayAdapter(Context context, int resource, List<Route> objects) {
        super(context, resource, objects);
        this.context = context;
        this.objects = objects;
    }

    /**
     * This only handles only the list. Fills in the positions from the objects List<Route>
     *
     * Also, if the background color (from the drawable rectangle) is light, use black text to
     * display the bus number.
     *
     * @param position location of the row in the listview
     * @param convertView
     * @param parent the listview
     * @return the view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.row_list, parent, false);

        TextView description = (TextView) convertView.findViewById(R.id.bus_description);
        TextView route = (TextView) convertView.findViewById(R.id.bus_route);
        GradientDrawable icon = (GradientDrawable) route.getBackground();
        description.setText(objects.get(position).getRouteInfo());
        route.setText(objects.get(position).getRoute());
        icon.setColor(objects.get(position).getRouteColor());
        return convertView;
    }

    /**
     * Decides whether or not the color (background color) is light or not.
     *
     * Formula was taken from here:
     * http://stackoverflow.com/questions/24260853/check-if-color-is-dark-or-light-in-android
     *
     * @param color the background color being fed
     * @return whether or not the background color is light or not (.345 is the current threshold)
     */
    private boolean isLight(int color) {
        return 1.0-(0.299*Color.red(color) + 0.587*Color.green(color) + 0.114*Color.blue(color))/255 < .345;
    }
}
