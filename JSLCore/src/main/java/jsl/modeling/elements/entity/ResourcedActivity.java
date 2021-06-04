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

import jsl.simulation.ModelElement;
import jsl.modeling.elements.entity.Delay.DelayOption;
import jsl.modeling.elements.entity.ReleaseResourceSetRequirement.ReleaseOption;
import jsl.modeling.elements.entity.SeizeResources.RequirementOption;
import jsl.utilities.random.RandomIfc;

/**
 *
 * @author rossetti
 */
public class ResourcedActivity extends CompositeEntityReceiver {

    protected SQSeize mySeize;
    
    protected Delay myDelay;
    
    protected ReleaseResources myRelease;
    
    public ResourcedActivity(ModelElement parent) {
        this(parent, null);
    }

    public ResourcedActivity(ModelElement parent, String name) {
        super(parent, name);
        mySeize = new SQSeize(this, getName() + "_Seize");
        myDelay = new Delay(this, getName() + "_Delay");
        myRelease = new ReleaseResources(this, getName() + "_Release");
        addInternalReceiver(mySeize);
        addInternalReceiver(myDelay);
        addInternalReceiver(myRelease);
    }

    public void setSeizeRequirementOption(RequirementOption option) {
        mySeize.setSeizeRequirementOption(option);
    }

    public RequirementOption getSeizeRequirementOption() {
        return mySeize.getSeizeRequirementOption();
    }

    public void addSeizeRequirement(ResourceSet set, int amt, int priority,
            boolean partialFillFlag, ResourceSelectionRuleIfc rule, String saveKey) {
        mySeize.addSeizeRequirement(set, amt, priority, partialFillFlag, rule, saveKey);
    }

    public void addSeizeRequirement(Resource r, int amt, int priority,
            boolean partialFillFlag) {
        mySeize.addSeizeRequirement(r, amt, priority, partialFillFlag);
    }

    public final void setDelayTime(RandomIfc distribution) {
        myDelay.setDelayTime(distribution);
    }

    public final void setDelayOption(DelayOption option) {
        myDelay.setDelayOption(option);
    }

    public void setReleaseRequirementOption(ReleaseResources.RequirementOption option) {
        myRelease.setReleaseRequirementOption(option);
    }

    public ReleaseResources.RequirementOption getReleaseRequirementOption() {
        return myRelease.getReleaseRequirementOption();
    }

    public void addReleaseRequirement(ResourceSet set, int amt,
            ReleaseOption option, String saveKey) {
        myRelease.addReleaseRequirement(set, amt, option, saveKey);
    }

    public void addReleaseRequirement(Resource r, int amt) {
        myRelease.addReleaseRequirement(r, amt);
    }

    public void addSeizeRequirement(ResourceSet set, int amt, int priority) {
        mySeize.addSeizeRequirement(set, amt, priority);
    }

    public void addSeizeRequirement(ResourceSet set, int amt, String saveKey) {
        mySeize.addSeizeRequirement(set, amt, saveKey);
    }

    public void addSeizeRequirement(ResourceSet set, int amt, ResourceSelectionRuleIfc rule) {
        mySeize.addSeizeRequirement(set, amt, rule);
    }

    public void addSeizeRequirement(ResourceSet set, int amt) {
        mySeize.addSeizeRequirement(set, amt);
    }

    public void addSeizeRequirement(ResourceSet set) {
        mySeize.addSeizeRequirement(set);
    }

    public void addSeizeRequirement(Resource r, int amt, int priority) {
        mySeize.addSeizeRequirement(r, amt, priority);
    }

    public void addSeizeRequirement(Resource r, int amt, boolean partialFillFlag) {
        mySeize.addSeizeRequirement(r, amt, partialFillFlag);
    }

    public void addSeizeRequirement(Resource r, int amt) {
        mySeize.addSeizeRequirement(r, amt);
    }

    public void addSeizeRequirement(Resource r) {
        mySeize.addSeizeRequirement(r);
    }

    public void addReleaseRequirement(ResourceSet set, int amt, String saveKey) {
        myRelease.addReleaseRequirement(set, amt, saveKey);
    }

    public void addReleaseRequirement(ResourceSet set, String saveKey) {
        myRelease.addReleaseRequirement(set, saveKey);
    }

    public void addReleaseRequirement(ResourceSet set, int amt, ReleaseOption option) {
        myRelease.addReleaseRequirement(set, amt, option);
    }

    public void addReleaseRequirement(ResourceSet set, ReleaseOption option) {
        myRelease.addReleaseRequirement(set, option);
    }

    public void addReleaseRequirement(Resource r) {
        myRelease.addReleaseRequirement(r);
    }

}
