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
package jsl.simulation;

import java.util.Map;
import java.util.Optional;

/**
 * This class provides the information for running a simulation experiment. An
 * experiment is a specification for the number of replications, the warm-up
 * length, replication length, etc. for controlling the running of a simulation.
 *
 * The defaults include:
 * - length of replication = Double.POSITIVE_INFINITY
 *
 * - length of warm up = 0.0
 *
 * - replication initialization TRUE - The system state is re-initialized prior to each replication
 *
 * - reset start stream option FALSE - Do not reset the streams of the random variables to their
 * starting points prior to running the replications within the experiment. This
 * implies that if the experiment is re-run on the same model in the same code
 * invocation that an independent set of replications will be made.
 *
 * - advance next sub-stream option TRUE - The random variables in a within an experiment
 * will start at the next sub-stream for each new replication
 *
 * - number of times to advance streams = 1 This indicates how many times that the streams should
 * be advanced prior to running the experiment. This can be used to ensure
 * simulations start with different streams
 *
 * - antithetic replication option is off by default
 *
 */
public class Experiment implements ExperimentGetIfc {

    /**
     * A counter to count the number of objects created to assign "unique" ids
     */
    private static int myIdCounter_;

    /**
     * The name of this object
     */
    private String myName;

    /**
     * The id of this object
     */
    private int myId;

    /**
     * The number of replications to run for this experiment
     *
     */
    private int myNumReps;

    /**
     * The current number of replications that have been run for this experiment
     */
    private int myCurRepNum;

    /**
     * The specified length of each planned replication for this experiment. The
     * default is Double.POSITIVE_INFINITY.
     */
    private double myLengthOfReplication = Double.POSITIVE_INFINITY;

    /**
     * The length of time from the start of an individual replication to the
     * warm-up event for that replication.
     */
    private double myLengthOfWarmUp = 0.0; // zero is no warmup

    /**
     * A flag to indicate whether each replication within the experiment
     * should be re-initialized at the beginning of each replication. True means
     * that it will be re-initialized.
     */
    private boolean myRepInitOption;

    /**
     * The maximum allowable execution time "wall" clock time for an individual
     * replication to complete processing in milliseconds
     */
    private long myMaxAllowedExecutionTimePR;

    /**
     * The reset start stream option This option indicates whether the
     * random variables used during the experiment will be reset to their
     * starting stream prior to running the first replication. The default is
     * FALSE. This ensures that the random variable's streams WILL NOT be reset
     * prior to running the experiment. This will cause different experiments or
     * the same experiment run multiple times that use the same random variables
     * (via the same model) to continue within their current stream. Therefore,
     * the experiments will be independent when invoked within the same program
     * execution. To get common random number (CRN), run the experiments in
     * different program executions OR set this option to true prior to running
     * the experiment again within the same program invocation.
     */
    private boolean myResetStartStreamOption;

    /**
     * The reset next sub stream option This option indicates whether the
     * random variables used during the replication within the experiment will
     * be reset to their next sub-stream after running each replication. The
     * default is TRUE. This ensures that the random variables will jump to the
     * next sub-stream within their current stream at the end of a replication.
     * This will cause the random variables in each subsequent replication to
     * start in the same sub-stream in the underlying random number streams if
     * the replication is repeatedly used and the ResetStartStreamOption is set
     * to false (which is the default) and then jump to the next sub-stream (if
     * this option is on). This option has no effect if there is only 1
     * replication in an experiment.
     *
     * Having ResetNextSubStreamOption true assists in synchronizing the random
     * number draws from one replication to another aiding in the implementation
     * of common random numbers. Each replication within the same experiment is
     * still independent.
     */
    private boolean myAdvNextSubStreamOption;

    /**
     * Indicates whether antithetic replications should be run. The
     * default is false. If set the user must supply an even number of
     * replications; otherwise an exception will be thrown. The replications
     * will no longer be independent; however, pairs of replications will be
     * independent. Thus, the number of independent samples will be one-half of
     * the specified number of replications
     */
    private boolean myAntitheticOption;

    /**
     * Indicates the number of times the streams should be advanced prior to
     * running the experiment
     *
     */
    private int myAdvStreamNum;

