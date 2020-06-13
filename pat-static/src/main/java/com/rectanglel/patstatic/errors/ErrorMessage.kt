package com.rectanglel.patstatic.errors

import io.paour.github.natorder.NaturalOrderComparator

/**
 * This is a processed error message object consisting of a message and its parameters. Since Port
 * Authority refuses to make errors work as expected,
 *
 * @author Jeremy Jao
 * @since 49
 */
data class ErrorMessage(val message: String, val parameterList: List<String?>?) {

    /**
     * The combined parameters for the message.
     */
    val parameters: String? by lazy {
         when {
            parameterList == null -> null
            parameterList.isEmpty() -> null
            parameterList[0] == null -> null
            else -> parameterList.sortedWith(NaturalOrderComparator()).joinToString()
        }
    }
}
