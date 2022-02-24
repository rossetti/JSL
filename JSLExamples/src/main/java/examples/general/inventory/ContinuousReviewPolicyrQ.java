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

import jsl.modeling.elements.variable.Counter;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.reporting.JSL;

public class ContinuousReviewPolicyrQ extends SchedulingElement {

    private static final double NOONHAND = 1.0;

    private static final double IMFILLED = 1.0;

    private static final double REORDERPOINT = 3.0;

    private static final double ORDERQ = 4.0;

    private ResponseVariable myTotalCost;

    private TimeWeighted myOnHandInv;

    private TimeWeighted myOnOrderInv;

    private TimeWeighted myBackOrder;

    private TimeWeighted myNetInv;

    private TimeWeighted myInvPosition;

    private TimeWeighted myStockOutIndicator;

    private TimeWeighted myFillRate;

    private TimeWeighted myImmediatelyFilled;

    private Counter myNumReplenishment;

    private RandomVariable myDemandTBA;

    private RandomVariable myLeadTimeRV;

    protected DemandArrivalAction myDemandArrivalAction;

    protected ReplenishmentArrivalAction myReplenishmentArrivalAction;

    public ContinuousReviewPolicyrQ(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent
     * @param name
     */
    public ContinuousReviewPolicyrQ(ModelElement parent, String name) {
        super(parent, name);
        myDemandTBA = new RandomVariable(this, new ExponentialRV(365.0 / 14.0));
        myLeadTimeRV = new RandomVariable(this, new ConstantRV(45.0));
        myOnHandInv = new TimeWeighted(this, REORDERPOINT + ORDERQ, "Amount of Inventory On Hand");
        myOnOrderInv = new TimeWeighted(this, 0.0, "Amount of Inventory On Order");
        myBackOrder = new TimeWeighted(this, 0.0, "Amount of Demand BackOrdered");
        myNetInv = new TimeWeighted(this, 0.0, "Amount of Net Inventory");
        myInvPosition = new TimeWeighted(this, 0.0, "Inventory Position");
        myStockOutIndicator = new TimeWeighted(this, 0.0, "Stock Out Indicator");
        myFillRate = new TimeWeighted(this, 0.0, "Fill rate (1-A)");
        myImmediatelyFilled = new TimeWeighted(this, 0.0, "Immediately Filled");
        myTotalCost = new ResponseVariable(this, 0.0, "Total Cost");
        myNumReplenishment = new Counter(this, "Number of Replenishment Orders");
        myDemandArrivalAction = new DemandArrivalAction();
        myReplenishmentArrivalAction = new ReplenishmentArrivalAction();
    }

    protected double getInventoryPosition() {
        myNetInv.setValue(myOnHandInv.getValue() - myBackOrder.getValue());
        myInvPosition.setValue(myNetInv.getValue() + myOnOrderInv.getValue());
        return (myInvPosition.getValue());
    }

    protected void initialize() {
        super.initialize();
        scheduleEvent(myDemandArrivalAction, myDemandTBA.getValue());
    }

    protected void replicationEnded() {
        double ibar = myOnHandInv.getWithinReplicationStatistic().getAverage();

        double bbar = myBackOrder.getWithinReplicationStatistic().getAverage();
        double of = myNumReplenishment.getValue(); // this is a Counter
        double TC = 30.0 * ibar + 100.0 * bbar + 15.0 * (of/10.0);
        myTotalCost.setValue(TC); // this is a ResponseVariable
        JSL.getInstance().out.println("----------------------------------------------------------------------");
        JSL.getInstance().out.println("Replication:" + getSimulation().getCurrentReplicationNumber());
        JSL.getInstance().out.println("ibar =" + ibar);
        JSL.getInstance().out.println("bbar =" + bbar);
        JSL.getInstance().out.println("of =" + of);
        JSL.getInstance().out.println("TC = " + TC);
        JSL.getInstance().out.println("----------------------------------------------------------------------");
    }

    protected class DemandArrivalAction extends EventAction {

        public void action(JSLEvent evt) {
            //demand is always single quantity D = 1
            if (myOnHandInv.getValue() >= 1) {
                myOnHandInv.decrement();
                //demand is immediately filled
                myImmediatelyFilled.setValue(IMFILLED);
                myStockOutIndicator.setValue(0.0);
                myFillRate.setValue(1.0 - myStockOutIndicator.getValue());
            } else {
                myBackOrder.increment();
                //demand is not immediately filled
                myImmediatelyFilled.setValue(0.0);
                myStockOutIndicator.setValue(NOONHAND);
                myFillRate.setValue(1.0 - myStockOutIndicator.getValue());
            }
            getInventoryPosition();
            if (myInvPosition.getValue() <= REORDERPOINT) {
                myOnOrderInv.increment(ORDERQ);
                scheduleEvent(myReplenishmentArrivalAction, myLeadTimeRV.getValue());
                getInventoryPosition();
            }
            scheduleEvent(myDemandArrivalAction, myDemandTBA.getValue());
        }
    }

    protected class ReplenishmentArrivalAction extends EventAction {

        public void action(JSLEvent evt) {
            myNumReplenishment.increment();
            myOnOrderInv.decrement(ORDERQ);
            getInventoryPosition();
            if (myBackOrder.getValue() == 0.0) {
                myOnHandInv.increment(ORDERQ);
            } else {
                if (myBackOrder.getValue() <= ORDERQ) {
                    myOnHandInv.increment(ORDERQ - myBackOrder.getValue());
                    myBackOrder.setValue(0.0);
                } else {
                    myBackOrder.decrement(ORDERQ);
                    myOnHandInv.setValue(0.0);
                }
            }
            getInventoryPosition();
            if (myInvPosition.getValue() <= REORDERPOINT) {
                myOnOrderInv.increment(ORDERQ);
                scheduleEvent(myReplenishmentArrivalAction, myLeadTimeRV.getValue());
                getInventoryPosition();
            }
        }
    }

    public static void main(String[] args) {

        Simulation s = new Simulation(" Continuous Review (r, Q) Simulation");

        // create the containing model
        Model m = s.getModel();
        // create the model element and attach it to the main model
        new ContinuousReviewPolicyrQ(m, "Continuous Review (r,Q) Policy");
        // set the parameters of the experiment
        s.setNumberOfReplications(30);
        s.setLengthOfWarmUp(5 * 365.0);
        s.setLengthOfReplication(10 * 365);
        s.run();
        
        SimulationReporter r = s.makeSimulationReporter();
        r.printAcrossReplicationSummaryStatistics();
        //r.printAcrossReplicationStatistics();
        System.out.println("Done!");
    }
}