    /**
     * Causes garbage collection System.gc() to be invoked after each
     * replication. The default is false
     *
     */
    private boolean myGCAfterRepFlag;

    /**
     *  Holds values for each controllable parameter of the simulation
     *  model.
     */
    private Map<String, Double> myControls;

    /**
     * Constructs an experiment called "Experiment" with antithetic option off
     */
    public Experiment() {
        this(null);
    }

    /**
     * Constructs an experiment called "name"
     *
     * @param name The name of the experiment
     */
    public Experiment(String name) {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
        setExperimentName(name);
        myNumReps = 1;
        myLengthOfReplication = Double.POSITIVE_INFINITY;
        myLengthOfWarmUp = 0.0; // zero means no warm up
        myRepInitOption = true;
        myMaxAllowedExecutionTimePR = 0; // zero means not used
        myResetStartStreamOption = false;
        myAdvNextSubStreamOption = true;
        myAntitheticOption = false;
        myAdvStreamNum = 0;
        myGCAfterRepFlag = false;
    }

    /**
     *
     * @return true if a control map has been supplied
     */
    @Override
    public final boolean hasControls(){
        return myControls != null;
    }

    /** Indicates that the experiment should be run with these control values.
     *
     * @param controlMap the controls to use, may be null to stop use of controls
     */
    @Override
    public final void useControls(Map<String, Double> controlMap){
        myControls = controlMap;
    }

    /**
     *
     * @return the control map if it was set
     */
    @Override
    public final Optional<Map<String, Double>> getControls(){
        return Optional.ofNullable(myControls);
    }

    /**
     * Gets the name.
     *
     * @return The name of object.
     */
    @Override
    public final String getExperimentName() {
        return myName;
    }

    /**
     * Returns the id for this object
     *
     * @return the identifier
     */
    @Override
    public final long getExperimentId() {
        return (myId);
    }

    /**
     * Sets the name
     *
     * @param str The name as a string.
     */
    public final void setExperimentName(String str) {
        if (str == null) {
            myName = this.getClass().getSimpleName();
        } else {
            myName = str;
        }
    }

    /**
     * Returns the number of times that the streams should be advanced prior to
     * running the experiment
     *
     * @return the number to advance
     */
    @Override
    public final int getNumberOfStreamAdvancesPriorToRunning() {
        return myAdvStreamNum;
    }

