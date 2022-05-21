package jslx.random.qmc;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.random.mcintegration.MCExperiment;
import jsl.utilities.random.mcmc.FunctionMVIfc;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.Objects;

/**
 * The purpose of this class is to facilitate the use of quasi-Monte Carlo
 * methods when solving multi-variable integration problems. This class
 * relies on no outside packages.
 * <p>
 * This class takes a simple approach to allowing QMC methods by
 * making some reasonable assumptions about the generation methods and implementing
 * a default QMC point set.
 * <p>
 * These assumptions may or not work well for specific integration problems.
 * For further investigation of these methods the user can reference the following paper.
 *
 * @see <a href="https://people.cs.kuleuven.be/~dirk.nuyens/taiwan/QMC-practical-guide-20161107-1up.pdf</a>
 * <p>
 * This class uses randomized QMC.  Thus, there is an inner loop that uses the deterministic
 * QMC sequence (point set) and an outer loop that averages over randomized executions of the QMC inner
 * loop results.  In general, the inner loop is over a large sequence (n) and the outer loop is
 * over a relatively smaller sequence (m).  Just as in MCMultiVariateIntegration, the outer loop
 * uses an absolute error stopping criterion after an initial sample. The main user control is
 * at this outer loop.
 * <p>
 * We assume that the supplied function has been standardized on the unit hypercube.
 * Since a sampler is not provided by the user, the dimension must be supplied by
 * the user.
 */
public class QMCMultiVariateIntegration extends MCExperiment {

    protected final FunctionMVIfc myFunction;
    // used to hold the uniforms that will make the quasi random numbers randomized
    protected final double[] uniforms;
    // the quasi random numbers with the proper dimension
    protected final double[] quasiU;
    protected final int myDimension;
    protected final RNStreamIfc myStream;
    protected boolean antitheticOptionOn = true;
    // square root of the first 100 prime numbers
    protected final double[] sqrtOfPrimes = {1.4142135623730951, 1.7320508075688772, 2.23606797749979,
            2.6457513110645907, 3.3166247903554, 3.605551275463989, 4.123105625617661,
            4.358898943540674, 4.795831523312719, 5.385164807134504, 5.5677643628300215,
            6.082762530298219, 6.4031242374328485, 6.557438524302, 6.855654600401044,
            7.280109889280518, 7.681145747868608, 7.810249675906654, 8.18535277187245,
            8.426149773176359, 8.54400374531753, 8.888194417315589, 9.1104335791443,
            9.433981132056603, 9.848857801796104, 10.04987562112089, 10.14889156509222,
            10.344080432788601, 10.44030650891055, 10.63014581273465, 11.269427669584644,
            11.445523142259598, 11.704699910719626, 11.789826122551595, 12.206555615733702,
            12.288205727444508, 12.529964086141668, 12.767145334803704, 12.922847983320086,
            13.152946437965905, 13.379088160259652, 13.45362404707371, 13.820274961085254,
            13.892443989449804, 14.035668847618199, 14.106735979665885, 14.52583904633395,
            14.933184523068078, 15.066519173319364, 15.132745950421556, 15.264337522473747,
            15.459624833740307, 15.524174696260024, 15.84297951775486, 16.0312195418814,
            16.217274740226856, 16.401219466856727, 16.46207763315433, 16.64331697709324,
            16.76305461424021, 16.822603841260722, 17.11724276862369, 17.52141546793523,
            17.635192088548397, 17.69180601295413, 17.804493814764857, 18.193405398660254,
            18.35755975068582, 18.627936010197157, 18.681541692269406, 18.788294228055936,
            18.947295321496416, 19.157244060668017, 19.313207915827967, 19.467922333931785,
            19.570385790780925, 19.72308292331602, 19.924858845171276, 20.024984394500787,
            20.223748416156685, 20.46948949045872, 20.518284528683193, 20.760539492026695,
            20.808652046684813, 20.952326839756964, 21.047565179849187, 21.18962010041709,
            21.37755832643195, 21.470910553583888, 21.517434791350013, 21.61018278497431,
            21.88606862823929, 22.06807649071391, 22.15851980616034, 22.338307903688676,
            22.427661492005804, 22.561028345356956, 22.825424421026653, 22.869193252058544,
            23.259406699226016
    };

    /**
     * @param dimension the dimension of the function to integrate
     * @param function  the function to integrate, must not be null
     */
    public QMCMultiVariateIntegration(int dimension, FunctionMVIfc function) {
        this(dimension, function, JSLRandom.nextRNStream(), true);
    }

