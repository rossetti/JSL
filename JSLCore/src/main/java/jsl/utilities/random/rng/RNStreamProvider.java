/*
 * Copyright (c) 2019. Manuel D. Rossetti, rossetti@uark.edu
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

import jsl.simulation.Simulation;
import jsl.utilities.reporting.JSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

/**
 * A concrete implementation of RNStreamProviderIfc.  If more than getStreamNumberWarningLimit()
 * streams are made a warning message is logged.  Generally, unless you know what you are doing
 * you should not need an immense number of streams.  Instead, use a small number of
 * streams many times. Conceptually this provider could have a possibly infinite number of streams,
 * which would have bad memory implications.  Thus, the reason for the warning.
 * The default stream if not set is the first stream.
 */
public final class RNStreamProvider implements RNStreamProviderIfc {

    public final static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private int myStreamNumberWarningLimit = 5000;

    private final RNStreamFactory myStreamFactory;

    private final List<RNStreamIfc> myStreams;

    private final int myDefaultStreamNum;

    /**
     * Assumes stream 1 is the default
     */
    public RNStreamProvider() {
        this(1);
    }

    /**
     * @param defaultStreamNum the stream number to use as the default
     */
    public RNStreamProvider(int defaultStreamNum) {
        if (defaultStreamNum <= 0) {
            throw new IllegalArgumentException("The default stream number must be > 0");
        }
        myDefaultStreamNum = defaultStreamNum;
        myStreamFactory = new RNStreamFactory();
        myStreams = new ArrayList<>();
        // get the default stream number, this makes the intermediate streams also
        defaultRNStream();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RNStreamProvider{");
        sb.append("StreamNumberWarningLimit=").append(getStreamNumberWarningLimit());
        sb.append(", DefaultStreamNum=").append(defaultRNStreamNumber());
        sb.append(", Number of Streams Provided =").append(lastRNStreamNumber());
        sb.append('}');
        return sb.toString();
    }

    /**
     * @return the limit associated with the warning message concerning the number of streams created
     */
    public final int getStreamNumberWarningLimit() {
        return myStreamNumberWarningLimit;
    }

    /**
     * @param limit the limit associated with the warning message concerning the number of streams created
     */
    public final void setStreamNumberWarningLimit(int limit) {
        myStreamNumberWarningLimit = limit;
    }

    @Override
    public int defaultRNStreamNumber() {
        return myDefaultStreamNum;
    }

    @Override
    public RNStreamIfc nextRNStream() {
        RNStreamIfc stream = myStreamFactory.getStream();
        myStreams.add(stream);
        if (myStreams.size() > myStreamNumberWarningLimit) {
            JSL.getInstance().LOGGER.warn("The number of streams made is now = {}", myStreams.size());
            JSL.getInstance().LOGGER.warn("Increase the stream warning limit if you don't want to see this message");
        }
        logger.info("Provided stream {}, stream {} of {} streams", stream.getId(), lastRNStreamNumber(), myStreams.size());
        return stream;
    }

    @Override
    public int lastRNStreamNumber() {
        return myStreams.size();
    }

    @Override
    public RNStreamIfc rnStream(int i) {
        if (i > lastRNStreamNumber()) {
            RNStreamIfc stream = null;
            for (int j = lastRNStreamNumber(); j <= i; j++) {
                stream = nextRNStream();
            }
            return stream;
        }
        return myStreams.get(i - 1);
    }

    @Override
    public int getStreamNumber(RNStreamIfc stream) {
        if (myStreams.indexOf(stream) == -1) {
            return -1;
        }
        return myStreams.indexOf(stream) + 1;
    }

    @Override
    public void advanceStreamMechanism(int n) {
        myStreamFactory.advanceSeeds(n);
    }

    @Override
    public void resetRNStreamSequence() {
        myStreams.clear();
        myStreamFactory.resetFactorySeed();
    }

    /**
     * Gets the default initial seed: seed = {12345, 12345, 12345,
     * 12345, 12345, 12345};
     *
     * @return an array holding the initial seed values
     */
    public final long[] getDefaultInitialSeed() {
        return myStreamFactory.getDefaultInitialFactorySeed();
    }

    /**
     * Returns the current seed
     *
     * @return the array of seed values for the current state
     */
    public final long[] getCurrentSeed() {
        return myStreamFactory.getFactorySeed();
    }

    /**
     * Sets the initial seed to the six integers in the vector seed[0..5]. This
     * will be the seed (initial state) of the first stream. By default, this
     * seed is (12345, 12345, 12345, 12345, 12345, 12345).
     * <p>
     * If it is	called,	the first 3 values of the seed must all be less than m1
     * = 4294967087, and not all 0; and the last 3 values must all be less than
     * m2 = 4294944443, and not all 0. Returns false for invalid seeds, and true
     * otherwise.
     *
     * @param seed the seeds
     *
     */
    public final void setInitialSeed(long[] seed) {
        myStreamFactory.setFactorySeed(seed);
        //return myStreamFactory.setFactorySeed(seed);
    }
}
