package jsl.utilities.random.rng;

import jsl.utilities.Identity;
import jsl.utilities.IdentityIfc;
import jsl.utilities.math.ArithmeticMod;

import java.util.Objects;

/**
 * An update (as of Jan-17-2021) of the MRG32k3a class from:
 * <p>
 * https://github.com/umontreal-simul/ssj  package umontreal.ssj.rng.MRG32k3a
 * <p>
 * This representation fits the stream architecture facilitated by the JSL and
 * (importantly) allows multiple instances of the factory to be instantiated. This
 * allows streams from different factories to produce the same streams and underlying
 * random numbers.
 */
public class RNStreamFactory extends Identity {

    private static RNStreamFactory DefaultFactory = new RNStreamFactory("Default");

    /**
     * A counter to count the number of created streams
     */
    private static int myStreamCounter_ = 0;

    /* private static constants global to all stream factories of this class */
    private static final double m1 = 4294967087.0;
    private static final double m2 = 4294944443.0;
    private static final double a12 = 1403580.0;
    private static final double a13n = 810728.0;
    private static final double a21 = 527612.0;
    private static final double a23n = 1370589.0;
    private static final double norm = 2.328306549295727688e-10;

    /* the following arrays are final and their elements should never be changed*/
    private static final double[][] A1p76 = {
            {82758667.0, 1871391091.0, 4127413238.0},
            {3672831523.0, 69195019.0, 1871391091.0},
            {3672091415.0, 3528743235.0, 69195019.0}
    };
    private static final double[][] A2p76 = {
            {1511326704.0, 3759209742.0, 1610795712.0},
            {4292754251.0, 1511326704.0, 3889917532.0},
            {3859662829.0, 4292754251.0, 3708466080.0}
    };
    private static final double[][] A1p127 = {
            {2427906178.0, 3580155704.0, 949770784.0},
            {226153695.0, 1230515664.0, 3580155704.0},
            {1988835001.0, 986791581.0, 1230515664.0}
    };
    private static final double[][] A2p127 = {
            {1464411153.0, 277697599.0, 1610723613.0},
            {32183930.0, 1464411153.0, 1022607788.0},
            {2824425944.0, 32183930.0, 2093834863.0}
    };

    /**
     * Multiply the first half of v by A with a modulo of m1
     * and the second half by B with a modulo of m2
     */
    private static void multMatVect(double[] v, double[][] A, double m1,
                                    double[][] B, double m2) {
        double[] vv = new double[3];
        for (int i = 0; i < 3; i++)
            vv[i] = v[i];
        ArithmeticMod.matVecModM(A, vv, vv, m1);
        for (int i = 0; i < 3; i++)
            v[i] = vv[i];
        for (int i = 0; i < 3; i++)
            vv[i] = v[i + 3];
        ArithmeticMod.matVecModM(B, vv, vv, m2);
        for (int i = 0; i < 3; i++)
            v[i + 3] = vv[i];
    }

    /**
     * Throws IllegalArgument exception if seed is not valid
     *
     * @param seed the seed to check
     */
    private static void validateSeed(long[] seed) {
        if (seed.length < 6)
            throw new IllegalArgumentException("Seed must contain 6 values");
        if (seed[0] == 0 && seed[1] == 0 && seed[2] == 0)
            throw new IllegalArgumentException
                    ("The first 3 values must not be 0");
        if (seed[3] == 0 && seed[4] == 0 && seed[5] == 0)
            throw new IllegalArgumentException
                    ("The last 3 values must not be 0");
        final long m1 = 4294967087L;
        if (seed[0] >= m1 || seed[1] >= m1 || seed[2] >= m1)
            throw new IllegalArgumentException
                    ("The first 3 values must be less than " + m1);
        final long m2 = 4294944443L;
        if (seed[3] >= m2 || seed[4] >= m2 || seed[5] >= m2)
            throw new IllegalArgumentException
                    ("The last 3 values must be less than " + m2);
    }

    /**
     * Use to check seeds to see if they are valid for the factory
     *
     * @param seed the seed to check
     * @return true if the supplied seed can be used, false otherwise
     */
    public static boolean checkSeed(long[] seed) {
        if (seed.length < 6)
            return false;
        if (seed[0] == 0 && seed[1] == 0 && seed[2] == 0)
            return false;
        if (seed[3] == 0 && seed[4] == 0 && seed[5] == 0)
            return false;
        final long m1 = 4294967087L;
        if (seed[0] >= m1 || seed[1] >= m1 || seed[2] >= m1)
            return false;
        final long m2 = 4294944443L;
        if (seed[3] >= m2 || seed[4] >= m2 || seed[5] >= m2)
            return false;
        return true;
    }

