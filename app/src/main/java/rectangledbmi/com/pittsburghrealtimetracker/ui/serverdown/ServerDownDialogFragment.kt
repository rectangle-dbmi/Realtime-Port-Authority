package rectangledbmi.com.pittsburghrealtimetracker.ui.serverdown

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment

import rectangledbmi.com.pittsburghrealtimetracker.R

/**
 * Dialog to tell users that the app is down.
 * Created by epicstar on 5/23/17.
 * @since 78
 */

class ServerDownDialogFragment : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstance: Bundle?): Dialog =
            AlertDialog.Builder(context!!, R.style.Theme_AppCompat_Dialog_Alert).run {
                this.setTitle(R.string.servers_down_title)
                this.setMessage(R.string.servers_down_description)
                this.setNegativeButton(R.string.servers_down_dismiss) { _, _ -> dismiss() }
                this.setPositiveButton(R.string.servers_down_tell_me_more_button) { _, _ ->
                    val url = Uri.parse("https://github.com/rectangle-dbmi/Realtime-Port-Authority/wiki/Port-Authority-Server-Downtimes")
                    val internetBrowser = Intent(Intent.ACTION_VIEW, url)
                    startActivity(internetBrowser)
                }
                this.create()
            }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    companion object {

        fun newInstance(): ServerDownDialogFragment {
            return ServerDownDialogFragment()
        }
    }
}
