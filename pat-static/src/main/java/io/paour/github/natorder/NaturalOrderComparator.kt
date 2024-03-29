package io.paour.github.natorder

// epicstar - changed to bring natural order comparator to app...
// mikeantonacci - converted to kotlin

/*
 NaturalOrderComparator.java -- Perform 'natural order' comparisons of strings in Java.
 Copyright (C) 2003 by Pierre-Luc Paour <natorder@paour.com>
 Based on the C version by Martin Pool, of which this is more or less a straight conversion.
 Copyright (C) 2000 by Martin Pool <mbp@humbug.org.au>
 This software is provided 'as-is', without any express or implied
 warranty.  In no event will the authors be held liable for any damages
 arising from the use of this software.
 Permission is granted to anyone to use this software for any purpose,
 including commercial applications, and to alter it and redistribute it
 freely, subject to the following restrictions:
 1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
 2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
 3. This notice may not be removed or altered from any source distribution.
 */

import java.util.*

class NaturalOrderComparator<T> : Comparator<T> // epicstar: change to abide by Generic Rules
{
    private fun compareRight(a: String, b: String): Int {
        var bias = 0
        var ia = 0
        var ib = 0

        // The longest run of digits wins. That aside, the greatest
        // value wins, but we can't know that it will until we've scanned
        // both numbers to know that they have the same magnitude, so we
        // remember it in BIAS.
        while (true) {
            val ca = a.getOrElse(ia) { 0.toChar() }
            val cb = b.getOrElse(ib) { 0.toChar() }

            bias = when {
                !ca.isDigit() && !cb.isDigit()     -> return bias
                !ca.isDigit()                      -> return -1
                !cb.isDigit()                      -> return +1
                ca < cb && bias == 0               -> -1
                ca > cb && bias == 0               -> +1
                ca.code == 0 && cb.code == 0 -> return bias
                else                               -> bias
            }

            ia++
            ib++
        }
    }

    override fun compare(o1: T, o2: T): Int {
        val a = o1.toString()
        val b = o2.toString()

        var ia = 0
        var ib = 0
        var nza: Int
        var nzb: Int
        var ca: Char
        var cb: Char
        var result: Int

        while (true) {
            // only count the number of zeroes leading the last number compared
            nzb = 0
            nza = nzb

            ca = a.getOrElse(ia) { 0.toChar() }
            cb = b.getOrElse(ib) { 0.toChar() }


            // skip over leading spaces or zeros
            while (Character.isSpaceChar(ca) || ca == '0') {
                if (ca == '0') {
                    nza++
                } else {
                    // only count consecutive zeroes
                    nza = 0
                }

                ca = a.getOrElse(++ia) { 0.toChar() }
            }

            while (Character.isSpaceChar(cb) || cb == '0') {
                if (cb == '0') {
                    nzb++
                } else {
                    // only count consecutive zeroes
                    nzb = 0
                }

                cb = b.getOrElse(++ib) { 0.toChar() }
            }

            // process run of digits
            if (ca.isDigit() && cb.isDigit()) {
                result = compareRight(a.substring(ia), b.substring(ib))
                if (result != 0) {
                    return result
                }
            }

            if (ca.code == 0 && cb.code == 0) {
                // The strings compare the same. Perhaps the caller
                // will want to call strcmp to break the tie.
                return nza - nzb
            }

            if (ca < cb) {
                return -1
            } else if (ca > cb) {
                return +1
            }

            ++ia
            ++ib
        }
    }
}