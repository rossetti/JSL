/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package jsl.utilities.random.rng;

public class MarseRobertsLCG implements RandU01Ifc {

    private final static int defaultSeeds[] = {0, 1973272912, 281629770, 20006270,
        1280689831, 2096730329, 1933576050, 913566091, 246780520,
        1363774876, 604901985, 1511192140, 1259851944, 824064364,
        150493284, 242708531, 75253171, 1964472944, 1202299975, 233217322,
        1911216000, 726370533, 403498145, 993232223, 726466604, 762430696,
        1922803170, 135516923, 76271663, 413682397, 72646604, 336157058,
        1432650381, 1120463904, 595778810, 877722890, 1046574445, 68611991,
        2088367019, 748545416, 622401386, 2122378830, 640690903,
        1774806513, 2132545692, 2079249579, 78130110, 852776735,
        1187867272, 1351423507, 1645973084, 1997049139, 922510944,
        2045512870, 898585771, 243649545, 1004818771, 773686062, 403188473,
        372279877, 1901633463, 498067494, 2087759558, 493157915, 597104727,
        1530940798, 1814496276, 536444882, 1663153658, 855503735, 67784357,
        1432404475, 619691088, 119025595, 880802310, 176192644, 1116780070,
        277854671, 1366580350, 1142483975, 2026948561, 1053920743,
        786262391, 1792203830, 1494667770, 1923011392, 1433700034,
        1244184613, 1147297105, 539712780, 1545929719, 190641742,
        1645390429, 264907697, 620389253, 1502074852, 927711160, 364849192,
        2049576050, 638580085, 547070247};

    private final static int MODULUS = 2147483647;

    private final static int MULT1 = 24112;

    private final static int MULT2 = 26143;

    private int myInitialStream = 1;

    private int myInitialSeed = defaultSeeds[1];

    private int myCurrentSeed;

    private int myCurrentStream;

    private double myPrevU;

    /** Constructs an instance of the generator using stream 1
     */
    public MarseRobertsLCG() {
        setInitialStream(1);
    }

    /** Constructs an instance of the generator using the
     *  supplied stream
     *
     * @param stream must be [1,100]
     */
    public MarseRobertsLCG(int stream) {
        setInitialStream(stream);
    }

    private void setInitialStream(int stream) {
        if ((stream < 1) || (stream > 100)) {
            throw new IllegalArgumentException("Initial stream must be in [1,100]");
        }
        myInitialStream = stream;
        myInitialSeed = defaultSeeds[stream];
        myCurrentStream = myInitialStream;
        myCurrentSeed = myInitialSeed;
    }

    /** Sets the current seed to the supplied integer, causing the generator
     *  to start generating using the supplied seed. After using this method
     *  the method getStream() will return the last stream used by the generator
     *  not necessarily the stream associated with the supplied stream
     *
     * @param seed
     */
    public final void setSeed(int seed) {
        if ((seed < 1) || (seed > MODULUS - 1)) {
            throw new IllegalArgumentException("Seed must be between 1 and " + (MODULUS - 1));
        }

        myCurrentSeed = seed;
    }

    /** Returns the current value for the seed
     *
     * @return
     */
    public final int getSeed() {
        return myCurrentSeed;
    }

    /** Gets the seed associated with the supplied stream. The stream
     *  must be in [1,100]
     *
     * @param stream
     * @return
     */
    public final int getDefaultSeed(int stream) {
        if ((stream < 1) || (stream > 100)) {
            throw new IllegalArgumentException("Stream must be in [1,100]");
        }
        return defaultSeeds[stream];
    }

    /** Changes the current stream for the generator.  Causes the
     *  generator to start generating from the beginning of the specified
     *  stream
     *
     * @param stream
     */
    public final void setStream(int stream) {
        if ((stream < 1) || (stream > 100)) {
            throw new IllegalArgumentException("Stream must be in [1,100]");
        }
        myCurrentStream = stream;
        myCurrentSeed = defaultSeeds[stream];
    }

    /** Gets the last stream specified by setStream().  Directly using setSeed()
     *  may cause the seed not to correspond to this returned stream
     *
     * @return
     */
    public final int getStream() {
        return (myCurrentStream);
    }

    /** Returns a U(0,1) using the current value of the seed.  The current seed
     *  is updated after this call
     *
     */
    public final double randU01() {
        int zi, lowprd, hi31;
        zi = myCurrentSeed;
        lowprd = (zi & 65535) * MULT1;
        hi31 = (zi >> 16) * MULT1 + (lowprd >> 16);
        zi = ((lowprd & 65535) - MODULUS) + ((hi31 & 32767) << 16)
                + (hi31 >> 15);
        if (zi < 0) {
            zi += MODULUS;
        }
        lowprd = (zi & 65535) * MULT2;
        hi31 = (zi >> 16) * MULT2 + (lowprd >> 16);
        zi = ((lowprd & 65535) - MODULUS) + ((hi31 & 32767) << 16)
                + (hi31 >> 15);
        if (zi < 0) {
            zi += MODULUS;
        }
        myCurrentSeed = zi;

        double u = ((zi >> 7 | 1) + 1) / 16777216.0;
        myPrevU = u;
        return (u);
    }

    /** The previous U(0,1) generated (returned) by randU01()
     *
     * @return
     */
    public final double getPrevU01() {
        return myPrevU;
    }

    /** Returns the antithetic of the previous U(0,1)
     *  i.e. 1.0 - getPrevU01()
     *
     * @return
     */
    public final double getAntitheticValue() {
        return 1.0 - myPrevU;
    }
}
