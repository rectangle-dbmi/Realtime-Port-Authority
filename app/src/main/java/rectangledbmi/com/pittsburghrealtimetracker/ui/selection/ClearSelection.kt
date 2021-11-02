package rectangledbmi.com.pittsburghrealtimetracker.ui.selection

/**
 * Interface for clearing selection state among the navigation drawer fragments and the other fragments
 * that interact with it.
 * @author Jeremy Jao
 */
interface ClearSelection {
    /**
     * Anything that implements this will clear the selection given.
     */
    fun clearSelection()
}