    /**
     * Default seed of the package and seed for the next stream to be created.
     * This represents the state of the factory. This array changes
     * as streams are created. Here it is initialized to the default starting point.
     */
    private final double[] nextSeed = {12345, 12345, 12345, 12345, 12345, 12345};

    /**
     * Creates a factory with no name
     */
    public RNStreamFactory() {
        this(null);
    }

    /**
     * Creates a factory with the provided name
     *
     * @param name a name for the factory, may be null
     */
    public RNStreamFactory(String name) {
        super(name);
    }

    /**
     * Returns a clone of the factory that will produce exactly the same streams.
     * The state of the returned factory will be exactly the same as the current factory.
     *
     * @return new instance of the factory
     */
    public RNStreamFactory newInstance() {
        return newInstance(getName() + " Clone");
    }

    /**
     * Returns a new instance of the factory that will produce exactly the same streams
     * The state of the returned factory will be exactly the same as the current factory.
     *
     * @param name the name for the factory, may be null
     * @return the new instance of the factory
     */
    public RNStreamFactory newInstance(String name) {
        RNStreamFactory f = new RNStreamFactory(name);
        f.setFactorySeed(getFactorySeed());
        return f;
    }

    /**
     * Returns a reference to a "global" stream factory
     *
     * @return a reference to a "global" stream factory
     */
    public static RNStreamFactory getDefaultFactory() {
        return DefaultFactory;
    }

    /**
     * Sets the default factory to the supplied factory
     *
     * @param f must not be null
     */
    public static void setDefaultFactory(RNStreamFactory f) {
        Objects.requireNonNull(f, "The supplied RNStreamFactory was null");
        DefaultFactory = f;
    }

    /**
     * Gets the default initial package seed: seed = {12345, 12345, 12345,
     * 12345, 12345, 12345};
     *
     * @return an array holding the initial seed values
     */
    public final long[] getDefaultInitialFactorySeed() {
        return new long[]{12345, 12345, 12345, 12345, 12345, 12345};
    }

    /**
     * Returns the current factory seed
     *
     * @return the array of seed values for the current state
     */
    public final long[] getFactorySeed() {
        long[] seed = new long[6];
        for (int i = 0; i < 6; ++i) {
            seed[i] = (long) nextSeed[i];
        }
        return seed;
    }

    /**
     * Sets the initial seed to the six integers in the vector seed[0..5]. This
     * will be the seed (initial state) of the first stream. By default, this
     * seed is (12345, 12345, 12345, 12345, 12345, 12345).
     * <p>
     * If it is	called,	the first 3 values of the seed must all be less than m1
     * = 4294967087, and not all 0; and the last 3 values must all be less than
     * m2 = 4294944443, and not all 0. Throws illegal argument exception for
     * invalid seeds
     *
     * @param seed the seeds
     */
    public final void setFactorySeed(long[] seed) {
        // Must use long because there is no unsigned int type.
        validateSeed(seed);
        for (int i = 0; i < 6; ++i) {
            nextSeed[i] = seed[i];
        }
    }

    /**
     * Resets the package seed to the default initial package seed: seed =
     * {12345, 12345, 12345, 12345, 12345, 12345};
     */
    public final void resetFactorySeed() {
        setFactorySeed(getDefaultInitialFactorySeed());
    }

    /**
     * Advances the seeds n times. Acts as if n streams were created, without
     * actually creating the streams. The seeds will be advanced 2^127 steps
     *
     * @param n the number of times to advance
     */
    public final void advanceSeeds(int n) {
        for (int k = 1; k <= n; k++) {
            multMatVect(nextSeed, A1p127, m1, A2p127, m2);
        }
    }

    /**
     * Tells the provider to make and return a RNStream with the provided name
     *
     * @param name can be null
     * @return the made stream
     */
    public final RNStreamIfc getStream(String name) {
        // create the stream using the current seed state of the factory
        return new RNStream(name);
    }

    /**
     * Tells the factory to make and return a RNStream
     *
     * @return the made stream
     */
    public final RNStreamIfc getStream() {
        return getStream(null);
    }

    /**
     * Instances of RNStream are what is made by the factory. Each created
     * stream should be 2 to 127 {@literal 2^127} steps ahead of the last stream created.
     */
    public class RNStream implements IdentityIfc, RNStreamIfc, RNStreamNewInstanceIfc, GetAntitheticStreamIfc  {

        /**
         * Describes the stream (for writing the state, error messages, etc.).
         */
        private String myName;

