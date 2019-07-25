package com.rectanglel.pattrack.extensions

import android.graphics.Color

/**
 * Check if the int representation of a color is considered light or dark.
 * @return true if the color is light, false if dark.
 */
fun Int.isLightColor() : Boolean =
        1.0 - (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255 < .5