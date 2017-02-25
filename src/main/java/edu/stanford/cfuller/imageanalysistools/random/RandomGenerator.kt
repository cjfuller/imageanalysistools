/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 2011 Colin J. Fuller
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.random

import java.util.Random

/**
 * Wrapper for random number generation to allow substitution of different methods for generating random numbers.

 * The current implementation just wraps the java default random number generator.

 * @author Colin J. Fuller
 */
class RandomGenerator protected constructor() {

    //    private long randState = 4101842887655102017L;

    private val r: Random

    init {
        r = Random()
    }

    fun randLong(): Long {

        return r.nextLong()
    }

    fun randLong(max: Long): Long {
        return Math.floor(max * randDouble()).toLong()
    }

    fun randInt(max: Int): Int {
        return Math.floor(max * randDouble()).toInt()
    }

    fun randDouble(): Double {
        return Math.random()

    }


    fun randInt(): Int {
        return randLong().toInt()
    }

    fun seed(seed: Long) {
        //        randState ^= seed;
        //        randState = randLong();
    }

    companion object {

        var generator: RandomGenerator? = null
            private set

        init {
            generator = RandomGenerator()
            generator!!.seed(System.currentTimeMillis())
        }

        fun rand(): Double {

            return generator!!.randDouble()

        }
    }

}
