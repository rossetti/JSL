package jsl.utilities.statistic;

import jsl.simulation.JSLTooManyIterationsException;
import jsl.utilities.Interval;
import jsl.utilities.distributions.Gamma;
import jsl.utilities.distributions.Normal;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.reporting.JSL;
import jsl.utilities.rootfinding.BisectionRootFinder;
import jsl.utilities.rootfinding.RootFinder;

/**
 * Functions used to calculate Rinott constants
 *
 * Derived from Fortran code in
 *
 * 		Design and Analysis of Experiments for Statistical Selection,
 * 		Screening, and Multiple Comparisons
 *
 * 		Robert E. Bechhofer, Thomas J. Santner, David M. Goldsman
 *
 * 		ISBN: 978-0-471-57427-9
 * 		Wiley, 1995
 *
 * Original Fortran code available on the authors' website
 * http://www.stat.osu.edu/~tjs/REB-TJS-DMG/describe.html
 *
 * Converted to Java by Eric Ni, cn254@cornell.edu
 *
 * Revised, May 5, 2022, M. D. Rossetti, rossetti@uark.edu
 */
public class Rinott implements FunctionIfc {

    static private final double[] X = {.44489365833267018419E-1,
            .23452610951961853745,
            .57688462930188642649,
            .10724487538178176330E1,
            .17224087764446454411E1,
            .25283367064257948811E1,
            .34922132730219944896E1,
            .46164567697497673878E1,
            .59039585041742439466E1,
            .73581267331862411132E1,
            .89829409242125961034E1,
            .10783018632539972068E2,
            .12763697986742725115E2,
            .14931139755522557320E2,
            .17292454336715314789E2,
            .19855860940336054740E2,
            .22630889013196774489E2,
            .25628636022459247767E2,
            .28862101816323474744E2,
            .32346629153964737003E2,
            .36100494805751973804E2,
            .40145719771539441536E2,
            .44509207995754937976E2,
            .49224394987308639177E2,
            .54333721333396907333E2,
            .59892509162134018196E2,
            .65975377287935052797E2,
            .72687628090662708639E2,
            .80187446977913523067E2,
            .88735340417892398689E2,
            .98829542868283972559E2,
            .11175139809793769521E3};

    static private final double[] LNGAM = new double[50];
    static private final double[] WEX = new double[32];

    static {
        final double[] W = {.10921834195238497114,
                .21044310793881323294,
                .23521322966984800539,
                .19590333597288104341,
                .12998378628607176061,
                .70578623865717441560E-1,
                .31760912509175070306E-1,
                .11918214834838557057E-1,
                .37388162946115247897E-2,
                .98080330661495513223E-3,
                .21486491880136418802E-3,
                .39203419679879472043E-4,
                .59345416128686328784E-5,
                .74164045786675522191E-6,
                .76045678791207814811E-7,
                .63506022266258067424E-8,
                .42813829710409288788E-9,
                .23058994918913360793E-10,
                .97993792887270940633E-12,
                .32378016577292664623E-13,
                .81718234434207194332E-15,
                .15421338333938233722E-16,
                .21197922901636186120E-18,
                .20544296737880454267E-20,
                .13469825866373951558E-22,
                .56612941303973593711E-25,
                .14185605454630369059E-27,
                .19133754944542243094E-30,
                .11922487600982223565E-33,
                .26715112192401369860E-37,
                .13386169421062562827E-41,
                .45105361938989742322E-47
        };
        for (int i = 1; i <= 32; ++i) {
            WEX[i - 1] = W[i - 1] * Math.exp(X[i - 1]);
//            System.out.printf("wex[%d] = %f %n", (i-1), WEX[i-1]);
        }
//        System.out.println();
//        LNGAM[0] = 0.5723649429;
//        LNGAM[1] = 0.0;
//        for (int i = 2; i <= 25; ++i) {
//            LNGAM[2 * i - 2] = Math.log(i - 1.5) + LNGAM[2 * i - 4];
//            LNGAM[2 * i - 1] = Math.log(i - 1.0) + LNGAM[2 * i - 3];
//        }
//        for (int i = 0; i < LNGAM.length; i++) {
//            System.out.printf("LNGAM[%d] = %f %n", i, LNGAM[i]);
//        }
//
//        System.out.println();

        for (int i = 0; i < LNGAM.length; i++) {
            LNGAM[i] = Gamma.logGammaFunction((i + 1.0) / 2.0);
//            System.out.printf("LNGAM[%d] = %f %n", i, LNGAM[i]);
        }
    }

    private final RootFinder myRootFinder = new BisectionRootFinder();
    private final Interval myInterval = new Interval(0.0, 20.0);

    private double pStar = 0.975;
    private int dof = 50;
    private int numTreatments = 10;

    public Rinott() {
        myRootFinder.setMaximumIterations(200);
        myRootFinder.setInterval(this, myInterval);
    }

    @Override
    public double fx(double x) {
        return rinottIntegral(x) - pStar;
    }

    public double findRinottConstant(int numTreatments, int dof, double pStar){
        setNumTreatments(numTreatments);
        setDegreesOfFreedom(dof);
        setPStar(pStar);
        myRootFinder.setInitialPoint(4.0);
        myRootFinder.setInterval(myInterval);
        myRootFinder.evaluate();
        if (!myRootFinder.hasConverged()) {
            JSL.getInstance().LOGGER.warn("The Rinott constant calculation did not converge");
            return Double.NaN;
        }
        return (myRootFinder.getResult());
    }

    public void setPStar(double p) {
        if ((p <= 0.0) || (p >= 1.0)) {
            throw new IllegalArgumentException("P star in Rinott must be (0,1)");
        }
        pStar = p;
    }

    public void setDegreesOfFreedom(int dof) {
        if (dof < 4) {
            throw new IllegalArgumentException("Degrees of freedom in Rinott must be >=5");
        }
        this.dof = dof;
    }

    public void setNumTreatments(int n) {
        if (n <= 1) {
            throw new IllegalArgumentException("Number of treatments in Rinott must be >=2");
        }
        numTreatments = n;
    }

    public double rinottIntegral(double x) {
        double ans = 0.0;
        for (int j = 1; j <= WEX.length; ++j) {
            double tmp = 0.0;
            for (int i = 1; i <= WEX.length; ++i) {
                double z = x / Math.sqrt(dof * (1d / X[i - 1] + 1d / X[j - 1]));
                double zcdf = Normal.stdNormalCDF(z);
                double chi2pdf = chiSquaredPDF(dof, X[i - 1], LNGAM);
                tmp = tmp + WEX[i - 1] * zcdf * chi2pdf;
            }
            tmp = Math.pow(tmp, numTreatments - 1);
            ans = ans + WEX[j - 1] * tmp * chiSquaredPDF(dof, X[j - 1], LNGAM);
        }
        return ans;
    }

    /**
     * Chi-distribution PDF
     *
     * @param dof   Degree of freedom
     * @param x     The point of evaluation
     * @param lngam LNGAM(N) is LN(GAMMA(N/2))
     * @return The PDF of the Chi^2 distribution with N degrees of freedom for N &lt;= 50, evaluated at C
     */
    private static double chiSquaredPDF(int dof, double x, double[] lngam) {
        double dof2 = ((double) dof) / 2.0;
        double lng = 0.0;
        if (dof > LNGAM.length) {
            lng = Gamma.logGammaFunction(dof2);
        } else {
            lng = lngam[dof - 1];
        }
        double tmp = -dof2 * Math.log(2d) - lng + (dof2 - 1d) * Math.log(x) - x / 2.;
        return Math.exp(tmp);
    }
}
