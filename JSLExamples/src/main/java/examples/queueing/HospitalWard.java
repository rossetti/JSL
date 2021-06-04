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

import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.elements.variable.Variable;
import jsl.observers.ObserverIfc;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.LognormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;

/**
 * @author rossetti
 *
 */
public class HospitalWard extends SchedulingElement {

    public static final double IDLE = 0.0;

    public static final double BUSY = 1.0;

    public static final double OPEN = 1.0;

    public static final double CLOSED = 0.0;

    protected RandomVariable myNonOpPatientStayTime;

    protected RandomVariable myPreOpStayTime;

    protected RandomVariable myOperationTime;

    protected RandomVariable myPostOpStayTime;

    protected RandomVariable myOpRoomOpenTime;

    protected RandomVariable myOpRoomCloseTime;

    protected RandomVariable myNonOpPatientTBA;

    protected RandomVariable myOpPatientTBA;

    protected TimeWeighted myNonOpPatientQ;

    protected TimeWeighted myOpPatientQ;

    protected TimeWeighted myOpRoomQ;

    protected TimeWeighted myAvailableBeds;

    protected TimeWeighted myNumBusyBeds;
    protected TimeWeighted myNBB;

    protected TimeWeighted myORRoomOpenStatus;

    protected TimeWeighted myORRoomIdleStatus;

    protected NonOperationPatientArrivalAction myNonOperationPatientArrivalAction;

    protected NonOperationPatientDepartureAction myNonOperationPatientEndOfStayAction;

    protected OperationPatientArrivalAction myOperationPatientArrivalAction;

    protected EndOfPreOperationStayAction myEndOfPreOperationStayAction;

    protected EndOfOperationAction myEndOfOperationAction;

    protected EndOfPostOperationStayAction myEndOfPostOperationStayAction;

    protected OpenOperatingRoomAction myOpenOperatingRoomAction;

    protected CloseOperatingRoomAction myCloseOperatingRoomAction;

