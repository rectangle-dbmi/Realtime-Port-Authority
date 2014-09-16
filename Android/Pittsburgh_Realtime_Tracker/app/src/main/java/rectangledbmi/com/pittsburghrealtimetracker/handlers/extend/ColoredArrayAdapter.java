package rectangledbmi.com.pittsburghrealtimetracker.handlers.extend;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import rectangledbmi.com.pittsburghrealtimetracker.R;

/**
 * Created by epicstar on 9/15/14.
 */
public class ColoredArrayAdapter extends ArrayAdapter<String> {

    private String[] colors;


    public ColoredArrayAdapter(Context context, int resource, int textViewResourceId, String[] objects) {
        super(context, resource, textViewResourceId, objects);
        setColors(context);
    }


    private void setColors(Context context) {
        colors = context.getResources().getStringArray(R.array.buscolors);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        view.setBackgroundColor(Color.parseColor(colors[position]));
        return view;
    }
}
