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
package examples.general.queueing;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.simulation.*;
import jsl.utilities.random.RandomIfc;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 *  A model for the machine repair problem.  N machines are available to fail.
 *  R operators are available to repair. When a machine fails, if an operator
 *  is not busy, then the operator works on the machine for the repair time.
 *  Once the machine is repaired, the time to failure may occur again.
 *  This example illustrates the use of Java 8 method references and the
 *  JSL's scheduling DSL.
 *
 * @author rossetti
 */
public class MachineRepairJ8 extends SchedulingElement {

    private final Queue<QObject> myRepairQ;

    private final RandomVariable myTBFailure;

    private final RandomVariable myRepairTime;

    private final TimeWeighted myNumFailedMachines;

    private final TimeWeighted myNumAvailableOperators;

    private final TimeWeighted myNumBusyOperators;

    private final TimeWeighted myProbAllBroken;

    private final int myNumMachines;

    private final int myNumOperators;

    private final double breakDownCost = 60.0;

    private final double laborCost = 15.0;

    private final ResponseVariable hourlyCost;
    private final ResponseVariable mySystemTime;

    /**
     *
     * @param parent the parent
     * @param numOperators the number of operators to repair
     * @param numMachines the number of machines that can fail
     * @param tbFailure the time between failures
     * @param repTime the time to repair
     */
    public MachineRepairJ8(ModelElement parent, int numOperators, int numMachines,
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
        mySystemTime = new ResponseVariable(this, "System Time");

    }

    @Override
    protected void initialize() {
        super.initialize();
        for (int i = 1; i <= myNumMachines; i++) {
            QObject machine = createQObject();
            schedule(this::failure).withMessage(machine).in(myTBFailure).units();
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
        QObject failedMachine = (QObject) event.getMessage();
        failedMachine.setTimeStamp(getTime()); // remember time of failure
        myRepairQ.enqueue(failedMachine);
        if (myNumAvailableOperators.getValue() > 0) {
            myNumAvailableOperators.decrement();
            myNumBusyOperators.increment();
            QObject nextMachine = myRepairQ.removeNext();
            schedule(this::repair).withMessage(nextMachine).in(myRepairTime).units();
        }
    }

    private void repair(JSLEvent event) {
        myNumFailedMachines.decrement();
        myProbAllBroken.setValue(myNumFailedMachines.getValue() == myNumMachines);
        QObject repairedMachine = (QObject) event.getMessage();
        mySystemTime.setValue(getTime() - repairedMachine.getTimeStamp());
        schedule(this::failure).withMessage(repairedMachine).in(myTBFailure).units();
        if (myRepairQ.isNotEmpty()) {
            QObject nextMachine = myRepairQ.removeNext();
            schedule(this::repair).withMessage(nextMachine).in(myRepairTime).units();
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
        int numOperators = 1;
        RandomIfc tbf = new ExponentialRV(10.0);
        RandomIfc rt = new ExponentialRV(4.0);
        MachineRepairJ8 machineRepair = new MachineRepairJ8(m, numOperators, numMachines, tbf, rt);

        s.run();

        s.printHalfWidthSummaryReport();
    }
}
