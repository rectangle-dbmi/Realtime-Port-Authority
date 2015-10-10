package rectangledbmi.com.pittsburghrealtimetracker.handlers.extend;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import rectangledbmi.com.pittsburghrealtimetracker.R;

/**
 * This is a dialog that pops up in the activity's onResume to tell the user to close the app and
 * turn on the data connection.
 *
 * Created by epicstar on 4/19/15.
 */
public class DataRequiredDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(R.string.data_not_available_title);
        dialog.setMessage(R.string.data_not_available);

        //sets the "close app" button
        dialog.setPositiveButton(R.string.data_close_app, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });

        //sets the "Cancel" button
        dialog.setNegativeButton(R.string.data_dismiss, null);
        return dialog.create();
    }
}
