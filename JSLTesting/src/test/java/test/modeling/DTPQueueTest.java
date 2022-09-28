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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.modeling;

import examples.general.models.DTPFunctionalTest;
import examples.general.models.DTPQueueModel;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.modeling.elements.variable.AcrossReplicationStatisticIfc;
import jsl.utilities.math.JSLMath;
import jsl.utilities.random.rvariable.ExponentialRV;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author rossetti
 */
public class DTPQueueTest {

    double aNB, aNS, aNQ, aTQ, aST;
    double bNB, bNS, bNQ, bTQ, bST;

    public DTPQueueTest() {
    }

    @BeforeEach
    public void setUp() {
        Simulation sim = new Simulation("Drive Through Pharmacy");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DTPFunctionalTest dtp = new DTPFunctionalTest(m);
        dtp.setArrivalRS(new ExponentialRV(6.0));
        dtp.setServiceRS(new ExponentialRV(3.0));
        // set the parameters of the experiment
        sim.setNumberOfReplications(75);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        sim.run();
        r.printAcrossReplicationSummaryStatistics();
        AcrossReplicationStatisticIfc sNB = m.getAcrossReplicationResponseVariable("NumBusy");
        AcrossReplicationStatisticIfc sNS = m.getAcrossReplicationResponseVariable("# in System");
        AcrossReplicationStatisticIfc sNQ = m.getAcrossReplicationResponseVariable("PharmacyQ:NumInQ");
        AcrossReplicationStatisticIfc sTQ = m.getAcrossReplicationResponseVariable("PharmacyQ:TimeInQ");
        AcrossReplicationStatisticIfc sST = m.getAcrossReplicationResponseVariable("System Time");
        aNB = sNB.getAcrossReplicationAverage();
        aNS = sNS.getAcrossReplicationAverage();
        aNQ = sNQ.getAcrossReplicationAverage();
        aTQ = sTQ.getAcrossReplicationAverage();
        aST = sST.getAcrossReplicationAverage();
    }

    @Test
    public void test1() {
        Simulation sim = new Simulation("New Queue Testing");
        // get the model
        Model m = sim.getModel();
        // add DriveThroughPharmacy to the main model
        DTPQueueModel driveThroughPharmacy = new DTPQueueModel(m);
        driveThroughPharmacy.setArrivalRS(new ExponentialRV(6.0));
        driveThroughPharmacy.setServiceRS(new ExponentialRV(3.0));

        // set the parameters of the experiment
        sim.setNumberOfReplications(150);
        sim.setLengthOfReplication(20000.0);
        sim.setLengthOfWarmUp(5000.0);
        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        sim.run();
        r.printAcrossReplicationSummaryStatistics();
        AcrossReplicationStatisticIfc sNB = m.getAcrossReplicationResponseVariable("NumBusy");
        AcrossReplicationStatisticIfc sNS = m.getAcrossReplicationResponseVariable("# in System");
        AcrossReplicationStatisticIfc sNQ = m.getAcrossReplicationResponseVariable("PharmacyQ:NumInQ");
        AcrossReplicationStatisticIfc sTQ = m.getAcrossReplicationResponseVariable("PharmacyQ:TimeInQ");
        AcrossReplicationStatisticIfc sST = m.getAcrossReplicationResponseVariable("System Time");
        bNB = sNB.getAcrossReplicationAverage();
        bNS = sNS.getAcrossReplicationAverage();
        bNQ = sNQ.getAcrossReplicationAverage();
        bTQ = sTQ.getAcrossReplicationAverage();
        bST = sST.getAcrossReplicationAverage();
        int k;
        double p;

        k = sNB.getAcrossReplicationStatistic().getLeadingDigitRule(1.0) + 1;
        p = Math.pow(10.0, k);
        System.out.printf("k = %d, p = %f, aNB = %f, bNB = %f, aNB - bNB = %f%n", k, p, aNB, bNB, (aNB - bNB));
        assertTrue(JSLMath.within(aNB, bNB, p));
        k = sNS.getAcrossReplicationStatistic().getLeadingDigitRule(1.0) + 1;
        k = sNS.getAcrossReplicationStatistic().getLeadingDigitRule(1.0) + 2;
        p = Math.pow(10.0, k);
        System.out.printf("k = %d, p = %f, aNS = %f, bNS = %f, aNS - bNS = %f%n", k, p, aNS, bNS, (aNS - bNS));
        assertTrue(JSLMath.within(aNS, bNS, p));
        k = sNQ.getAcrossReplicationStatistic().getLeadingDigitRule(1.0) + 1;
        k = sNQ.getAcrossReplicationStatistic().getLeadingDigitRule(1.0) + 2;
        p = Math.pow(10.0, k);
        System.out.printf("k = %d, p = %f, aNQ = %f, bNQ = %f, aNQ - bNQ = %f%n", k, p, aNQ, bNQ, (aNQ - bNQ));
        assertTrue(JSLMath.within(aNQ, bNQ, p));
        k = sTQ.getAcrossReplicationStatistic().getLeadingDigitRule(1.0) + 1;
        k = sTQ.getAcrossReplicationStatistic().getLeadingDigitRule(1.0) + 2;
        p = Math.pow(10.0, k);
        System.out.printf("k = %d, p = %f, aTQ = %f, bTQ = %f, aTQ - bTQ = %f%n", k, p, aTQ, bTQ, (aTQ - bTQ));
        assertTrue(JSLMath.within(aTQ, bTQ, p));
        k = sST.getAcrossReplicationStatistic().getLeadingDigitRule(1.0) + 1;
        k = sST.getAcrossReplicationStatistic().getLeadingDigitRule(1.0) + 2;
        p = Math.pow(10.0, k);
        System.out.printf("k = %d, p = %f, aTST = %f, bTST = %f, aST - bST = %f%n", k, p, aST, bST, (aST - bST));
        assertTrue(JSLMath.within(aST, bST, p));
    }
}
