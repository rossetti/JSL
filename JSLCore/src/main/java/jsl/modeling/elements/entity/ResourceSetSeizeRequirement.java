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
package jsl.modeling.elements.entity;

/**
 *
 * @author rossetti
 */
public class ResourceSetSeizeRequirement extends SeizeRequirement {
    
    protected ResourceSet myResourceSet;

    protected ResourceSelectionRuleIfc myRule;

    protected String mySaveResourceKey;

    public ResourceSetSeizeRequirement(ResourceSet resource) {
        this(resource, 1, Request.DEFAULT_PRIORITY, false, 
                ResourceSet.CYCLICAL, null);
    }

    public ResourceSetSeizeRequirement(ResourceSet resource, int amt) {
        this(resource, amt, Request.DEFAULT_PRIORITY, false, 
                ResourceSet.CYCLICAL, null);
    }

    public ResourceSetSeizeRequirement(ResourceSet resource, int amt, int priority) {
        this(resource, amt, priority, false, ResourceSet.CYCLICAL,
                null);
    }

    public ResourceSetSeizeRequirement(ResourceSet resource, int amt, int priority,
            boolean partialFillFlag, ResourceSelectionRuleIfc rule, String saveKey) {
        super(amt, priority, partialFillFlag);
        if (resource == null) {
            throw new IllegalArgumentException("The SeizeIfc was null");
        }

        myResourceSet = resource;
        setResourceSelectionRule(rule);
        setSaveResourceKey(saveKey);
    }

    public ResourceSelectionRuleIfc getResourceSelectionRule() {
        return myRule;
    }

    public void setResourceSelectionRule(ResourceSelectionRuleIfc rule) {
        if (rule == null) {
            throw new IllegalArgumentException("The ResourceSelectionRuleIfc was null");
        }
        myRule = rule;
    }

    @Override
    public SeizeIfc getResource() {
        return myResourceSet;
    }

    @Override
    public Request createRequest(Entity entity, AllocationListenerIfc listener) {
        Request r = new Request(myAmtNeeded, myPriority, myPartialFillFlag);
        r.setEntity(entity);
        r.setResourceAllocationListener(listener);
        r.setResourceSelectionRule(myRule);
//        r.setSeizeRequirement(this);
        r.setResourceSaveKey(mySaveResourceKey);
        return r;
    }

    public String getSaveResourceKey() {
        return mySaveResourceKey;
    }

    public void setSaveResourceKey(String resourceKey) {
        mySaveResourceKey = resourceKey;
    }


}
