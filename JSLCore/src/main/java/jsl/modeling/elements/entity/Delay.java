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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.modeling.elements.entity;

import jsl.simulation.EventActionIfc;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.utilities.random.RandomIfc;

/**
 *
 * @author rossetti
 */
public class Delay extends EntityReceiver {

    /** NONE = no duration specified, will result in an exception
     *  DIRECT = uses the activity time specified directly for the activity
     *  BY_TYPE = asks the EntityType to provide the time for this activity
     *  ENTITY = uses the entity's getDurationTime() method
     *
     */
    public enum DelayOption {

        NONE, DIRECT, BY_TYPE, ENTITY
    };

    private RandomVariable myDelayTimeRV;

    protected DelayOption myDelayOption = DelayOption.NONE;

    private final DelayAction myDelayAction;

    public Delay(ModelElement parent) {
        this(parent, null);
    }

    public Delay(ModelElement parent, String name) {
        super(parent, name);
        myDelayAction = new DelayAction();
    }

    public final void setDelayTime(RandomIfc distribution) {
        if (distribution == null) {
            throw new IllegalArgumentException("Attempted to set the activity time distribution to null!");
        }

        if (myDelayTimeRV == null) {
            myDelayTimeRV = new RandomVariable(this, distribution);
        } else {
            myDelayTimeRV.setInitialRandomSource(distribution);
        }

        myDelayOption = DelayOption.DIRECT;

    }

    @Override
    protected void receive(Entity entity) {
//        System.out.println(getTime() + " > " + entity + " started activity");
        scheduleDelayCompletion(entity);
    }

    protected void scheduleDelayCompletion(Entity entity) {

        JSLEvent e = scheduleEvent(myDelayAction, getDelayTime(entity), entity);

        // entity.setTimedEvent(e);
        // entity.setStatus(Entity.Status.TIME_DELAYED);

    }

    protected double getDelayTime(Entity e) {
        double time = 0.0;
        if (myDelayOption == DelayOption.DIRECT) {
            if (myDelayTimeRV == null) {
                throw new NoActivityTimeSpecifiedException();
            } else {
                time = myDelayTimeRV.getValue();
            }
        } else if (myDelayOption == DelayOption.ENTITY) {
            time = e.getDurationTime();
        } else if (myDelayOption == DelayOption.BY_TYPE) {
            EntityType et = e.getType();
            time = et.getActivityTime(this);
        } else if (myDelayOption == DelayOption.NONE) {
            throw new NoActivityTimeSpecifiedException();
        }
        return time;
    }

    protected void endOfDelay(Entity entity) {
 //        System.out.println(getTime() + " > " + entity + " ended activity");
        sendEntity(entity);
    }

    public final void setDelayOption(DelayOption option) {
        myDelayOption = option;
    }

    private class DelayAction implements EventActionIfc<Entity> {

        @Override
        public void action(JSLEvent<Entity> e) {
            Entity entity = e.getMessage();
            endOfDelay(entity);
        }
    }
}
