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

import examples.hospitalward.HospitalWard.OpPatient;
import jsl.simulation.EventAction;
import jsl.simulation.EventActionIfc;
import jsl.simulation.JSLEvent;
import jsl.simulation.SchedulingElement;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.Queue;
import jsl.utilities.random.rvariable.ConstantRV;

/**
 *
 * @author rossetti
 */
public class OperatingRoom extends SchedulingElement {

    public static final double IDLE = 0.0;

    public static final double BUSY = 1.0;

    public static final double OPEN = 1.0;

    public static final double CLOSED = 0.0;

    private HospitalWard myHospitalWard;

    private Queue<OpPatient> myORQ;

    private RandomVariable myOpRoomOpenTime;

    private RandomVariable myOpRoomCloseTime;

    private TimeWeighted myORRoomOpenStatus;

    private TimeWeighted myORRoomIdleStatus;

    private OpenOperatingRoomAction myOpenOperatingRoomAction;

    private CloseOperatingRoomAction myCloseOperatingRoomAction;

    private EndOfOperationAction myEndOfOperationAction;

    public OperatingRoom(HospitalWard ward) {
        this(ward, null);
    }

    public OperatingRoom(HospitalWard ward, String name) {
        super(ward, name);
        myHospitalWard = ward;
        myORQ = new Queue<>(this, "OR Q");
        myOpRoomOpenTime = new RandomVariable(this, new ConstantRV(24.0));
        myOpRoomCloseTime = new RandomVariable(this, new ConstantRV(4.0));
        myORRoomOpenStatus = new TimeWeighted(this, OPEN, "OR-Open-Status");
        myORRoomIdleStatus = new TimeWeighted(this, IDLE, "OR-Idle-Status");
        myOpenOperatingRoomAction = new OpenOperatingRoomAction();
        myCloseOperatingRoomAction = new CloseOperatingRoomAction();
        myEndOfOperationAction = new EndOfOperationAction();
    }

    @Override
    protected void initialize() {
        scheduleEvent(myCloseOperatingRoomAction, myOpRoomOpenTime);
    }

    protected void receivePatient(OpPatient p) {
        myORQ.enqueue(p);
        if (isIdle() && isOpen()) {
            if (p == myORQ.peekNext()) {
                myORRoomIdleStatus.setValue(BUSY);
                myORQ.removeNext();
                scheduleEvent(myEndOfOperationAction, p.getOperationTime(), p);
            }
        }
    }

    public boolean isIdle() {
        return myORRoomIdleStatus.getValue() == IDLE;
    }

    public boolean isOpen() {
        return myORRoomOpenStatus.getValue() == OPEN;
    }

    protected class OpenOperatingRoomAction extends EventAction {

        @Override
        public void action(JSLEvent evt) {

            myORRoomOpenStatus.setValue(OPEN);
            if (isIdle() && myORQ.isNotEmpty()) {
                myORRoomIdleStatus.setValue(BUSY);
                OpPatient p = myORQ.removeNext();
                scheduleEvent(myEndOfOperationAction, p.getOperationTime(), p);
            }
            scheduleEvent(myCloseOperatingRoomAction, myOpRoomOpenTime);
        }
    }

    protected class CloseOperatingRoomAction extends EventAction {

        @Override
        public void action(JSLEvent evt) {
            myORRoomOpenStatus.setValue(CLOSED);
            scheduleEvent(myOpenOperatingRoomAction, myOpRoomCloseTime);
        }
    }

    protected class EndOfOperationAction implements EventActionIfc<OpPatient> {

        @Override
        public void action(JSLEvent<OpPatient> evt) {
            if (myORQ.isNotEmpty() && isOpen()) {
                OpPatient nextP = myORQ.removeNext();
                scheduleEvent(myEndOfOperationAction, nextP.getOperationTime(), nextP);
            } else {
                myORRoomIdleStatus.setValue(IDLE);
            }
            OpPatient currentP = evt.getMessage();
            myHospitalWard.endOfOperation(currentP);
        }
    }
}