    /**
     * Sets the number of times that the streams should be advanced prior to
     * running the experiment
     *
     * @param n must be &gt; 0 if supplied
     */
    final void setNumberOfStreamAdvancesPriorToRunning(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("The number times to advance the stream must be > 0");
        }
        myAdvStreamNum = n;
    }

    /**
     * Returns the number of replications to run
     *
     * @return the number as a double
     */
    @Override
    public final int getNumberOfReplications() {
        return (myNumReps);
    }

    /**
     * Sets the desired number of replications for the experiment
     *
     * @param numReps must be &gt; 0
     */
    public final void setNumberOfReplications(int numReps) {
        setNumberOfReplications(numReps, false);
    }

    /**
     * Sets the desired number of replications for the experiment
     *
     * @param numReps must be &gt; 0, and even (divisible by 2) if antithetic
     * option is true
     * @param antitheticOption controls whether antithetic replications occur
     */
    public final void setNumberOfReplications(int numReps, boolean antitheticOption) {
        if (numReps <= 0) {
            throw new IllegalArgumentException("Number of replications <= 0");
        }

        if (antitheticOption == true) {
            if ((numReps % 2) != 0) {
                throw new IllegalArgumentException("Number of replications must be even if antithetic option is on.");
            }
            myAntitheticOption = true;
        }

        myNumReps = numReps;

    }

    /**
     * Returns the current number of replications completed
     *
     * @return the number as a double
     */
    @Override
    public final int getCurrentReplicationNumber() {
        return (myCurRepNum);
    }

    /**
     * Checks if the current number of replications that have been executed is
     * less than the number of replications specified.
     *
     * @return true if more
     */
    @Override
    public final boolean hasMoreReplications() {
        return myCurRepNum < myNumReps;
    }

    /**
     * Returns whether or not the antithetic option is turned on. True means
     * that it has been turned on.
     *
     * @return true if on
     */
    @Override
    public final boolean getAntitheticOption() {
        return myAntitheticOption;
    }

    /**
     * Returns the length of the replication as a double
     *
     * @return the length of the replication
     */
    @Override
    public final double getLengthOfReplication() {
        return (myLengthOfReplication);
    }

    /**
     * Sets the length of each replication for this experiment. The length of
     * the replication must be &gt; 0, if set
     *
     * @param lengthOfReplication the length of the replication
     */
    public final void setLengthOfReplication(double lengthOfReplication) {
        if (lengthOfReplication <= 0.0) {
            throw new IllegalArgumentException("Simulation replication length < 0.0");
        }
        myLengthOfReplication = lengthOfReplication;
    }

    /**
     * Gets the length of the warm up for each replication with this experiment
     *
     * @return the the length of the warm up
     */
    @Override
    public final double getLengthOfWarmUp() {
        return (myLengthOfWarmUp);
    }

    /**
     * Sets the length of the warm up for each replication within this
     * experiment The length of the warm up must be &gt; 0.0, if set. If the
     * warm up is set greater than the run length then there will be no warm up
     *
     * @param lengthOfWarmUp length of the warm up, must be &gt;= 0.0
     */
    public final void setLengthOfWarmUp(double lengthOfWarmUp) {
        if (lengthOfWarmUp < 0.0) {
            throw new IllegalArgumentException("Warmup time cannot be less than zero");
        }

        myLengthOfWarmUp = lengthOfWarmUp;
    }

    /**
     * Returns the setting for whether or not each replication will be
     * reinitialized prior to running.
     *
     * @return true means that each replication will be initialized
     */
    @Override
    public final boolean getReplicationInitializationOption() {
        return (myRepInitOption);
    }

    /**
     * Sets the replication initialization option for the experiment If set to
     * true, each replication within the experiment will be initialized prior to
     * running.
     *
     * @param repInitOption true means to initialize
     */
    public final void setReplicationInitializationOption(boolean repInitOption) {
        myRepInitOption = repInitOption;
    }

    /**
     * Returns maximum (real) clock time allocated for the iterative process
     *
     * @return the number as long representing milliseconds
     */
    @Override
    public final long getMaximumAllowedExecutionTimePerReplication() {
        return (myMaxAllowedExecutionTimePR);
    }

    /**
     * Set the maximum allotted (suggested) execution (real) clock for the
     * entire iterative process. This is suggested because the execution time
     * requirement is only checked after the completion of an individual step
     * After it is discovered that cumulative time for executing the step has
     * exceeded the maximum time, then the iterative process will be ended
     * (perhaps) not completing other steps.
     *
     * @param milliseconds the maximum time
     */
    public final void setMaximumExecutionTimePerReplication(long milliseconds) {
        if (milliseconds <= 0.0) {
            throw new IllegalArgumentException("The maximum number of execution time (clock time) must be > 0.0");
        }
        myMaxAllowedExecutionTimePR = milliseconds;
    }

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
     * this option is on). Having ResetNextSubStreamOption true assists in
     * synchronizing the random number draws from one replication to another
     * aiding in the implementation of common random numbers. Each replication
     * within the same experiment is still independent.
     *
     * @return true means the option is on
     */
    @Override
    public final boolean getAdvanceNextSubStreamOption() {
        return myAdvNextSubStreamOption;
    }

    /**
     * Sets the reset next substream option. The reset next sub stream option
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
     * this option is on). Having ResetNextSubStreamOption true assists in
     * synchronizing the random number draws from one replication to another
     * aiding in the implementation of common random numbers. Each replication
     * within the same experiment is still independent.
     *
     * @param b true means option is on
     */
    public final void setAdvanceNextSubStreamOption(boolean b) {
        myAdvNextSubStreamOption = b;
    }

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
    @Override
    public final boolean getResetStartStreamOption() {
        return myResetStartStreamOption;
    }

    /**
     * Sets the reset start stream option. The reset start stream option This
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
     * @param b true if reset desired
     */
    public final void setResetStartStreamOption(boolean b) {
        myResetStartStreamOption = b;
    }

    /**
     * Indicates whether or not System.gc() should be called after each
     * replication
     *
     * @return true if on
     */
    @Override
    public boolean getGarbageCollectAfterReplicationFlag() {
        return myGCAfterRepFlag;
    }

    /**
     * Indicates whether or not System.gc() should be called after each
     * replication
     *
     * @param flag true if on
     */
    public void setGarbageCollectAfterReplicationFlag(boolean flag) {
        myGCAfterRepFlag = flag;
    }

    /**
     * Sets all attributes of this experiment to the same values as the supplied
     * experiment (except for getId()).
     *
     * @param e the experiment to copy
     */
    public void setExperiment(Experiment e) {
        myName = e.myName;
        myNumReps = e.myNumReps;
        myCurRepNum = e.myCurRepNum;
        myLengthOfReplication = e.myLengthOfReplication;
        myLengthOfWarmUp = e.myLengthOfWarmUp;
        myRepInitOption = e.myRepInitOption;
        myResetStartStreamOption = e.myResetStartStreamOption;
        myAdvNextSubStreamOption = e.myAdvNextSubStreamOption;
        myAntitheticOption = e.myAntitheticOption;
        myAdvStreamNum = e.myAdvStreamNum;
        myMaxAllowedExecutionTimePR = e.myMaxAllowedExecutionTimePR;
        myGCAfterRepFlag = e.myGCAfterRepFlag;
    }

    /**
     * Returns a new Experiment based on the supplied experiment.
     *
     * Essentially a clone, except for the getId()
     *
     * @return a new Experiment
     */
    public Experiment newInstance() {
        Experiment n = new Experiment();
        n.myName = myName;
        n.myNumReps = myNumReps;
        n.myCurRepNum = myCurRepNum;
        n.myLengthOfReplication = myLengthOfReplication;
        n.myLengthOfWarmUp = myLengthOfWarmUp;
        n.myRepInitOption = myRepInitOption;
        n.myResetStartStreamOption = myResetStartStreamOption;
        n.myAdvNextSubStreamOption = myAdvNextSubStreamOption;
        n.myAntitheticOption = myAntitheticOption;
        n.myAdvStreamNum = myAdvStreamNum;
        n.myMaxAllowedExecutionTimePR = myMaxAllowedExecutionTimePR;
        n.myGCAfterRepFlag = myGCAfterRepFlag;
        return n;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Experiment Name: ");
        sb.append(getExperimentName());
        sb.append("\n");
        sb.append("Experiment ID: ");
        sb.append(getExperimentId());
        sb.append("\n");
        sb.append("Planned number of replications: ");
        sb.append(getNumberOfReplications());
        sb.append("\n");
        sb.append("Replication initialization option: ");
        sb.append(getReplicationInitializationOption());
        sb.append("\n");
        sb.append("Antithetic option: ");
        sb.append(getAntitheticOption());
        sb.append("\n");
        sb.append("Reset start stream option: ");
        sb.append(getResetStartStreamOption());
        sb.append("\n");
        sb.append("Reset next substream option: ");
        sb.append(getAdvanceNextSubStreamOption());
        sb.append("\n");
        sb.append("Number of stream advancements: ");
        sb.append(getNumberOfStreamAdvancesPriorToRunning());
        sb.append("\n");
        sb.append("Planned time horizon for replication: ");
        sb.append(getLengthOfReplication());
        sb.append("\n");
        sb.append("Warm up time period for replication: ");
        sb.append(getLengthOfWarmUp());
        sb.append("\n");
        long et = getMaximumAllowedExecutionTimePerReplication();
        if (et == 0) {
            sb.append("Maximum allowed replication execution time not specified.");
        } else {
            sb.append("Maximum allowed replicaton execution time: ");
            sb.append(et);
            sb.append(" milliseconds.");
        }
        sb.append("\n");
        sb.append("Current Replication Number: ");
        sb.append(getCurrentReplicationNumber());
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Resets the current replication number to zero
     *
     */
    protected final void resetCurrentReplicationNumber() {
        myCurRepNum = 0;
    }

    /**
     * Increments the number of replications that has been executed
     *
     */
    protected final void incrementCurrentReplicationNumber() {
        myCurRepNum = myCurRepNum + 1;
    }
}