        /**
         * The id of this object
         */
        private final int myId;

        // The arrays Cg, Bg and Ig contain the current state,
        // the starting point of the current substream,
        // and the starting point of the stream, respectively.
        private double Cg0, Cg1, Cg2, Cg3, Cg4, Cg5;
        private final double[] Bg = new double[6];
        private final double[] Ig = new double[6];

        /**
         * This stream generates antithetic variates if and only if {\tt anti =
         * true}.
         */
        private boolean anti;

        /**
         * The previous U generated (returned) by randU01()
         */
        private double myPrevU;

        private boolean advanceToNextSubStreamOption = true;

        private boolean resetStartStreamOption = true;

        private RNStream() {
            this(null);
        }

        /**
         * Makes a stream with the given name
         *
         * @param name the name of the stream
         */
        private RNStream(String name) {
            myStreamCounter_ = myStreamCounter_ + 1;
            myId = myStreamCounter_;
            setName(name);
            anti = false;
            myPrevU = Double.NaN;
            // copies the current factory seed value to the initial state vector
            // of the newly created stream
            for (int i = 0; i < 6; i++)
                Ig[i] = nextSeed[i];
            // cause Bg to be set to Ig, and Cg set to Bg
            resetStartStream();
            // advances nextSeed by 2^127 steps, to be ready for next stream
            multMatVect(nextSeed, A1p127, m1, A2p127, m2);
        }

        public RNStream newInstance() {
            return newInstance(null);
        }

        public RNStream newInstance(String name) {
            RNStream s = new RNStream(name);
            s.anti = anti;
            s.myPrevU = myPrevU;
            s.Cg0 = Cg0;
            s.Cg1 = Cg1;
            s.Cg2 = Cg2;
            s.Cg3 = Cg3;
            s.Cg4 = Cg4;
            s.Cg5 = Cg5;
            for (int i = 0; i < 6; ++i) {
                s.Bg[i] = Bg[i];
                s.Ig[i] = Ig[i];
            }
            return s;
        }

        @Override
        public RNStream newAntitheticInstance() {
            return newAntitheticInstance(null);
        }

        @Override
        public RNStream newAntitheticInstance(String name) {
            RNStream s = newInstance(name);
            if (s.getAntitheticOption()) {
                s.setAntitheticOption(false);
            } else {
                s.setAntitheticOption(true);
            }
            return s;
        }

        @Override
        public final String getName() {
            return myName;
        }

        @Override
        public final int getId() {
            return (myId);
        }

        /**
         * Sets the name
         *
         * @param str The name as a string.
         */
        public final void setName(String str) {
            if (str == null) {
                myName = this.getClass().getSimpleName();
            } else {
                myName = str;
            }
        }

        @Override
        public final void resetStartStream() {
            for (int i = 0; i < 6; ++i)
                Bg[i] = Ig[i];
            resetStartSubStream();
            RNStreamProvider.logger.trace("Resetting stream {} to the start of its stream", getId());
            RNStreamProvider.logger.trace(this.toString());
        }

        @Override
        public final void resetStartSubStream() {
            Cg0 = Bg[0];
            Cg1 = Bg[1];
            Cg2 = Bg[2];
            Cg3 = Bg[3];
            Cg4 = Bg[4];
            Cg5 = Bg[5];
        }

        @Override
        public final void advanceToNextSubStream() {
            multMatVect(Bg, A1p76, m1, A2p76, m2);
            resetStartSubStream();
            RNStreamProvider.logger.trace("Advancing stream {} to the start of its next sub-stream", getId());
            RNStreamProvider.logger.trace(this.toString());
        }

        /**
         * Returns the seed for the start of the substream
         *
         * @return the seed for the start of the substream
         */
        public final long[] getStartSubStreamSeed() {
            long[] seed = new long[6];
            for (int i = 0; i < 6; ++i) {
                seed[i] = (long) Bg[i];
            }
            return seed;
        }

        @Override
        public final void setAntitheticOption(boolean option) {
            anti = option;
        }

        @Override
        public final boolean getAntitheticOption() {
            return anti;
        }

        /**
         * Returns the current state C_g of this stream. This is a vector of 6
         * integers. This method is convenient if we want to save the state for
         * subsequent use.
         *
         * @return the current state of the generator
         */
        public final long[] getState() {
            return new long[]{(long) Cg0, (long) Cg1, (long) Cg2,
                    (long) Cg3, (long) Cg4, (long) Cg5};
        }

        @Override
        public final double randU01() {
            double u = U01();
            myPrevU = u;
            return u;
        }

