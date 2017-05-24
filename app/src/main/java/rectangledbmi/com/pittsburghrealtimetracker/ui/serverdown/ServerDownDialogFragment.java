package rectangledbmi.com.pittsburghrealtimetracker.ui.serverdown;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;

import rectangledbmi.com.pittsburghrealtimetracker.R;

/**
 * Dialog to tell users that the app is down.
 * Created by epicstar on 5/23/17.
 * @since 78
 */

public class ServerDownDialogFragment extends AppCompatDialogFragment {

    @SuppressWarnings("unused")
    public static ServerDownDialogFragment newInstance() {
        return new ServerDownDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstance) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), R.style.Theme_AppCompat_Dialog_Alert);
        dialog.setTitle(R.string.servers_down_title);
        dialog.setMessage(R.string.servers_down_description);
        dialog.setNegativeButton(R.string.servers_down_dismiss, (dialog1, which) -> dismiss());
        dialog.setPositiveButton(R.string.servers_down_tell_me_more_button, (dialog1, which) -> {
            Uri url = Uri.parse("https://github.com/rectangle-dbmi/Realtime-Port-Authority/wiki/Port-Authority-Server-Downtimes");
            Intent internetBrowser = new Intent(Intent.ACTION_VIEW, url);
            startActivity(internetBrowser);
        });
        return dialog.create();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }
}
