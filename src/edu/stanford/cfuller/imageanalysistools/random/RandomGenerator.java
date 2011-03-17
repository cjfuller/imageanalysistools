/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package edu.stanford.cfuller.imageanalysistools.random;

import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: cfuller
 * Date: Dec 9, 2010
 * Time: 3:22:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class RandomGenerator {

    private long randState = 4101842887655102017L;

    private Random r;

    protected RandomGenerator() {
        r = new Random();
    }

    public long randLong() {
/*
        randState ^= randState >>> 21;
        randState ^= randState << 35;
        randState ^= randState >>> 4;

        return randState * 2685821657736338717L;
*/
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
        /*
        long rLong = randLong();
        double normValue = 5.42101086242752217e-20;
        return (normValue *(rLong >>> 1))*2 + normValue*(rLong&0xFF); //bit of a hack because of unsigned vs signed stuff going on-- need to check on a better way
        */
        //TODO: deal with signed vs. unsigned better
    }


    public int randInt() {
        return (int) randLong();
    }

    public void seed(long seed) {
        randState ^= seed;
        randState = randLong();
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
