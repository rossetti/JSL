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

/**
 *
 * @author rossetti
 */
public class ReleaseResourceSetRequirement extends ReleaseRequirement {

    public enum ReleaseOption {

        LAST_MEMBER_SEIZED, FIRST_MEMBER_SEIZED//, SPECIFIC_MEMBER
    }

    protected ReleaseOption myReleaseOption;

    protected ResourceSet myResourceSet;

    protected String myResourceSaveKey;

    public ReleaseResourceSetRequirement(int amt) {
        super(amt);
    }

    public ResourceSet getResourceSet() {
        return myResourceSet;
    }

    public void setResourceSet(ResourceSet set) {
        myResourceSet = set;
    }

    public ReleaseOption getReleaseOption() {
        return myReleaseOption;
    }

    public void setReleaseOption(ReleaseOption option) {
        myReleaseOption = option;
    }

    public void setResourceSaveKey(String saveKey) {
        myResourceSaveKey = saveKey;
    }

    public String getResourceSaveKey(){
        return myResourceSaveKey;
    }

        @Override
    public void release(Entity e) {

        if (myReleaseOption == ReleaseOption.FIRST_MEMBER_SEIZED) {
            e.releaseFirstMemberSeized(myResourceSet, myReleaseAmount);
        } else if (myReleaseOption  == ReleaseOption.LAST_MEMBER_SEIZED) {
            e.releaseLastMemberSeized(myResourceSet, myReleaseAmount);
        } else if (myResourceSaveKey != null) {
            e.releaseSpecificMember(myResourceSet, myResourceSaveKey, myReleaseAmount);
        } else {
            throw new IllegalArgumentException("The ReleaseRequirment's option was not defined");
        }
    }

}
