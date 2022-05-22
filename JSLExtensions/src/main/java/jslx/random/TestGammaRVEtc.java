package jslx.random;

import jsl.utilities.distributions.Gamma;
import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;
import umontreal.ssj.probdist.ChiSquareDist;

public class TestGammaRVEtc {

    public static void main(String[] args) {
        RNStreamIfc stream = JSLRandom.getDefaultRNStream();

        for (int i=1; i<=1000; i++) {
            double u = stream.randU01();
            double c1 = Gamma.invChiSquareDistribution(u, 10.0);
            double c2 = ChiSquareDist.inverseF(10,u);
//            System.out.printf("u = %10.10f \t c1 = %10.10f \t c2 = %10.10f %n", u, c1, c2);
            if (!JSLMath.equal(c1, c2, 1E-5)) {
                System.out.printf("u = %10.10f \t c1 = %10.10f \t c2 = %10.10f %n", u, c1, c2);
                // does not match due to numerical approximations
                throw new IllegalStateException("c1 was not equal to c2");
            }
        }

    }
}
