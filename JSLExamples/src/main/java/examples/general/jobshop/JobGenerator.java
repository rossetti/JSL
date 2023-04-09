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

import java.util.Iterator;
import java.util.List;

import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.RandomElement;
import jsl.modeling.queue.QObject;
import jsl.utilities.random.RandomIfc;

/**
 * @author rossetti
 *
 */
public class JobGenerator extends EventGenerator {

    protected RandomElement<JobType> myJobTypes;
    protected JobShop myJobShop;

    /**
     * @param parent
     */
    public JobGenerator(JobShop parent, List<JobType> jobTypes, double[] typeCDf) {
        this(parent, jobTypes, typeCDf, null, null, Long.MAX_VALUE, Double.POSITIVE_INFINITY, null);
    }

    /**
     * @param parent
     * @param name
     */
    public JobGenerator(JobShop parent, List<JobType> jobTypes, double[] typeCDf, String name) {
        this(parent, jobTypes, typeCDf, null, null, Long.MAX_VALUE, Double.POSITIVE_INFINITY, name);
    }

    /**
     * @param parent
     * @param timeUntilFirst
     */
    public JobGenerator(JobShop parent, List<JobType> jobTypes, double[] typeCDf, RandomIfc timeUntilFirst) {
        this(parent, jobTypes, typeCDf, timeUntilFirst, null, Long.MAX_VALUE, Double.POSITIVE_INFINITY, null);
    }

    /**
     * @param parent
     * @param timeUntilFirst
     * @param timeUntilNext
     */
    public JobGenerator(JobShop parent, List<JobType> jobTypes, double[] typeCDf, RandomIfc timeUntilFirst,
            RandomIfc timeUntilNext) {
        this(parent, jobTypes, typeCDf, timeUntilFirst, timeUntilNext, Long.MAX_VALUE, Double.POSITIVE_INFINITY, null);
    }

    /**
     * @param parent
     * @param timeUntilFirst
     * @param timeUntilNext
     * @param name
     */
    public JobGenerator(JobShop parent, List<JobType> jobTypes, double[] typeCDf, RandomIfc timeUntilFirst,
            RandomIfc timeUntilNext, String name) {
        this(parent, jobTypes, typeCDf, timeUntilFirst, timeUntilNext, Long.MAX_VALUE, Double.POSITIVE_INFINITY, name);
    }

    /**
     * @param parent
     * @param timeUntilFirst
     * @param timeUntilNext
     * @param maxNum
     */
    public JobGenerator(JobShop parent, List<JobType> jobTypes, double[] typeCDf, RandomIfc timeUntilFirst,
            RandomIfc timeUntilNext, Long maxNum) {
        this(parent, jobTypes, typeCDf, timeUntilFirst, timeUntilNext, maxNum, Double.POSITIVE_INFINITY, null);
    }

    /**
     * @param parent
     * @param timeUntilFirst
     * @param timeUntilNext
     * @param maxNum
     * @param timeUntilLast
     */
    public JobGenerator(JobShop parent, List<JobType> jobTypes, double[] typeCDf, RandomIfc timeUntilFirst,
            RandomIfc timeUntilNext, Long maxNum,
            double timeUntilLast) {
        this(parent, jobTypes, typeCDf, timeUntilFirst, timeUntilNext, maxNum, timeUntilLast, null);
    }

    /**
     * @param parent
     * @param timeUntilFirst
     * @param timeUntilNext
     * @param maxNum
     * @param timeUntilLast
     * @param name
     */
    public JobGenerator(JobShop parent, List<JobType> jobTypes, double[] typeCDf, RandomIfc timeUntilFirst,
                        RandomIfc timeUntilNext, Long maxNum,
                        double timeUntilLast, String name) {
        super(parent, null, timeUntilFirst, timeUntilNext, maxNum, timeUntilLast, name);
        myJobShop = parent;
        //myJobTypes = new DEmpiricalList<JobType>();
        myJobTypes = new RandomElement<JobType>(this, jobTypes, typeCDf);
    }

    @Override
    protected void generate(JSLEvent event) {
        if (!myJobTypes.isEmpty()) {
            // create the job
            myJobShop.myNumInSystem.increment();
            Job job = new Job(getTime());
            // tell it to start its sequence
            job.doNextJobStep();
        }
    }

    public class Job extends QObject {

        JobType myType;

        Iterator<JobStep> myProcessPlan;

        double myServiceTime;

        Job(double time) {
            super(time);
            myType = myJobTypes.getRandomElement();
            myProcessPlan = myType.getSequence().getIterator();
            setName(myType.getName());
        }

        public void doNextJobStep() {

            if (myProcessPlan.hasNext()) {
                JobStep step = myProcessPlan.next();
                myServiceTime = step.getProcessingTime();
                WorkStation w = step.getWorkStation();
                w.arrive(this);
            } else {
                myType.getSystemTime().setValue(getTime() - getCreateTime());
                myJobShop.departSystem(this);
            }
        }

        public double getServiceTime() {
            return (myServiceTime);
        }
    }
}
