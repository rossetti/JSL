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
package test.modeling;

import examples.queueing.DriverLicenseBureau;
import jsl.simulation.ExperimentGetIfc;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.statistic.StatisticAccessorIfc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class DriverLicenseBureauTest {

    private Simulation mySim;

    private ExperimentGetIfc myExp;

    private Model myModel;

    DriverLicenseBureau myDLB;

    @BeforeEach
    public void setUp() {

        mySim = new Simulation();
        myExp = mySim.getExperiment();
        myModel = mySim.getModel();
        // create the model element and attach it to the main model
        myDLB = new DriverLicenseBureau(myModel);

        // set the parameters of the experiment
        mySim.setNumberOfReplications(20);
        mySim.setLengthOfReplication(200000.0);
        mySim.setLengthOfWarmUp(50000.0);
    }

    @Test
    public void test1() {

        int k = 0;
        double p = 0.0;
        ExponentialRV d = new ExponentialRV(0.5);
        myDLB.setServiceDistributionInitialRandomSource(d);

        // run the simulation
        mySim.run();

        StatisticAccessorIfc sNB = myDLB.getNBAcrossReplicationStatistic();
        StatisticAccessorIfc sNS = myDLB.getNSAcrossReplicationStatistic();
        StatisticAccessorIfc sNQ = myDLB.getNQAcrossReplicationStatistic();

        k = sNB.getLeadingDigitRule(1.0) + 1;
        p = Math.pow(10.0, k);
        assertTrue(JSLMath.within(sNB.getAverage(), 0.5, p));

        k = sNS.getLeadingDigitRule(1.0) + 1;
        p = Math.pow(10.0, k);
        assertTrue(JSLMath.within(sNS.getAverage(), 1.0, p));

        k = sNQ.getLeadingDigitRule(1.0) + 1;
        p = Math.pow(10.0, k);
        assertTrue(JSLMath.within(sNQ.getAverage(), 0.5, p));

    }

    @Test
    public void test2() {

        int k = 0;
        double p = 0.0;
        ExponentialRV d = new ExponentialRV(0.8);
        myDLB.setServiceDistributionInitialRandomSource(d);

        // run the simulation
        mySim.run();

        StatisticAccessorIfc sNB = myDLB.getNBAcrossReplicationStatistic();
        StatisticAccessorIfc sNS = myDLB.getNSAcrossReplicationStatistic();
        StatisticAccessorIfc sNQ = myDLB.getNQAcrossReplicationStatistic();

        k = sNB.getLeadingDigitRule(1.0) + 1;
        p = Math.pow(10.0, k);
        assertTrue(JSLMath.within(sNB.getAverage(), 0.8, p));

        k = sNS.getLeadingDigitRule(1.0) + 1;
        p = Math.pow(10.0, k);
        assertTrue(JSLMath.within(sNS.getAverage(), 4.0, p));

        k = sNQ.getLeadingDigitRule(1.0) + 1;
        p = Math.pow(10.0, k);
        assertTrue(JSLMath.within(sNQ.getAverage(), 3.2, p));
    }
}
