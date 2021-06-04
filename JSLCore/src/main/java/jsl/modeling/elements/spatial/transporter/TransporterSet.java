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
package jsl.modeling.elements.spatial.transporter;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import jsl.simulation.ModelElement;
import jsl.modeling.queue.QObject;
import jsl.modeling.elements.variable.TimeWeighted;

public class TransporterSet extends ModelElement {

    protected List<Transporter> myTransporters;

    protected List<Transporter> myIdleTransporters;

    protected List<TransporterProvider> myTransporterProviders;

    protected TransporterSelectionRuleIfc myTransporterSelectionRule;

    protected TimeWeighted myNumIdleTransporters;

    /** Creates a transporter set with the given model element as a parent
     *  and assigned a default name.
     *
     * @param parent
     */
    public TransporterSet(ModelElement parent) {
        this(parent, null);
    }

    /** Creates a transporter set with the given model element as a parent and the
     *  given name.
     *
     * @param parent
     * @param name
     */
    public TransporterSet(ModelElement parent, String name) {
        super(parent, name);
        myTransporters = new LinkedList<Transporter>();
        myIdleTransporters = new LinkedList<Transporter>();
        myTransporterProviders = new LinkedList<TransporterProvider>();
        myNumIdleTransporters = new TimeWeighted(this, "Num Idle Transporters");
    }

    /** Creates and adds a transporter to the set
     *
     * @return
     */
    public final Transporter addTransporter() {
        Transporter transporter = new Transporter(this);
        addTransporter(transporter);
        return (transporter);
    }

    /** Creates the specified number of transporters and add them
     *  to the transporter set
     *
     * @param n The number of desired transporters (n&gt;0)
     */
    public final void addTransporter(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("The number of transporters must be > 0");
        }

        for (int i = 1; i <= n; i++) {
            addTransporter();
        }

    }

    /** Adds a transporter to the set
     *
     * @param transporter
     */
    public final void addTransporter(Transporter transporter) {
        if (transporter == null) {
            throw new IllegalArgumentException("The supplied transporter was null!");
        }

        myTransporters.add(transporter);
        transporter.addTransporterSet(this);

    }

    /** Returns an iterator to the transporters in this set
     *
     * @return
     */
    public final ListIterator<Transporter> getTransporterIterator() {
        return (myTransporters.listIterator());
    }

    /** Selects an idle transporter from this TransporterSet for use by the client.
     *  Returns null if no idle transporter can be recommended.  By default
     *  the recommended transporter is whatever transporter is at the top of the
     *  list of idle transporters. Since by default the transporters are added to the end
     *  of the list when they become idle, this will be the transporter that has been idle
     *  the longest.  The client can supply a TransporterSelectionRuleIfc rule to change
     *  this behavior.  Alternately the client can override this method.
     *
     *  This method only recommends an idle transporter. It does not remove it from
     *  the list of idle transporters.  The transporter will remove itself if it become
     *  non-idle
     *
     * @param request
     * @return
     */
    public Transporter selectIdleTransporter(QObject request) {

        if (myIdleTransporters.isEmpty()) {
            return (null);
        }

        if (myTransporterSelectionRule != null) {
            return (myTransporterSelectionRule.selectTransporter(myIdleTransporters, request));
        } else {
            return (myIdleTransporters.get(0));
        }

    }

    /** Returns the current transportation selection rule or null if none
     * 
     * @return
     */
    public final TransporterSelectionRuleIfc getTransporterSelectionRule() {
        return myTransporterSelectionRule;
    }

    /** Sets the transportation selection rule.  The supplied rule is responsible for
     *  both recommending an idle transporter (not removing it) and for returning an idle
     *  transporter back to the list.  This allows the rule to maintain the list in an
     *  order if necessary.
     *
     * @param rule
     */
    public final void setTransporterSelectionRule(TransporterSelectionRuleIfc rule) {
        myTransporterSelectionRule = rule;
    }

    protected boolean removeIdleTransporter(Transporter transporter) {

        if (transporter == null) {
            throw new IllegalArgumentException("The supplied transporter was null!");
        }

        if (!transporter.isIdle()) {
            throw new IllegalArgumentException("The supplied transporter is not idle!");
        }

        if (!myTransporters.contains(transporter)) {
            throw new IllegalArgumentException("The supplied transporter is not a member of this transporter set!");
        }

        if (myIdleTransporters.isEmpty()) {
            return (false);
        }

        if (!myIdleTransporters.contains(transporter)) {
            throw new IllegalArgumentException("The supplied transporter is not a member of this idle transporter set!");
        }

        myNumIdleTransporters.decrement();
        return (myIdleTransporters.remove(transporter));

    }

    protected void addIdleTransporter(Transporter transporter) {

        if (transporter == null) {
            throw new IllegalArgumentException("The supplied transporter was null!");
        }

        if (!transporter.isIdle()) {
            throw new IllegalArgumentException("The supplied transporter is not idle!");
        }

        if (!myTransporters.contains(transporter)) {
            throw new IllegalArgumentException("The supplied transporter is not a member of this transporter set!");
        }

        // add it only if it is not already in the idle list
        if (!myIdleTransporters.contains(transporter)) {
            myNumIdleTransporters.increment();
            if (myTransporterSelectionRule != null) {
                myTransporterSelectionRule.addIdleTransporter(myIdleTransporters, transporter);
            } else {
                myIdleTransporters.add(transporter);
            }

            for (TransporterProvider tp : myTransporterProviders) {
                tp.transporterFreed();
            }
        }

    }

    protected final boolean addTransporterProvider(TransporterProvider arg0) {
        return myTransporterProviders.add(arg0);
    }
}
