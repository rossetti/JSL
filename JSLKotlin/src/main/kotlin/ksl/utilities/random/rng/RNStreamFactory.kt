package ksl.utilities.random.rng

import jsl.utilities.random.rng.RNStreamFactory
import ksl.utilities.IdentityIfc

class RNStreamFactory {

    companion object {
        private val DefaultFactory = RNStreamFactory("Default")

        /**
         * A counter to count the number of created streams
         */
        private var myStreamCounter_ = 0

        /* private static constants global to all stream factories of this class */
        private const val m1 = 4294967087.0
        private const val m2 = 4294944443.0
        private const val a12 = 1403580.0
        private const val a13n = 810728.0
        private const val a21 = 527612.0
        private const val a23n = 1370589.0
        private const val norm = 2.328306549295727688e-10

        /* the following arrays are final and their elements should never be changed*/
        private val A1p76 = arrayOf(
                doubleArrayOf(82758667.0, 1871391091.0, 4127413238.0),
                doubleArrayOf(3672831523.0, 69195019.0, 1871391091.0),
                doubleArrayOf(3672091415.0, 3528743235.0, 69195019.0))

        private val A2p76 = arrayOf(
                doubleArrayOf(1511326704.0, 3759209742.0, 1610795712.0),
                doubleArrayOf(4292754251.0, 1511326704.0, 3889917532.0),
                doubleArrayOf(3859662829.0, 4292754251.0, 3708466080.0))

        private val A1p127 = arrayOf(
                doubleArrayOf(2427906178.0, 3580155704.0, 949770784.0),
                doubleArrayOf(226153695.0, 1230515664.0, 3580155704.0),
                doubleArrayOf(1988835001.0, 986791581.0, 1230515664.0))

        private val A2p127 = arrayOf(
                doubleArrayOf(1464411153.0, 277697599.0, 1610723613.0),
                doubleArrayOf(32183930.0, 1464411153.0, 1022607788.0),
                doubleArrayOf(2824425944.0, 32183930.0, 2093834863.0))

        /**
         * Multiply the first half of v by A with a modulo of m1
         * and the second half by B with a modulo of m2
         */
        private fun multMatVect(v: DoubleArray, A: Array<DoubleArray>, m1: Double,
                                B: Array<DoubleArray>, m2: Double) {
            val vv = DoubleArray(3)
            for (i in 0..2) vv[i] = v[i]
            ArithmeticMod.matVecModM(A, vv, vv, m1)
            for (i in 0..2) v[i] = vv[i]
            for (i in 0..2) vv[i] = v[i + 3]
            ArithmeticMod.matVecModM(B, vv, vv, m2)
            for (i in 0..2) v[i + 3] = vv[i]
        }

        /**
         * Throws IllegalArgument exception if seed is not valid
         *
         * @param seed the seed to check
         */
        private fun validateSeed(seed: LongArray) {
            require(seed.size >= 6) { "Seed must contain 6 values" }
            require(!(seed[0] == 0L && seed[1] == 0L && seed[2] == 0L)) { "The first 3 values must not be 0" }
            require(!(seed[3] == 0L && seed[4] == 0L && seed[5] == 0L)) { "The last 3 values must not be 0" }
            val m1 = 4294967087L
            require(!(seed[0] >= m1 || seed[1] >= m1 || seed[2] >= m1)) { "The first 3 values must be less than $m1" }
            val m2 = 4294944443L
            require(!(seed[3] >= m2 || seed[4] >= m2 || seed[5] >= m2)) { "The last 3 values must be less than $m2" }
        }

        /**
         * Use to check seeds to see if they are valid for the factory
         *
         * @param seed the seed to check
         * @return true if the supplied seed can be used, false otherwise
         */
        fun checkSeed(seed: LongArray): Boolean {
            if (seed.size < 6) return false
            if (seed[0] == 0L && seed[1] == 0L && seed[2] == 0L) return false
            if (seed[3] == 0L && seed[4] == 0L && seed[5] == 0L) return false
            val m1 = 4294967087L
            if (seed[0] >= m1 || seed[1] >= m1 || seed[2] >= m1) return false
            val m2 = 4294944443L
            return if (seed[3] >= m2 || seed[4] >= m2 || seed[5] >= m2) false else true
        }

    }