        @Override
        public final double getPrevU01() {
            return myPrevU;
        }

        @Override
        public final double getAntitheticValue() {
            return 1.0 - myPrevU;
        }

        @Override
        public final int randInt(int i, int j) {
            if (i > j) {
                throw new IllegalArgumentException("The lower limit must be <= the upper limit");
            }
            return (i + (int) (randU01() * (j - i + 1)));
        }

        /**
            The primary method for getting pseudo-random numbers
         */
        private double U01() {
            int k;
            double p1, p2;
            /* Component 1 */
            p1 = a12 * Cg1 - a13n * Cg0;
            k = (int) (p1 / m1);
            p1 -= k * m1;
            if (p1 < 0.0)
                p1 += m1;
            Cg0 = Cg1;
            Cg1 = Cg2;
            Cg2 = p1;
            /* Component 2 */
            p2 = a21 * Cg5 - a23n * Cg3;
            k = (int) (p2 / m2);
            p2 -= k * m2;
            if (p2 < 0.0)
                p2 += m2;
            Cg3 = Cg4;
            Cg4 = Cg5;
            Cg5 = p2;
            /* Combination */
            double u = ((p1 > p2) ? (p1 - p2) * norm : (p1 - p2 + m1) * norm);
            return (anti) ? (1 - u) : u;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("The RngStream\n");
            sb.append("Name: ");
            sb.append(getName());
            sb.append("\n");
            sb.append("Id: ");
            sb.append(getId());
            sb.append("\n");

            sb.append(":\n   anti = ");
            sb.append((anti ? "true" : "false"));
            sb.append("\n");

            sb.append("   Ig = { ");
            for (int i = 0; i < 5; i++) {
                sb.append((long) Ig[i]);
                sb.append(", ");
            }
            sb.append((long) Ig[5]);
            sb.append(" }");
            sb.append("\n");

            sb.append("   Bg = { ");
            for (int i = 0; i < 5; i++) {
                sb.append((long) Bg[i]);
                sb.append(", ");
            }
            sb.append((long) Bg[5]);
            sb.append(" }");
            sb.append("\n");

            long[] Cg = getState();
            sb.append("   Cg = { ");
            for (int i = 0; i < 5; i++) {
                sb.append(Cg[i]);
                sb.append(", ");
            }
            sb.append(Cg[5]);
            sb.append(" }");
            sb.append("\n");

            return sb.toString();
        }

        @Override
        public boolean getResetNextSubStreamOption() {
            return advanceToNextSubStreamOption;
        }

        @Override
        public boolean getResetStartStreamOption() {
            return resetStartStreamOption;
        }

        @Override
        public void setResetNextSubStreamOption(boolean b) {
            advanceToNextSubStreamOption = b;
        }

        @Override
        public void setResetStartStreamOption(boolean b) {
            resetStartStreamOption = b;
        }
    }

//    public static void main(String[] args) {
//        double sumTest = sumTest(1000);
//        double jumpStreamTest = jumpStreamTest(20, 100);
//        System.out.println("sumTest = " + sumTest);
//        System.out.println("jumpStreamTest = " + jumpStreamTest);
//    }
//
//    public static double sumTest(int n){
//        RNStreamFactory rm = new RNStreamFactory();
//        RNStreamIfc rng = rm.getStream();
//        double sum = 0.0;
//        for (int i = 1; i <= n; i++) {
//            sum = sum + rng.randU01();
//        }
//        return sum;
//    }
//
//    public static double jumpStreamTest(int advance, int count){
//        RNStreamFactory rm = new RNStreamFactory();
//        rm.advanceSeeds(advance);
//        RNStreamIfc rng = rm.getStream();
//        double sum = 0.0;
//        for (int i = 1; i <= count; i++) {
//            sum = sum + rng.randU01();
//        }
//        return sum;
//    }
//
//    public static void test1(){
//        RNStreamFactory rm = new RNStreamFactory();
//        RNStreamIfc rng = rm.getStream();
//
//        for (int i = 0; i < 9; i++) {
//            System.out.println(rng.randU01());
//        }
//        RNStreamIfc rng2 = rm.getStream();
//
//        System.out.println();
//        for (int i = 0; i < 9; i++) {
//            System.out.println(rng2.randU01());
//        }
//
//        System.out.println();
//        rng.advanceToNextSubstream();
//        for (int i = 0; i < 9; i++) {
//            System.out.println(rng.randU01());
//        }
//
//        System.out.println();
//        rm.advanceSeeds(20);
//        for (int i = 0; i < 9; i++) {
//            System.out.println(rng.randU01());
//        }
//    }
}
