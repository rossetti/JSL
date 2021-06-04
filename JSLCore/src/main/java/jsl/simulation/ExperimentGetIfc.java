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
package jsl.simulation;

/**
 *
 * @author rossetti
 */
public interface ExperimentGetIfc {

        /**
     * Gets the name.
     *
     * @return The name of object.
     */
    String getExperimentName();
    /**
     * Returns the id for this object
     *
     * @return the id
     */
    long getExperimentId();
    
    /**
     * Gets the reset next substream option. The reset next sub stream option
     * This option indicates whether or not the random variables used during the
     * replication within the experiment will be reset to their next substream
     * after running each replication. The default is TRUE. This ensures that
     * the random variables will jump to the next substream within their current
     * stream at the end of a replication. This will cause the random variables
     * in each subsequent replication to start in the same substream in the
     * underlying random number streams if the replication is repeatedly used
     * and the ResetStartStreamOption is set to false (which is the default).
     * Otherwise, this option really has no effect if there is only 1
     * replication in an experiment. and then jump to the next substream (if
     * this option is on). Having this option true assists in
     * synchronizing the random number draws from one replication to another
     * aiding in the implementation of common random numbers. Each replication
     * within the same experiment is still independent but each replication will
     * start in the same place (across experiments).
     *
     * @return true means the option is on
     */
    boolean getAdvanceNextSubStreamOption();

    /**
     * Returns the number of times that the streams should be advanced prior to
     * running the experiment
     *
     * @return number of streams to advance
     */
    int getNumberOfStreamAdvancesPriorToRunning();

    /**
     * Returns whether or not the antithetic option is turned on. True means
     * that it has been turned on.
     *
     * @return true if option is on
     */
    boolean getAntitheticOption();

    /**
     * Returns the current number of replications completed
     *
     * @return the number as a double
     */
    int getCurrentReplicationNumber();

    /**
     * Indicates whether or not System.gc() should be called after each
     * replication
     *
     * @return true if option is on 
     */
    boolean getGarbageCollectAfterReplicationFlag();

    /**
     * Returns the length of the replication as a double
     *
     * @return the length of the replication
     */
    double getLengthOfReplication();

    /**
     * Gets the length of the warm up for each replication with this experiment
     *
     * @return the the length of the warm up
     */
    double getLengthOfWarmUp();

    /**
     * Returns maximum (real) clock time allocated for the iterative process
     *
     * @return the number as long representing milliseconds
     */
    long getMaximumAllowedExecutionTimePerReplication();

    /**
     * Returns the number of replications to run
     *
     * @return the number as a double
     */
    int getNumberOfReplications();

    /**
     * Returns the setting for whether or not each replication will be
     * reinitialized prior to running.
     *
     * @return true means that each replication will be initialized
     */
    boolean getReplicationInitializationOption();

    /**
     * Gets the reset start stream option. The reset start stream option This
     * option indicates whether or not the random variables used during the
     * experiment will be reset to their starting stream prior to running the
     * first replication. The default is FALSE. This ensures that the random
     * variable's streams WILL NOT be reset prior to running the experiment.
     * This will cause different experiments or the same experiment run multiple
     * times that use the same random variables (via the same model) to continue
     * within their current stream. Therefore the experiments will be
     * independent when invoked within the same program execution. To get common
     * random number (CRN), run the experiments in different program executions
     * OR set this option to true prior to running the experiment again within
     * the same program invocation.
     *
     * @return true means the option is on
     */
    boolean getResetStartStreamOption();

    /**
     * Checks if the current number of replications that have been executed is
     * less than the number of replications specified.
     *
     * @return true if more 
     */
    boolean hasMoreReplications();

}
