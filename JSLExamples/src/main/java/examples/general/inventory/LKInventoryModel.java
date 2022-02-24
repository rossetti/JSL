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
package examples.general.inventory;

import java.lang.Math;

import jsl.modeling.elements.variable.*;
import jsl.modeling.elements.*;
import jsl.simulation.*;
import jsl.utilities.random.*;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.random.rvariable.DEmpiricalRV;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.UniformRV;

/**
 *
 */
public class LKInventoryModel extends SchedulingElement {

    private int myOrderUpToLevel = 40;

    private int myReorderPoint = 20;

    private double myHoldingCost = 1.0;

    private double myCostPerItem = 3.0;

    private double myBackLogCost = 5.0;

    private double mySetupCost = 32;

    private double myInitialInventoryLevel = 60;

    private RandomVariable myLeadTime;

    private RandomVariable myDemandAmount;

    private TimeWeighted myInvLevel;

    private TimeWeighted myPosInv;

    private TimeWeighted myNegInv;

    private TimeWeighted myAvgTotalCost;

    private TimeWeighted myAvgHoldingCost;

    private TimeWeighted myAvgSetupCost;

    private TimeWeighted myAvgShortageCost;

    private EventGenerator myDemandGenerator;

    private EventGenerator myInventoryCheckGenerator;

    private OrderArrival myOrderArrivalListener;

    /**
     * Creates a new instance of LKInventoryModel
     */
    public LKInventoryModel(ModelElement parent) {
        super(parent);
        myDemandGenerator =
                new EventGenerator(this, new DemandArrival(),
                        new ExponentialRV(0.1), new ExponentialRV(0.1), "Demand Generator");
        myInventoryCheckGenerator =
                new EventGenerator(this, new InventoryCheck(), ConstantRV.ZERO, ConstantRV.ONE, "Inventory Check");
        myLeadTime = new RandomVariable(this, new UniformRV(0.5, 1.0));
        RandomIfc d = new DEmpiricalRV(new double[]{1.0, 2.0, 3.0, 4.0},
                                       new double[]{1.0 / 6.0, 3.0 / 6.0, 5.0 / 6.0, 1.0});
        myDemandAmount = new RandomVariable(this, d);
        myOrderArrivalListener = new OrderArrival();
        myInvLevel = new TimeWeighted(this, 0.0, "Inventory Level");
        myNegInv = new TimeWeighted(this, 0.0, "BackOrder Level");
        myPosInv = new TimeWeighted(this, 0.0, "On Hand Level");
        myAvgTotalCost = new TimeWeighted(this, "Avg Total Cost");
        myAvgSetupCost = new TimeWeighted(this, "Avg Setup Cost");
        myAvgHoldingCost = new TimeWeighted(this, "Avg Holding Cost");
        myAvgShortageCost = new TimeWeighted(this, "Avg Shortage Cost");
    }

    public void setInitialInventoryLevel(double level) {
        myInitialInventoryLevel = level;
        myInvLevel.setInitialValue(myInitialInventoryLevel);
        myPosInv.setInitialValue(Math.max(0, myInvLevel.getInitialValue()));
        myNegInv.setInitialValue(-Math.min(0, myInvLevel.getInitialValue()));
        myAvgHoldingCost.setInitialValue(myHoldingCost * myPosInv.getInitialValue());
        myAvgShortageCost.setInitialValue(myBackLogCost * myNegInv.getInitialValue());
        myAvgSetupCost.setInitialValue(0.0);
        double cost =
                myAvgSetupCost.getInitialValue() + myAvgHoldingCost.getInitialValue() + myAvgShortageCost.getInitialValue();
        myAvgTotalCost.setInitialValue(cost);

    }

    public void setReorderPoint(int level) {
        myReorderPoint = level;
    }

    public void setOrderUpToLevel(int level) {
        myOrderUpToLevel = level;
    }

    protected void initialize() {
        super.initialize();
        setInitialInventoryLevel(myInitialInventoryLevel);
    }

    private void scheduleReplenishment(double orderSize) {
        double t = myLeadTime.getValue();
        scheduleEvent(myOrderArrivalListener, t, Double.valueOf(orderSize));
    }

    private class DemandArrival implements EventGeneratorActionIfc {

        public void generate(EventGenerator generator, JSLEvent event) {
            myInvLevel.decrement(myDemandAmount.getValue());
            myPosInv.setValue(Math.max(0, myInvLevel.getValue()));
            myNegInv.setValue(-Math.min(0, myInvLevel.getValue()));
            myAvgHoldingCost.setValue(myHoldingCost * myPosInv.getValue());
            myAvgShortageCost.setValue(myBackLogCost * myNegInv.getValue());
            double cost = myAvgSetupCost.getValue() + myAvgHoldingCost.getValue() + myAvgShortageCost.getValue();
            myAvgTotalCost.setValue(cost);
        }
    }

    private class InventoryCheck implements EventGeneratorActionIfc {

        public void generate(EventGenerator generator, JSLEvent event) {
            if (myInvLevel.getValue() < myReorderPoint) {
                double orderSize = myOrderUpToLevel - myInvLevel.getValue();
                scheduleReplenishment(orderSize);
                myAvgSetupCost.setValue(mySetupCost + myCostPerItem * orderSize);
            } else {
                myAvgSetupCost.setValue(0.0);
            }
            double cost = myAvgSetupCost.getValue() + myAvgHoldingCost.getValue() + myAvgShortageCost.getValue();
            myAvgTotalCost.setValue(cost);
        }
    }

    private class OrderArrival implements EventActionIfc<Double> {

        public void action(JSLEvent<Double> event) {
            Double ordersize = event.getMessage();
            myInvLevel.increment(ordersize.doubleValue());
            myPosInv.setValue(Math.max(0, myInvLevel.getValue()));
            myNegInv.setValue(-Math.min(0, myInvLevel.getValue()));
            myAvgHoldingCost.setValue(myHoldingCost * myPosInv.getValue());
            myAvgShortageCost.setValue(myBackLogCost * myNegInv.getValue());
            double cost = myAvgSetupCost.getValue() + myAvgHoldingCost.getValue() + myAvgShortageCost.getValue();
            myAvgTotalCost.setValue(cost);
        }
    }

    public static void main(String[] args) {
        System.out.println("LKInventory Test");

        Simulation s = new Simulation("LK Inventory Test");
        SimulationReporter r = s.makeSimulationReporter();
        r.turnOnReplicationCSVStatisticReporting();

        // create the containing model
        Model m = s.getModel();

        // create the model element and attach it to the main model
        LKInventoryModel im = new LKInventoryModel(m);
        im.setReorderPoint(20);
        im.setOrderUpToLevel(40);

        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(120.0);
        s.setLengthOfWarmUp(20.0);
        s.run();

        r.printFullAcrossReplicationStatistics();

        System.out.println("Done!");
    }
}