    /**
     * @param dimension the dimension of the function to integrate
     * @param function  the function to integrate, must not be null
     */
    public QMCMultiVariateIntegration(int dimension, FunctionMVIfc function, boolean antitheticOptionOn) {
        this(dimension, function, JSLRandom.nextRNStream(), antitheticOptionOn);
    }

    /**
     * @param dimension          the dimension of the function to integrate
     * @param function           the function to integrate, must not be null
     * @param stream             the source of randomness
     * @param antitheticOptionOn true means to use antithetic sampling for the replication
     */
    public QMCMultiVariateIntegration(int dimension, FunctionMVIfc function, RNStreamIfc stream, boolean antitheticOptionOn) {
        Objects.requireNonNull(stream, "The RNStreamIfc was null!");
        Objects.requireNonNull(function, "The FunctionMVIfc was null!");
        if (dimension < 1) {
            throw new IllegalArgumentException("The dimension of the function must be 1 or more");
        }
        if (dimension > 100) {
            throw new IllegalArgumentException("The dimension of the function must be <= 100");
        }
        myStream = stream;
        myFunction = function;
        myDimension = dimension;
        uniforms = new double[myDimension];
        quasiU = new double[myDimension];
    }

    protected void generateQuasiPoints(int j, double[] w, double[] u) {
        //w holds the transforms until completed
        System.arraycopy(sqrtOfPrimes, 0, w, 0, w.length);// start with fresh q
//        System.out.println("w = q");
//        System.out.println(JSLArrayUtil.toCSVString(w));
//        System.out.println();
        w = JSLArrayUtil.multiplyConstant(w, j); //w = j*q
//        System.out.println("w = j*q");
//        System.out.println(JSLArrayUtil.toCSVString(w));
//        System.out.println();
        w = JSLArrayUtil.addElements(w, u);// w = j*q + u
//        System.out.println("j*q + u");
//        System.out.println(JSLArrayUtil.toCSVString(w));
//        System.out.println();
        // get fractional part
        JSLArrayUtil.remainder(w, 1.0);// w = {j*q+u}, where {} denotes fractional part
//        System.out.println("w = {j*q+u}");
//        System.out.println(JSLArrayUtil.toCSVString(w));
//        System.out.println();
        // now multiply by 2
        JSLArrayUtil.multiplyConstant(w, 2.0);//w = 2{j*q+u}
//        System.out.println("w = 2{j*q+u}");
//        System.out.println(JSLArrayUtil.toCSVString(w));
//        System.out.println();
        // now subtract the ones
        w = JSLArrayUtil.subtractConstant(w, 1.0);//w = 2{j*q+u} - 1
//        System.out.println("w = 2{j*q+u} - 1");
//        System.out.println(JSLArrayUtil.toCSVString(w));
//        System.out.println();
        JSLArrayUtil.abs(w); //w = |2{j*q+u} - 1|
//        System.out.println("w = |2{j*q+u} - 1|");
//        System.out.println(JSLArrayUtil.toCSVString(w));
//        System.out.println();
        System.arraycopy(w, 0, quasiU, 0, w.length);// copy into quasiU
    }

    private void fillUniforms() {
        for (int i = 0; i < uniforms.length; i++) {
            uniforms[i] = myStream.randU01();
        }
    }

    private double[] makeAntithetic(double[] u){
        for (int i = 0; i < u.length; i++) {
            u[i] = 1.0 - u[i];
        }
        return u;
    }

    /**
     * @return true if the antithetic option is on
     */
    public boolean isAntitheticOptionOn() {
        return antitheticOptionOn;
    }

    @Override
    protected void beforeMicroReplications() {
//        myStream.advanceToNextSubstream();
    }

    @Override
    protected double replication(int r) {
        fillUniforms();
        generateQuasiPoints(r, quasiU, uniforms);
//        System.out.println("Replication r = " + r);
//        System.out.println("Uniforms");
//        System.out.println(JSLArrayUtil.toCSVString(uniforms));
//        System.out.println();
//        System.out.println("Quasi Points");
//        System.out.println(JSLArrayUtil.toCSVString(quasiU));
//        System.out.println();

        if (isAntitheticOptionOn()) {
            double y1 = myFunction.fx(quasiU);
            double y2 = myFunction.fx(makeAntithetic(quasiU));
            return (y1 + y2) / 2.0;
        } else {
            return myFunction.fx(quasiU);
        }
    }
}
