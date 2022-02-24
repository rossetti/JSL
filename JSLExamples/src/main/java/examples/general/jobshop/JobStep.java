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
package examples.general.jobshop;

import jsl.utilities.random.RandomIfc;

/**
 *
 */
public class JobStep {

    private WorkStation myWorkStation;

    private RandomIfc myProcessingTime;

    /** Creates a new instance of ServiceRequirement */
    public JobStep(WorkStation w, RandomIfc r) {
        if (w == null) {
            throw new IllegalArgumentException("The workstation was null when creating the JobStep");
        }

        if (r == null) {
            throw new IllegalArgumentException("The processing time was null when creating the JobStep");
        }

        myWorkStation = w;
        myProcessingTime = r;
    }

    public WorkStation getWorkStation() {
        return (myWorkStation);
    }

    public double getProcessingTime() {
        return (myProcessingTime.getValue());
    }
}