    /**
     * Default seed of the package and seed for the next stream to be created.
     * This represents the state of the factory. This array changes
     * as streams are created. Here it is initialized to the default starting point.
     */
    private val nextSeed = doubleArrayOf(12345.0, 12345.0, 12345.0, 12345.0, 12345.0, 12345.0)


    /**
     * Gets the default initial package seed: seed = {12345, 12345, 12345,
     * 12345, 12345, 12345};
     *
     * @return an array holding the initial seed values
     */
    fun defaultInitialFactorySeed(): LongArray {
        return longArrayOf(12345, 12345, 12345, 12345, 12345, 12345)
    }

    /**
     * Returns the current factory seed
     *
     * @return the array of seed values for the current state
     */
    fun getFactorySeed(): LongArray {
        val seed = LongArray(6)
        for (i in 0..5) {
            seed[i] = nextSeed[i].toLong()
        }
        return seed
    }

    /**
     * Sets the initial seed to the six integers in the vector seed[0..5]. This
     * will be the seed (initial state) of the first stream. By default, this
     * seed is (12345, 12345, 12345, 12345, 12345, 12345).
     *
     *
     * If it is	called,	the first 3 values of the seed must all be less than m1
     * = 4294967087, and not all 0; and the last 3 values must all be less than
     * m2 = 4294944443, and not all 0. Throws illegal argument exception for
     * invalid seeds
     *
     * @param seed the seeds
     */
    fun setFactorySeed(seed: LongArray) {
        // Must use long because there is no unsigned int type.
        validateSeed(seed)
        for (i in 0..5) {
            nextSeed[i] = seed[i].toDouble()
        }
    }

    /**
     * Resets the package seed to the default initial package seed: seed =
     * {12345, 12345, 12345, 12345, 12345, 12345};
     */
    fun resetFactorySeed() {
        setFactorySeed(defaultInitialFactorySeed())
    }

    /**
     * Advances the seeds n times. Acts as if n streams were created, without
     * actually creating the streams. The seeds will be advanced 2^127 steps
     *
     * @param n the number of times to advance
     */
    fun advanceSeeds(n: Int) {
        for (k in 1..n) {
            multMatVect(nextSeed, A1p127, m1, A2p127, m2)
        }
    }

    /**
     * Tells the provider to make and return a RNStream with the provided name
     *
     * @param name can be null
     * @return the made stream
     */
    fun nextStream(name: String?): RNStreamIfc {
        // create the stream using the current seed state of the factory
        return RNStream(name)
    }

    /**
     * Tells the factory to make and return a RNStream
     *
     * @return the made stream
     */
    fun nextStream(): RNStreamIfc {
        return nextStream(null)
    }

