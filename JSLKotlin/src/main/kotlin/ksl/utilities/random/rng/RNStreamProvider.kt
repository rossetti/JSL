package ksl.utilities.random.rng

import jsl.utilities.reporting.JSL

/**
 * A concrete implementation of RNStreamProviderIfc.  If more than streamNumberWarningLimit
 * streams are made a warning message is logged.  Generally, unless you know what you are doing
 * you should not need an immense number of streams.  Instead, use a small number of
 * streams many times. Conceptually this provider could have a possibly infinite number of streams,
 * which would have bad memory implications.  Thus, the reason for the warning.
 * The default stream if not set is the first stream.
 */
class RNStreamProvider(defaultStreamNum: Int = 1) : RNStreamProviderIfc {

    var streamNumberWarningLimit = 5000

    private val myStreamFactory: RNStreamFactory = RNStreamFactory()

    private val myStreams: MutableList<RNStreamIfc> = ArrayList()

    override val defaultStreamNumber: Int

    init {
        require(defaultStreamNum > 0) {
            "The default stream number must be > 0!"
        }
        defaultStreamNumber = defaultStreamNum
    }

    override fun nextRNStream(): RNStreamIfc {
        val stream = myStreamFactory.nextStream()
        myStreams.add(stream)
        if (myStreams.size > streamNumberWarningLimit) {
            //TODO change so logger is local to this class
            JSL.getInstance().LOGGER.warn("The number of streams made is now = {}", myStreams.size)
            JSL.getInstance().LOGGER.warn("Increase the stream warning limit if you don't want to see this message")
        }
        return stream
    }

    override fun lastRNStreamNumber() = myStreams.size

    override fun rnStream(i: Int): RNStreamIfc {
        if (i > lastRNStreamNumber()) {
            var stream: RNStreamIfc? = null
            for (j in lastRNStreamNumber()..i) {
                stream = nextRNStream()
            }
            // this is safe because there must be at least one call to nextRNStream()
            return stream!!
        }
        return myStreams[i - 1]
    }

    override fun streamNumber(stream: RNStreamIfc): Int {
        return if (myStreams.indexOf(stream) == -1) {
            -1
        } else myStreams.indexOf(stream) + 1
    }

    override fun advanceStreamMechanism(n: Int) {
        myStreamFactory.advanceSeeds(n)
    }

    override fun resetRNStreamSequence() {
        myStreams.clear()
        myStreamFactory.resetFactorySeed()
    }

    /**
     * Gets the default initial seed: seed = {12345, 12345, 12345,
     * 12345, 12345, 12345};
     *
     * @return an array holding the initial seed values
     */
    fun defaultInitialSeed(): LongArray {
        return myStreamFactory.defaultInitialFactorySeed()
    }

    /**
     * Returns the current seed
     *
     * @return the array of seed values for the current state
     */
    fun currentSeed(): LongArray {
        return myStreamFactory.getFactorySeed()
    }

    /**
     * Sets the initial seed to the six integers in the vector seed[0..5]. This
     * will be the seed (initial state) of the first stream. By default, this
     * seed is (12345, 12345, 12345, 12345, 12345, 12345).
     *
     * If it is	called,	the first 3 values of the seed must all be less than m1
     * = 4294967087, and not all 0; and the last 3 values must all be less than
     * m2 = 4294944443, and not all 0.
     *
     * @param seed the seeds
     */
    fun initialSeed(seed: LongArray) {
        myStreamFactory.setFactorySeed(seed)
    }
}