    /**
     * @param parent
     */
    public HospitalWard(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent
     * @param name
     */
    public HospitalWard(ModelElement parent, String name) {
        super(parent, name);

        myNonOpPatientTBA = new RandomVariable(this, new ExponentialRV(12.0));
        myOpPatientTBA = new RandomVariable(this, new ExponentialRV(6.0));

        myNonOpPatientStayTime = new RandomVariable(this, new ExponentialRV(60.0));
        myPreOpStayTime = new RandomVariable(this, new ExponentialRV(24.0));
        myOperationTime = new RandomVariable(this, new LognormalRV(0.75, 0.25 * 0.25));
        myPostOpStayTime = new RandomVariable(this, new ExponentialRV(72.0));

        myOpRoomOpenTime = new RandomVariable(this, new ConstantRV(24.0));
        myOpRoomCloseTime = new RandomVariable(this, new ConstantRV(4.0));


        myNonOpPatientQ = new TimeWeighted(this, 0.0, "NonOpPatientQ");
        myOpPatientQ = new TimeWeighted(this, 0.0, "OpPatientQ");
        myOpRoomQ = new TimeWeighted(this, 0.0, "OpRoomQ");
        myAvailableBeds = new TimeWeighted(this, 20.0, "Beds Available");
        myNumBusyBeds = new TimeWeighted(this, 0.0, "Beds Busy");
        myAvailableBeds.addObserver(new BedObserver());
        myNBB = new TimeWeighted(this, 0.0, "Beds Busy Direct");
        myORRoomOpenStatus = new TimeWeighted(this, OPEN, "OR-Open-Status");
        myORRoomIdleStatus = new TimeWeighted(this, IDLE, "OR-Idle-Status");

        myNonOperationPatientArrivalAction = new NonOperationPatientArrivalAction();
        myNonOperationPatientEndOfStayAction = new NonOperationPatientDepartureAction();
        myOperationPatientArrivalAction = new OperationPatientArrivalAction();
        myEndOfPreOperationStayAction = new EndOfPreOperationStayAction();
        myEndOfOperationAction = new EndOfOperationAction();
        myEndOfPostOperationStayAction = new EndOfPostOperationStayAction();
        myOpenOperatingRoomAction = new OpenOperatingRoomAction();
        myCloseOperatingRoomAction = new CloseOperatingRoomAction();

    }

    public void setInitialNumberOfBeds(double value) {
        myAvailableBeds.setInitialValue(value);
    }

    public void setORInitialStatusToOpen() {
        myORRoomOpenStatus.setInitialValue(OPEN);
    }

    public void setORInitialStatusToClosed() {
        myORRoomOpenStatus.setInitialValue(CLOSED);
    }

    public void setNonOpPatientStayTimeInitialRandomSource(RVariableIfc source) {
        myNonOpPatientStayTime.setInitialRandomSource(source);
    }

    public void setPreOperationTimeInitialRandomSource(RVariableIfc source) {
        myPreOpStayTime.setInitialRandomSource(source);
    }

    public void setPostOperationTimeInitialRandomSource(RVariableIfc source) {
        myPostOpStayTime.setInitialRandomSource(source);
    }

    public void setOperationTimeInitialRandomSource(RVariableIfc source) {
        myOperationTime.setInitialRandomSource(source);
    }

    public void setOperatingRoomOpenTimeInitialRandomSource(RVariableIfc source) {
        myOpRoomOpenTime.setInitialRandomSource(source);
    }

    public void setOperatingRoomCloseTimeInitialRandomSource(RVariableIfc source) {
        myOpRoomCloseTime.setInitialRandomSource(source);
    }

    public void setNonOpPatientTBAInitialRandomSource(RVariableIfc source) {
        myNonOpPatientTBA.setInitialRandomSource(source);
    }

    public void setOpPatientTBAInitialRandomSource(RVariableIfc source) {
        myOpPatientTBA.setInitialRandomSource(source);
    }

    protected void initialize() {
        scheduleEvent(myNonOperationPatientArrivalAction, myNonOpPatientTBA.getValue());
        scheduleEvent(myOperationPatientArrivalAction, myOpPatientTBA.getValue());
        scheduleEvent(myCloseOperatingRoomAction, myOpRoomOpenTime.getValue());
    }

    protected class NonOperationPatientArrivalAction extends EventAction {

        public void action(JSLEvent evt) {
            if (myAvailableBeds.getValue() > 0.0) {
                myAvailableBeds.decrement();
                myNBB.increment();
                scheduleEvent(myNonOperationPatientEndOfStayAction, myNonOpPatientStayTime.getValue());
            } else {
                myNonOpPatientQ.increment();
            }
            scheduleEvent(myNonOperationPatientArrivalAction, myNonOpPatientTBA.getValue());
        }
    }

    protected class NonOperationPatientDepartureAction extends EventAction {

        public void action(JSLEvent evt) {
            if (myNonOpPatientQ.getValue() > 0.0) {
                myNonOpPatientQ.decrement();
                scheduleEvent(myNonOperationPatientEndOfStayAction, myNonOpPatientStayTime.getValue());
            } else if (myOpPatientQ.getValue() > 0.0) {
                myOpPatientQ.decrement();
                scheduleEvent(myEndOfPreOperationStayAction, myPreOpStayTime.getValue());
            } else {
                myAvailableBeds.increment();
                myNBB.decrement();
            }
        }
    }

    protected class OperationPatientArrivalAction extends EventAction {

        public void action(JSLEvent evt) {
            if (myAvailableBeds.getValue() > 0.0) {
                myAvailableBeds.decrement();
                myNBB.increment();
                scheduleEvent(myEndOfPreOperationStayAction, myPreOpStayTime.getValue());
            } else {
                myOpPatientQ.increment();
            }
            scheduleEvent(myOperationPatientArrivalAction, myOpPatientTBA.getValue());
        }
    }

    protected class EndOfPreOperationStayAction extends EventAction {

        public void action(JSLEvent evt) {
            if ((myORRoomIdleStatus.getValue() == IDLE) && (myORRoomOpenStatus.getValue() == OPEN)) {
                myORRoomIdleStatus.setValue(BUSY);
                scheduleEvent(myEndOfOperationAction, myOperationTime.getValue());
            } else {
                myOpRoomQ.increment();
            }
        }
    }

    protected class EndOfOperationAction extends EventAction {

        public void action(JSLEvent evt) {
            if ((myOpRoomQ.getValue() > 0.0) && (myORRoomOpenStatus.getValue() == OPEN)) {
                myOpRoomQ.decrement();
                scheduleEvent(myEndOfOperationAction, myOperationTime.getValue());
            } else {
                myORRoomIdleStatus.setValue(IDLE);
            }
            scheduleEvent(myEndOfPostOperationStayAction, myPostOpStayTime.getValue());
        }
    }

    protected class EndOfPostOperationStayAction extends EventAction {

        public void action(JSLEvent evt) {
            if (myNonOpPatientQ.getValue() > 0.0) {
                myNonOpPatientQ.decrement();
                scheduleEvent(myNonOperationPatientEndOfStayAction, myNonOpPatientStayTime.getValue());
            } else if (myOpPatientQ.getValue() > 0.0) {
                myOpPatientQ.decrement();
                scheduleEvent(myEndOfPreOperationStayAction, myPreOpStayTime.getValue());
            } else {
                myAvailableBeds.increment();
                myNBB.decrement();
            }
        }
    }

    protected class OpenOperatingRoomAction extends EventAction {

        public void action(JSLEvent evt) {
            myORRoomOpenStatus.setValue(OPEN);
            if ((myORRoomIdleStatus.getValue() == IDLE) && (myOpRoomQ.getValue() > 0.0)) {
                myOpRoomQ.decrement();
                myORRoomIdleStatus.setValue(BUSY);
                scheduleEvent(myEndOfOperationAction, myOperationTime.getValue());
            }
            scheduleEvent(myCloseOperatingRoomAction, myOpRoomOpenTime.getValue());
        }
    }

    protected class CloseOperatingRoomAction extends EventAction {

        public void action(JSLEvent evt) {
            myORRoomOpenStatus.setValue(CLOSED);
            scheduleEvent(myOpenOperatingRoomAction, myOpRoomCloseTime.getValue());
        }
    }

    protected class BedObserver implements ObserverIfc {

        public void update(Object arg0, Object arg1) {
            Variable v = (Variable) arg0;

            if (v.checkForUpdate()) {
                double na = myAvailableBeds.getValue(); // current value
                double beds = myAvailableBeds.getInitialValue();
                myNumBusyBeds.setValue(beds - na);
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        
        Simulation sim = new Simulation("HospitalWard");
                
        // create the model element and attach it to the main model
        new HospitalWard(sim.getModel());

        // set the parameters of the experiment
        sim.setNumberOfReplications(30);
        sim.setLengthOfWarmUp(5000.0);
        sim.setLengthOfReplication(15000.0);

        // tell the experiment to run
        sim.run();

        SimulationReporter r = sim.makeSimulationReporter();
        
        r.printAcrossReplicationSummaryStatistics();

    }
}
