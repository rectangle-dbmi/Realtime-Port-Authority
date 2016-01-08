package rectangledbmi.com.pittsburghrealtimetracker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.Set;

/**
 * This is the base fragment class that
 *
 * @author Jeremy Jao
 * @since 64
 */
public abstract class SelectionFragment extends Fragment implements NavigationDrawerFragment.BusListCallbacks {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    protected static final String ARG_PARAM1 = "param1";
    protected static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    protected String mParam1;
    protected String mParam2;

    protected BusSelectionInteraction busDrawerInteractor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BusSelectionInteraction) {
            busDrawerInteractor = (BusSelectionInteraction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface BusSelectionInteraction {

        /**
         *
         * @return the selected buses
         */
        Set<String> getSelectedRoutes();
    }
}
