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
package jsl.modeling.elements.variable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import jsl.simulation.Executive;

import jsl.simulation.ModelElement;
import jsl.utilities.GetValueIfc;
import jsl.utilities.PreviousValueIfc;

/** This class represents something that can be aggregated.
 * 
 *
 */
public abstract class Aggregatable extends ModelElement implements GetValueIfc,
        PreviousValueIfc {

    /** The aggregatable's list of aggregates. An aggregate
     *  can be formed from aggregatables and react to
     *  changes in the aggregatables.  Lazy initialization
     *  is used for this list.  No list is created until
     *  the aggregatable is added to an aggregate.
     *
     */
    protected List<Aggregate> myAggregates;

    /**
     * @param parent
     */
    public Aggregatable(ModelElement parent) {
        this(parent, null);
    }

    /**
     * @param parent
     * @param name
     */
    public Aggregatable(ModelElement parent, String name) {
        super(parent, name);
        //	System.out.println("Aggregatable: " + getName() + " constructed at time " + getTime());
    }

    /** This method is called by Aggregate to register itself
     *  as an aggregate for this variable, when it is told
     *  to observe the variable
     * 
     * @param aggregate the aggregate to attach
     */
    protected void attachAggregate(Aggregate aggregate) {
        if (myAggregates == null) {
            myAggregates = new LinkedList<Aggregate>();
        }
        myAggregates.add(aggregate);
    }

    /** This method is called by Aggregate to unregister itself
     *  as an aggregate for this variable, when it is told
     *  to stop observing the variable
     * 
     * @param aggregate the aggregate to detach
     */
    protected void detachAggregate(Aggregate aggregate) {
        if (myAggregates == null) {
            return;
        }
        myAggregates.remove(aggregate);
    }

    /** This method should be overridden by subclasses that
     * need actions performed when a model element is removed from a model after
     * the replication has started.
     */
    @Override
    protected void removedFromModel() {
        // this aggregatable has been removed from the model
        // notify any attached aggregates that the aggregatable
        // is no longer available for observation
//		System.out.println(getName() + " In removedDuringReplication()");
//		System.out.println("Notifying aggregates of model removal");
        notifyAggregatesOfModelRemoval();
    }

    /** Notifies any aggregates that initialization has occurred.
     * 
     */
    protected void notifyAggregatesOfInitialization() {
        if (myAggregates != null) {
            for (Aggregate a : myAggregates) {
                a.initialized(this);
            }
        }
    }

    /** Notifies any aggregates that warm up has occurred.
     * 
     */
    protected void notifyAggregatesOfWarmUp() {
        if (myAggregates != null) {
            for (Aggregate a : myAggregates) {
                a.warmedUp(this);
            }
        }
    }

    /** Notifies any aggregates that its value has changed
     * 
     */
    protected void notifyAggregatesOfValueChange() {
        //TODO this is problematic because we are depending on
        // the state of the executive
        if (myAggregates != null) {
            for (Aggregate a : myAggregates) {
                if (getModel().isRunning()) {//TODO this is the problem!
                    a.valueChangedDuringReplication(this);
                } else {
                    // executive has been stopped, but we don't know
                    // if we just ended.
                    Executive e = getModel().getExecutive();
                    if (!e.isDone()){
                        // if executive is not running
                        // and it is done, then must be at end of iterations
                        // if not done, then must be before replication
                         a.valueChangedBeforeReplication(this);                       
                    }

                }
                //TODO
/*
                Replication r = getCurrentReplication();
                if (r != null) {
                    if (r.isRunning()) {
                        a.valueChangedDuringReplication(this);
                    } else {
                        a.valueChangedBeforeReplication(this);
                    }
                } else {
                    a.valueChangedBeforeReplication(this);
                }
 */
            }
        }
    }

    /** Notifies any aggregates that the element has been removed
     *  from the model
     * 
     */
    protected void notifyAggregatesOfModelRemoval() {
        if (myAggregates != null) {
            List<Aggregate> list = new ArrayList<Aggregate>(myAggregates);
            for (Aggregate a : list) {
                a.removedFromModel(this);
            }
            list.clear();
            list = null;
            /*
            Aggregate[] a =myAggregates.toArray(new Aggregate[myAggregates.size()]);
            for(int i=0;i<a.length;i++){
            a[i].removedFromModel(this);
            a[i] = null;
            }
            a = null;
             */
            //   		System.out.println("Aggregatable: " + getName() + " notifyAggregatesOfModelRemoval().");

        }
    }

    @Override
    protected void initialize_() {
        super.initialize_();
    }

    @Override
    protected void warmUp_() {
        super.warmUp_();
    }
}
