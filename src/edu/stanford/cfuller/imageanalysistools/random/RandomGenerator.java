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

package edu.stanford.cfuller.imageanalysistools.random;

import java.util.Random;

/**
 * Wrapper for random number generation to allow substitution of different methods for generating random numbers.
 * 
 * The current implementation just wraps the java default random number generator.
 * 
 * @author Colin J. Fuller
 */
public class RandomGenerator {

//    private long randState = 4101842887655102017L;

    private Random r;

    protected RandomGenerator() {
        r = new Random();
    }

    public long randLong() {

        return r.nextLong();
    }

    public long randLong(long max) {
        return (long) Math.floor(max*randDouble());
    }

    public int randInt(int max) {
        return (int) Math.floor(max*randDouble());
    }

    public double randDouble() {
        return Math.random();

    }


    public int randInt() {
        return (int) randLong();
    }

    public void seed(long seed) {
//        randState ^= seed;
//        randState = randLong();
    }

    private static RandomGenerator gen;

    static {
        gen = new RandomGenerator();
        gen.seed(System.currentTimeMillis());
    }

    public static double rand() {

        return gen.randDouble();

    }

    public static RandomGenerator getGenerator() {
        return gen;
    }

}
