package rectangledbmi.com.pittsburghrealtimetracker

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.util.TypedValue
import timber.log.Timber

/**
 * This is the base fragment class that

 * @author Jeremy Jao
 *
 * @since 64
 */
abstract class SelectionFragment : Fragment(), NavigationDrawerFragment.BusListCallbacks {

    protected companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        protected val ARG_PARAM1 = "param1"
        protected val ARG_PARAM2 = "param2"
    }

    // TODO: Rename and change types of parameters
    lateinit protected var mParam1: String
    lateinit protected var mParam2: String

    protected var busDrawerInteractor: BusSelectionInteraction = null!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments.getString(ARG_PARAM1)
            mParam2 = arguments.getString(ARG_PARAM2)
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            busDrawerInteractor = context as BusSelectionInteraction

        } catch (e: RuntimeException) {
            Timber.e("Activity does not implement BusSelectionInteraction:\n${Log.getStackTraceString(e)}")
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
     */
    interface BusSelectionInteraction {

        /**

         * @return the selected buses
         */
        val selectedRoutes: Set<String>
    }
}
