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
package jsl.utilities.distributions;

/**
 * @author rossetti
 *
 */
public class ShiftedLossFunctionDistribution extends ShiftedDistribution
        implements LossFunctionDistributionIfc {

    /**
     * @param distribution the distribution to shift
     * @param shift the shift
     */
    public ShiftedLossFunctionDistribution(LossFunctionDistributionIfc distribution, double shift) {
        super((DistributionIfc) distribution, shift, null);
    }

    @Override
    public double firstOrderLossFunction(double x) {
        LossFunctionDistributionIfc cdf = (LossFunctionDistributionIfc) myDistribution;
        return cdf.firstOrderLossFunction(x - myShift);
    }

    @Override
    public double secondOrderLossFunction(double x) {
        LossFunctionDistributionIfc cdf = (LossFunctionDistributionIfc) myDistribution;
        return cdf.secondOrderLossFunction(x - myShift);
    }

    public static void main(String[] args) {
        ShiftedLossFunctionDistribution SD = new ShiftedLossFunctionDistribution(new Poisson(1.0), 0.5);
        Poisson p = new Poisson(1.0);

        System.out.println("PMF_P(1) =" + p.pmf(1));
        System.out.println("CDF(1.5) =" + SD.cdf(1.5));
        System.out.println("CDF_P(1) =" + p.cdf(1));
        System.out.println("CCDF(1.5) =" + SD.complementaryCDF(1.5));
        System.out.println("FOLF(1.5) =" + SD.firstOrderLossFunction(1.5));
        System.out.println("SOLF(1.5) =" + SD.secondOrderLossFunction(1.5));
        System.out.println("FOLF_P(1) =" + p.firstOrderLossFunction(1.0));
        System.out.println("SOLF_P(1) =" + p.secondOrderLossFunction(1.0));
    }
}
