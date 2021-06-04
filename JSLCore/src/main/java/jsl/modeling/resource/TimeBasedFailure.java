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
package jsl.modeling.resource;

import jsl.simulation.JSLEvent;
import jsl.modeling.elements.EventGenerator;
import jsl.simulation.Simulation;
import jsl.utilities.random.RandomIfc;
import jsl.modeling.elements.EventGeneratorActionIfc;

/**
 * A TimeBasedFailure uses time to determine the next failure. By default the
 * failure process does not start automatically at time zero. The user can turn
 * on automatic starting of the failure process at time zero or use the
 * start() method. Once the failure process has been started it
 * cannot be started again. Once the failure process has been stopped, it cannot
 * be started again.
 *
 * @author rossetti
 */
public class TimeBasedFailure extends FailureProcess {

    protected EventGenerator myFailureGenerator;

    public TimeBasedFailure(ResourceUnit resourceUnit, RandomIfc failureDuration,
                            RandomIfc timeToFirstFailure, RandomIfc timeBtwFailures) {
        this(resourceUnit, failureDuration, timeToFirstFailure, timeBtwFailures, null);
    }

    public TimeBasedFailure(ResourceUnit resourceUnit, RandomIfc failureDuration,
            RandomIfc timeToFirstFailure, RandomIfc timeBtwFailures, String name) {
        super(resourceUnit, failureDuration, timeToFirstFailure, name);
        myFailureGenerator = new EventGenerator(this, new FailureAction(),
                timeToFirstFailure, timeBtwFailures, getName() + ":FailureGenerator");
        // use the FailureProcess to start the generator
        myFailureGenerator.setStartOnInitializeFlag(false);
    }

    /**
     * Sets the time between failures source of randomness
     *
     * @param tbf the time between failure
     */
    public final void setTimeBetweenFailureEvents(RandomIfc tbf) {
        myFailureGenerator.setInitialTimeBetweenEvents(tbf);
    }

    @Override
    protected void failureNoticeActivated(FailureNotice fn) {
//        System.out.printf("%f > The time based failure %d was activated. %n", getTime(), fn.getId());
        // can't fail again until after the resource is repaired (after the failure duration occurs)
        suspend();
    }

    @Override
    protected void failureNoticeDelayed(FailureNotice fn) {
//        System.out.printf("%f > The time based failure %d was delayed. %n", getTime(), fn.getId());
        suspend();
    }

    @Override
    protected void failureNoticeIgnored(FailureNotice fn) {
        StringBuilder sb = new StringBuilder();
        sb.append(getTime());
        sb.append(" > ");
        sb.append(getName());
        sb.append(" ignored FailureNotice ");
        sb.append(fn.toString());
        sb.append(System.lineSeparator());
        Simulation.LOGGER.warn(sb.toString());
    }

    @Override
    protected void failureNoticeCompleted(FailureNotice fn) {
//        System.out.printf("%f > The time based failure %d was completed. %n", getTime(), fn.getId());
        resume();
    }

    @Override
    protected void suspendProcess() {
        myFailureGenerator.suspend();
    }

    @Override
    protected void stopProcess() {
        myFailureGenerator.turnOffGenerator();
    }

    @Override
    protected void resumeProcess() {
        myFailureGenerator.resume();
    }

    private class FailureAction implements EventGeneratorActionIfc {

        @Override
        public void generate(EventGenerator generator, JSLEvent event) {
            fail();
        }

    }
}
