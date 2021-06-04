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
package examples.queueing;

import jsl.simulation.JSLEvent;
import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.simulation.SchedulingElement;
import jsl.simulation.Simulation;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.utilities.random.RandomIfc;
import jsl.simulation.SimulationReporter;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 *
 * @author rossetti
 */
public class MachineRepair extends SchedulingElement {

    protected Queue<QObject> myRepairQ;

    protected RandomVariable myTBFailure;

    protected RandomVariable myRepairTime;

    protected TimeWeighted myNumFailedMachines;

    protected TimeWeighted myNumAvailableOperators;

    protected TimeWeighted myNumBusyOperators;

    protected TimeWeighted myProbAllBroken;

    protected int myNumMachines;

    protected int myNumOperators;

    protected double breakDownCost = 60.0;

    protected double laborCost = 15.0;

    protected ResponseVariable hourlyCost;

    /**
     * 
     * @param parent
     * @param numOperators
     * @param numMachines
     * @param tbFailure
     * @param repTime 
     */
    public MachineRepair(ModelElement parent, int numOperators, int numMachines,
            RandomIfc tbFailure, RandomIfc repTime) {
        super(parent);
        if (numMachines <= numOperators) {
            throw new IllegalArgumentException("The number of machines must be > number operators");
        }
        myRepairQ = new Queue<>(this, "RepairQ");
        myNumMachines = numMachines;
        myNumOperators = numOperators;
        myNumFailedMachines = new TimeWeighted(this, 0.0, "Num Failed Machines");
        myProbAllBroken = new TimeWeighted(this, 0.0, "Prob all broken");
        myNumAvailableOperators = new TimeWeighted(this, myNumOperators, "Num Available Operators");
        myNumBusyOperators = new TimeWeighted(this, 0.0, "Num Busy Operators");
        myTBFailure = new RandomVariable(this, tbFailure);
        myRepairTime = new RandomVariable(this, repTime);
        hourlyCost = new ResponseVariable(this, "Hourly Cost");

    }

    @Override
    protected void initialize() {
        super.initialize();
        for (int i = 1; i <= myNumMachines; i++) {
            schedule(this::failure).in(myTBFailure).units();
        }
    }

    @Override
    protected void replicationEnded() {
        double avgBroken = myNumFailedMachines.getWithinReplicationStatistic().getAverage();
        double cost = avgBroken*breakDownCost + laborCost*myNumOperators;
        hourlyCost.setValue(cost);
    }

    private void failure(JSLEvent event) {
        myNumFailedMachines.increment();
        myProbAllBroken.setValue(myNumFailedMachines.getValue() == myNumMachines);
        QObject arrival = createQObject();
        myRepairQ.enqueue(arrival);
        if (myNumAvailableOperators.getValue() > 0) {
            myNumAvailableOperators.decrement();
            myNumBusyOperators.increment();
            QObject nc = myRepairQ.removeNext();
            schedule(this::repair).withMessage(nc).in(myRepairTime).units();
        }
    }

    private void repair(JSLEvent event) {
        myNumFailedMachines.decrement();
        myProbAllBroken.setValue(myNumFailedMachines.getValue() == myNumMachines);
        schedule(this::failure).in(myTBFailure).units();
        if (myRepairQ.isNotEmpty()) {
            QObject nc = myRepairQ.removeNext();
            schedule(this::repair).withMessage(nc).in(myRepairTime).units();
        } else {
            myNumAvailableOperators.increment();
            myNumBusyOperators.decrement();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation s = new Simulation("Machine Repair");
        s.setLengthOfReplication(11000.0);
        s.setLengthOfWarmUp(1000.0);
        s.setNumberOfReplications(30);

        Model m = s.getModel();

        int numMachines = 5;
        int numOperators = 4;
        RandomIfc tbf = new ExponentialRV(10.0);
        RandomIfc rt = new ExponentialRV(4.0);
        MachineRepair machineRepair = new MachineRepair(m, numOperators, numMachines, tbf, rt);
        SimulationReporter r = s.makeSimulationReporter();
        s.run();

        s.printHalfWidthSummaryReport();
    }
}