    /**
     * Instances of RNStream are what is made by the factory. Each created
     * stream should be 2 to 127 {@literal 2^127} steps ahead of the last stream created.
     */
    inner class RNStream(sName: String?) : IdentityIfc, RNStreamIfc,
            RNStreamNewInstanceIfc, GetAntitheticStreamIfc {

        /**
         * The id of this object
         */
        override val id = ++myStreamCounter_

        /**
         * Describes the stream (for writing the state, error messages, etc.).
         */
        override var name: String? = sName ?: javaClass.simpleName + "#" + id

        override var label: String? = null

        /**
         * This stream generates antithetic variates if and only if {\tt anti = true}.
         */
        override var antithetic = false

        /**
         * The previous U generated (returned) by randU01()
         */
        override var previousU = Double.NaN
            private set

        override val antitheticValue: Double
            get() = 1.0 - previousU

        // The arrays Cg, Bg and Ig contain the current state,
        // the starting point of the current substream,
        // and the starting point of the stream, respectively.
        private var Cg0 = 0.0
        private var Cg1 = 0.0
        private var Cg2 = 0.0
        private var Cg3 = 0.0
        private var Cg4 = 0.0
        private var Cg5 = 0.0
        private val Bg = DoubleArray(6)
        private val Ig = DoubleArray(6)

        init {
            //previousU = Double.NaN
            // copies the current factory seed value to the initial state vector
            // of the newly created stream
            // copies the current factory seed value to the initial state vector
            // of the newly created stream
            for (i in 0..5) Ig[i] = nextSeed[i]
            // cause Bg to be set to Ig, and Cg set to Bg
            // cause Bg to be set to Ig, and Cg set to Bg
            resetStartStream()
            // advances nextSeed by 2^127 steps, to be ready for next stream
            // advances nextSeed by 2^127 steps, to be ready for next stream
            multMatVect(nextSeed, A1p127, m1, A2p127, m2)
        }

        override fun resetStartStream() {
            for (i in 0..5) Bg[i] = Ig[i]
            resetStartSubstream()
        }

        override fun resetStartSubstream() {
            Cg0 = Bg[0]
            Cg1 = Bg[1]
            Cg2 = Bg[2]
            Cg3 = Bg[3]
            Cg4 = Bg[4]
            Cg5 = Bg[5]
        }

        override fun advanceToNextSubstream() {
            multMatVect(Bg, A1p76, m1, A2p76, m2)
            resetStartSubstream()
        }

        /**
         * Returns the seed for the start of the substream
         *
         * @return the seed for the start of the substream
         */
        fun startingSubStreamSeed(): LongArray {
            val seed = LongArray(6)
            for (i in 0..5) {
                seed[i] = Bg[i].toLong()
            }
            return seed
        }

        /**
         * Returns the current state C_g of this stream. This is a vector of 6
         * integers. This method is convenient if we want to save the state for
         * subsequent use.
         *
         * @return the current state of the generator
         */
        fun state(): LongArray {
            return longArrayOf(Cg0.toLong(), Cg1.toLong(), Cg2.toLong(),
                    Cg3.toLong(), Cg4.toLong(), Cg5.toLong())
        }

        override fun randU01(): Double {
            previousU = U01()
            return previousU
        }

        /**
         * The primary method for getting pseudo-random numbers
         */
        private fun U01(): Double {
            /* Component 1 */
            var p1: Double = a12 * Cg1 - a13n * Cg0
            var k: Int = (p1 / m1).toInt()
            p1 -= k * m1
            if (p1 < 0.0) p1 += m1
            Cg0 = Cg1
            Cg1 = Cg2
            Cg2 = p1
            /* Component 2 */
            var p2: Double = a21 * Cg5 - a23n * Cg3
            k = (p2 / m2).toInt()
            p2 -= k * m2
            if (p2 < 0.0) p2 += m2
            Cg3 = Cg4
            Cg4 = Cg5
            Cg5 = p2
            /* Combination */
            val u = if (p1 > p2) (p1 - p2) * norm else (p1 - p2 + m1) * norm
            return if (antithetic) 1 - u else u
        }

        override fun randInt(i: Int, j: Int): Int {
            require(i <= j) { "The lower limit must be <= the upper limit" }
            return i + (randU01() * (j - i + 1)).toInt()
        }

        override fun newInstance(): RNStreamIfc {
            return RNStream(null)
        }

        override fun newInstance(name: String?): RNStreamIfc {
            val s: RNStream = RNStream(name)
            s.antithetic = antithetic
            s.previousU = previousU
            s.Cg0 = Cg0
            s.Cg1 = Cg1
            s.Cg2 = Cg2
            s.Cg3 = Cg3
            s.Cg4 = Cg4
            s.Cg5 = Cg5
            for (i in 0..5) {
                s.Bg[i] = Bg[i]
                s.Ig[i] = Ig[i]
            }
            return s
        }

        override fun newAntitheticInstance(name: String?): RNStreamIfc {
            val s = newInstance(name)
            s.antithetic = !s.antithetic
            return s
        }

        override fun newAntitheticInstance(): RNStreamIfc {
            return newAntitheticInstance(null)
        }


    }

}