package examples.general.utilities.random;

import jsl.utilities.distributions.Beta;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.BetaRV;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.reporting.JSL;

public class TestBetaDistribution {

    public static void main(String[] args) {
        BetaRV betaRV = new BetaRV(2.4, 5.5, 1);
        double mylnBetaA1A2 = Beta.logBetaFunction(2.4, 5.5);
//        RNStreamIfc stream = JSLRandom.rnStream(1);
        for (int i = 1; i <= 10; i++) {
//            double u = stream.randU01();
//            double x = Beta.stdBetaInvCDF(u, 2.4, 5.5, mylnBetaA1A2);
//            System.out.printf("u = %15.15f   x = %15.15f %n", u, x);
            System.out.println(betaRV.getValue());
        }
    }
}
