package rectangledbmi.com.pittsburghrealtimetracker.selection

/**
 * Class instance to pass an object to create toasts and snackbar messages reactively.
 *
 * @author Jeremy Jao
 * @since 70
 */
class NotificationMessage private constructor(message: String, length: Int) {

    var message: String? = null
        private set
    var length: Int = 0
        private set

    init {
        this.message = message
        this.length = length
    }

    companion object {

        @JvmStatic
        fun create(message: String, length: Int): NotificationMessage {
            return NotificationMessage(message, length)
        }
    }
}
