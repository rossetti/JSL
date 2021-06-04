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
package test.random;

import jsl.utilities.math.JSLMath;
import jsl.utilities.distributions.Binomial;
import jsl.utilities.random.rng.RNStreamFactory;
import jsl.utilities.random.rng.RNStreamIfc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class BinomialTest {

    double[] pdf1 = {3.486784e-11, 1.627166e-09, 3.606885e-08, 5.049639e-07,
        5.007558e-06, 3.738977e-05, 2.181070e-04, 1.017833e-03,
        3.859282e-03, 1.200665e-02, 3.081708e-02, 6.536957e-02, 1.143967e-01,
        1.642620e-01, 1.916390e-01, 1.788631e-01,
        1.304210e-01, 7.160367e-02, 2.784587e-02, 6.839337e-03, 7.979227e-04};

    double[] cdf1 = {3.486784e-11, 1.662034e-09, 3.773088e-08, 5.426947e-07,
        5.550253e-06, 4.294002e-05, 2.610470e-04, 1.278880e-03,
        5.138162e-03, 1.714482e-02, 4.796190e-02, 1.133315e-01, 2.277282e-01,
        3.919902e-01, 5.836292e-01, 7.624922e-01,
        8.929132e-01, 9.645169e-01, 9.923627e-01, 9.992021e-01, 1.000000e+00};

    double[] flf1 = {14.0, 13.000000000034868, 12.000000001696902, 11.000000039427784,
        10.00000058212253, 9.000006132375608, 8.000049072397562, 7.000310119404621,
        6.001588999008842, 5.006727160543964, 4.023871976975222, 3.0718338743065665,
        2.1851653371835447, 1.4128935397653848, 0.8048837275644622, 0.3885128981169821,
        0.15100511923938242, 0.04391831473565233, 0.008435182437185773,
        7.979226629881708E-4, 1.4210854715202004E-14};

    double[] slf1 = {93.1, 80.09999999996512, 68.09999999826823, 57.09999995884044, 47.09999937671791,
        38.0999932443423, 30.099944171944742, 23.099634052540125, 17.09804505353128,
        12.09131789298732, 8.067445916012105, 4.995612041705542, 2.8104467045219934,
        1.3975531647566015, 0.5926694371921428, 0.20415653907515718, 0.05315141983577121,
        0.00923310510012243, 7.979226629402092E-4, -4.263256414560601E-14, -5.6843418860808015E-14};

    double[] pdf2 = {8.693156e-01, 1.217894e-01, 8.488576e-03, 3.924469e-04,
        1.353909e-05, 3.717728e-07, 8.463755e-09, 1.643119e-10,
        2.776760e-12, 4.149533e-14, 5.551812e-16, 6.717348e-18, 7.411064e-20,
        7.507532e-22, 7.024460e-24, 6.101502e-26,
        4.941863e-28, 3.746810e-30, 2.668348e-32, 1.790451e-34, 1.135045e-36};

    double[] cdf2 = {0.8693156, 0.9911051, 0.9995936, 0.9999861, 0.9999996,
        1.0000000, 1.0000000, 1.0000000, 1.0000000, 1.0000000, 1.0000000,
        1.0000000, 1.0000000, 1.0000000, 1.0000000, 1.0000000, 1.0000000,
        1.0000000, 1.0000000, 1.0000000, 1.0000000};

    double[] flf2 = {0.13999999999999999,
        0.009315618000120235, 4.206751276465137E-4, 1.430869390370293E-5, 3.892044912745529E-7,
        8.80084469190301E-9, 1.699524687648335E-10, 2.8150537456639313E-12, -1.04638520070921E-14,
        -5.920264278813647E-14, -6.641909244819999E-14, -7.308043059595093E-14, -7.974176874370187E-14,
        -8.640310689145281E-14, -9.306444503920375E-14, -9.972578318695469E-14, -1.0638712133470563E-13,
        -1.1304845948245656E-13, -1.197097976302075E-13, -1.2637113577795844E-13, -1.3303247392570938E-13};

    double[] slf2 = {0.009750999999999996,
        4.353819998797609E-4, 1.4706872233247181E-5, 3.9817832954425203E-7, 8.973838269699108E-9,
        1.7299357779609892E-10, 3.041109031265421E-12, 2.2605528560148969E-13, 2.365191376085818E-13,
        2.9572178039671826E-13, 3.6214087284491825E-13, 4.352213034408692E-13, 5.14963072184571E-13,
        6.013661790760239E-13, 6.944306241152276E-13, 7.941564073021823E-13, 9.005435286368879E-13,
        1.0135919881193445E-12, 1.133301785749552E-12, 1.2596729215275104E-12, 1.3927053954532198E-12};

    double[] pdf3 = {1.368915e-157, 9.582404e-155, 3.342662e-152, 7.747547e-150,
        1.342263e-147, 1.854112e-145, 2.127078e-143,
        2.084537e-141, 1.781411e-139, 1.348594e-137, 9.156951e-136, 5.632913e-134,
        3.165384e-132, 1.636260e-130,
        7.826777e-129, 3.482046e-127, 1.447225e-125, 5.641341e-124, 2.069537e-122,
        7.167132e-121, 2.349625e-119};

    double[] cdf3 = {1.368915e-157, 9.596093e-155, 3.352258e-152, 7.781070e-150,
        1.350044e-147, 1.867612e-145, 2.145755e-143,
        2.105994e-141, 1.802470e-139, 1.366618e-137, 9.293613e-136, 5.725849e-134,
        3.222642e-132, 1.668486e-130,
        7.993626e-129, 3.561982e-127, 1.482845e-125, 5.789626e-124, 2.127433e-122,
        7.379875e-121, 2.423423e-119};

   /** This is based on the precision available with the data checked from R
    *
    */
    double precision = 0.000001;

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void test1() {

        int n = 20;
        double p = 0.7;
        System.out.println("Test 1");
        System.out.println("n = " + n);
        System.out.println("p = " + p);
        
        RNStreamIfc rng = RNStreamFactory.getDefaultFactory().getStream();
        //RngStream rng = new RngStream();

        Binomial b = new Binomial(p, n);

        for (int i = 0; i <= 20; i++) {
            //System.out.println(b.pmf(i) + "\t" + pdf1[i]);

            assertTrue(JSLMath.equal(b.pmf(i), pdf1[i], precision));
            assertTrue(JSLMath.equal(b.cdf(i), cdf1[i], precision));
            assertTrue(JSLMath.equal(b.firstOrderLossFunction(i), flf1[i], precision));
            assertTrue(JSLMath.equal(b.secondOrderLossFunction(i), slf1[i], precision));
        }

        for (int i = 1; i <= 20; i++) {
            double u = rng.randU01();
            assertTrue(JSLMath.equal(b.invCDF(u), Binomial.binomialInvCDF(u, n, p), precision));
        }
    }

    @Test
    public void test2() {

        int n = 200;
        double p = 0.0007;
        System.out.println("Test 2");
        System.out.println("n = " + n);
        System.out.println("p = " + p);

        RNStreamIfc rng = RNStreamFactory.getDefaultFactory().getStream();

        Binomial b = new Binomial(p, n);

        for (int i = 0; i <= 20; i++) {
            assertTrue(JSLMath.equal(b.pmf(i), pdf2[i], precision));
            assertTrue(JSLMath.equal(b.cdf(i), cdf2[i], precision));
            assertTrue(JSLMath.equal(b.firstOrderLossFunction(i), flf2[i], precision));
            assertTrue(JSLMath.equal(b.secondOrderLossFunction(i), slf2[i], precision));
        }

        for (int i = 1; i <= 20; i++) {
            double u = rng.randU01();
            assertTrue(JSLMath.equal(b.invCDF(u), Binomial.binomialInvCDF(u, n, p), precision));
        }

    }

    @Test
    public void test3() {

        int n = 300;
        double p = 0.7;
        System.out.println("Test 3");
        System.out.println("n = " + n);
        System.out.println("p = " + p);

        RNStreamIfc rng = RNStreamFactory.getDefaultFactory().getStream();

        Binomial b = new Binomial(p, n);

        for (int i = 0; i <= 20; i++) {
            assertTrue(JSLMath.equal(b.pmf(i), pdf3[i], precision));
            assertTrue(JSLMath.equal(b.cdf(i), cdf3[i], precision));
        }

        for (int i = 1; i <= 20; i++) {
            double u = rng.randU01();
            assertTrue(JSLMath.equal(b.invCDF(u), Binomial.binomialInvCDF(u, n, p), precision));
        }

    }

    @Test
    public void test4() {

        System.out.println("Test 4");
        System.out.println("n = " + 20);
        System.out.println("p = " + 0.3);

        Binomial d = new Binomial(0.3, 20);
        d.setRecursiveAlgorithmFlag(true);
        double x = d.cdf(12);

        assertTrue(JSLMath.equal(x, Binomial.binomialCDF(12, 20, 0.3, false), precision));

        d.setRecursiveAlgorithmFlag(false);
        x = d.cdf(12);

        assertTrue(JSLMath.equal(x, Binomial.binomialCDF(12, 20, 0.3, false), precision));


    }

    @Test
    public void test5() {

        int n = 20;
        double p = 0.3;

        System.out.println("Test 5");
        System.out.println("n = " + 20);
        System.out.println("p = " + 0.3);

        for (int i = 0; i <= 20; i++) {
            double x = Binomial.binomialCDF(i, n, p, false);
            double xr = Binomial.binomialCDF(i, n, p, true);
            assertTrue(JSLMath.equal(x, xr, precision));
        }
    }
}
