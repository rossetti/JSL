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
package examples.hospitalward;

import examples.hospitalward.HospitalWard.NoOpPatient;
import examples.hospitalward.HospitalWard.OpPatient;
import jsl.simulation.EventActionIfc;
import jsl.simulation.JSLEvent;
import jsl.simulation.SchedulingElement;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.elements.variable.TimeWeighted;

/**
 *
 * @author rossetti
 */
public class BedWard extends SchedulingElement {

    private Queue<NoOpPatient> myNoOpPatientQ;

    private Queue<OpPatient> myOpPatientQ;

    private TimeWeighted myAvailableBeds;

    private TimeWeighted myBusyBeds;

    private EndNoOpPatientStayListener myNoOpPatientStayListener;

    private EndOpPatientPreOpStayListener myOpPatientPreOpStayListener;

    private EndOfPostOperationStayAction myEndOfPostOperationStayAction;

    private HospitalWard myHospitalWard;

    public BedWard(HospitalWard ward) {
        this(ward, null);
    }

    public BedWard(HospitalWard ward, String name) {
        super(ward, name);

        myHospitalWard = ward;
        myNoOpPatientQ = new Queue<>(this, "No Op Patient Q");
        myOpPatientQ = new Queue<>(this, "Op Patient Q");
        myAvailableBeds = new TimeWeighted(this, 20.0, "Beds Available");
        myBusyBeds = new TimeWeighted(this, 0.0, "Number Busy Beds");
        myNoOpPatientStayListener = new EndNoOpPatientStayListener();

        myOpPatientPreOpStayListener = new EndOpPatientPreOpStayListener();

        myEndOfPostOperationStayAction = new EndOfPostOperationStayAction();

    }

    protected void setInitialNumberOfBeds(int numberOfBeds){
        myAvailableBeds.setInitialValue(numberOfBeds);
    }

    protected void receiveNewPatient(NoOpPatient p) {
        myNoOpPatientQ.enqueue(p);
        if (myAvailableBeds.getValue() > 0.0) {
            if (p == myNoOpPatientQ.peekNext()) {
                myNoOpPatientQ.removeNext();
                myAvailableBeds.decrement();
                myBusyBeds.increment();
                scheduleEvent(myNoOpPatientStayListener, p.getHospitalStayTime(), p);
            }
        }
    }

    protected void receiveNewPatient(OpPatient p) {
        myOpPatientQ.enqueue(p);
        if (myAvailableBeds.getValue() > 0.0) {
            if (p == myOpPatientQ.peekNext()) {
                myOpPatientQ.removeNext();
                myAvailableBeds.decrement();
                myBusyBeds.increment();
                scheduleEvent(myOpPatientPreOpStayListener, p.getPreOperationTime(), p);
            }
        }
    }

    protected void receivePostOperationPatient(OpPatient p) {
        scheduleEvent(myEndOfPostOperationStayAction, p.getPostOperationTime(), p);
    }

    private void reallocateBed() {
        // preference by order of checking
        if (myNoOpPatientQ.isNotEmpty()) {
            NoOpPatient p = myNoOpPatientQ.removeNext();
            scheduleEvent(myNoOpPatientStayListener, p.getHospitalStayTime(), p);
        } else if (myOpPatientQ.isNotEmpty()) {
            OpPatient p = myOpPatientQ.removeNext();
            scheduleEvent(myOpPatientPreOpStayListener, p.getPreOperationTime(), p);
        } else {
            myAvailableBeds.increment();
            myBusyBeds.decrement();
        }
    }

    protected class EndNoOpPatientStayListener implements EventActionIfc<QObject> {

        @Override
        public void action(JSLEvent<QObject> evt) {
            reallocateBed();
            myHospitalWard.departingPatient(evt.getMessage());
        }
    }

    protected class EndOpPatientPreOpStayListener implements EventActionIfc<OpPatient> {

        @Override
        public void action(JSLEvent<OpPatient> evt) {
            OpPatient p = evt.getMessage();
            myHospitalWard.sendToOperatingRoom(p);
        }
    }

    protected class EndOfPostOperationStayAction implements EventActionIfc<QObject> {

        @Override
        public void action(JSLEvent<QObject> evt) {
            reallocateBed();
            myHospitalWard.departingPatient(evt.getMessage());
        }
    }
}
