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

/**
 * An interface to define the ability to provide random number streams (RNStreamIfc)
 * Conceptualizes this process as making a sequence of streams, numbered 1, 2, 3, ...
 * for use in generating pseudo-random numbers that can be controlled.
 */
public interface RNStreamProviderIfc {

    /**
     * It is useful to designate one of the streams in the sequence of streams as
     * the default stream from the provider.  When clients don't care to get new
     * streams, this method guarantees that the same stream is returned every time.
     *
     * @return the default stream from this provider
     */
    default RNStreamIfc defaultRNStream() {
        return rnStream(defaultRNStreamNumber());
    }

    /**
     * The sequence number associated with the default random number stream. This
     * allows clients to know what stream number has been assigned to the default.
     *
     * @return the stream number of the default random number stream for this provider
     */
    int defaultRNStreamNumber();

    /**
     * Tells the provider to make and return the next RNStreamIfc in the sequence
     * of streams
     *
     * @return the made stream
     */
    RNStreamIfc nextRNStream();

    /**
     * Each call to nextStream() makes another stream in the sequence of streams.
     * This method should return the number of streams provided by the provider. If nextStream() is
     * called once, then lastRNStreamNumber() should return 1.  If nextStream() is called
     * twice then lastRNStreamNumber() should return 2, etc. Thus, this method
     * returns the number in the sequence associated with the last stream made.
     * If lastRNStreamNumber() returns 0, then no streams have been provided.
     *
     * @return the number in the sequence associated with the last stream made
     */
    int lastRNStreamNumber();

    /**
     * Tells the provider to return the ith stream of the sequence of streams that it provides.
     * If i is greater than lastRNStreamNumber() then lastRNStreamNumber() is advanced
     * according to the additional number of streams. For example, if lastRNStreamNumber() = 10
     * and i = 15, then streams 11, 12, 13, 14, 15 are assumed provided and stream 15 is returned and
     * lastRNStreamNumber() now equals 15.  If i is less than or equal to lastRNStreamNumber(),
     * then no new streams are created, lastRNStreamNumber() stays at its current value and the ith
     * stream is returned.
     *
     * @param i the ith stream in the sequence of provided streams, must be 1, 2, 3 ...
     * @return the ith RNStreamIfc provided in the sequence of streams
     */
    RNStreamIfc rnStream(int i);

    /**
     *
     * @param stream the stream to find the number for
     * @return the stream number of the stream for this provider or -1 if the stream has not been
     * provided by this provider. Valid stream numbers start at 1.
     */
    int getStreamNumber(RNStreamIfc stream);

    /**
     *
     * @param stream the stream to find the number for
     * @return true if this provider has provided the stream
     */
    default boolean hasProvided(RNStreamIfc stream){
        return getStreamNumber(stream) > 0;
    }

    /**
     * Advances the state of the provider through n streams. Acts as if n streams were created, without
     * actually creating the streams.  lastRNStreamNumber() remains the same after calling
     * this method. In other words, this method should act as if nextRNStream() was not called but
     * advance the underlying stream mechanism as if n streams had been provided.
     *
     * @param n the number of times to advance
     */
    void advanceStreamMechanism(int n);

    /**
     * Causes the provider to act as if it has never created any streams. Thus, the next call
     * to nextRNStream() after the reset should return the 1st stream in the sequence of streams
     * that this provider would normally provide in the order in which they would be provided.
     */
    void resetRNStreamSequence();

    /**
     * Causes all streams that have been provided to be reset to the start of their stream. Thus,
     * the individual streams act as if they have not generated any pseudo-random numbers.
     * Note: This call only effects previously provided streams.
     */
    default void resetAllStreamsToStart() {
        int n = lastRNStreamNumber();
        for (int i = 1; i <= n; i++) {
            rnStream(i).resetStartStream();
        }
    }

    /**
     * Causes all streams that have been provided to be reset to the start of their current sub-stream. Thus,
     * the individual streams act as if they have not generated any pseudo-random numbers relative
     * to their current sub-stream.
     * Note: This call only effects previously provided streams.
     */
    default void resetAllStreamsToStartOfCurrentSubStream() {
        int n = lastRNStreamNumber();
        for (int i = 1; i <= n; i++) {
            rnStream(i).resetStartSubStream();
        }
    }

    /**
     * Causes all streams that have been provided to advance to the start of their next sub-stream. Thus,
     * the individual streams skip any pseudo-random numbers in their current sub-stream and are positioned
     * at the beginning of their next sub-stream.
     * Note: This call only effects previously provided streams.
     */
    default void advanceAllStreamsToNextSubstream() {
        int n = lastRNStreamNumber();
        for (int i = 1; i <= n; i++) {
            rnStream(i).advanceToNextSubStream();
        }
    }

    /**
     * Causes all streams that have been provided to change their antithetic option to the supplied
     * value.  Any new streams provided after this call will not necessarily have the same antithetic
     * option as previous streams if this method is called in the interim.
     * Note: This call only effects previously provided streams.
     *
     * @param option true means that the streams will now produce antithetic variates from the current
     *               position in their stream
     */
    default void setAllStreamsAntitheticOption(boolean option) {
        int n = lastRNStreamNumber();
        for (int i = 1; i <= n; i++) {
            rnStream(i).setAntitheticOption(option);
        }
    }
}
