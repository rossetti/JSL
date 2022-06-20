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

import jsl.observers.ObservableIfc;
import jsl.observers.ObserverIfc;
import jsl.observers.scheduler.ExecutiveTraceReport;
import jsl.observers.textfile.IPLogReport;
import jsl.utilities.reporting.LogPrintWriter;
import jsl.utilities.reporting.OutputDirectory;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.StatisticAccessorIfc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static jsl.utilities.reporting.JSL.getInstance;

/**
 * Simulation represents a model and experiment that can be run. It encapsulates
 * a model to which model elements can be attached. It allows an experiment and
 * its run parameters to be specified. It allows reporting of results
 * to text files via a SimulationReporter.
 *
 * If you want to store simulation results in a database, then use the JSLDatabaseObserver
 * class to observe the simulation with an instance of the JSLDatabase class.
 *
 * @author Manuel Rossetti (rossetti@uark.edu)
 */
public class Simulation implements ObservableIfc, IterativeProcessIfc,
        ExperimentGetIfc {

    /**
     * for logging
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * A counter to count the number of objects created to assign "unique" ids
     */
    private static int myIdCounter_;

    /**
     * The name of this object
     */
    private final String myName;

    /**
     * The id of this object
     */
    private final int myId;

    /**
     * The executive for running events
     */
    private final Executive myExecutive;

    /**
     * The experiment for running the simulation
     */
    private final Experiment myExperiment;

    /**
     * The model to simulate
     */
    protected final Model myModel;

    /**
     * Controls the execution of replications
     */
    private final ReplicationExecutionProcess myReplicationExecutionProcess;

    /**
     * A flag to control whether or not a warning is issued if the user does not
     * set the replication run length
     */
    private boolean myRepLengthWarningMsgOption = true;

    /**
     * Used to control statistical batching
     */
    private StatisticalBatchingElement myBatchingElement;

    private final OutputDirectory myOutputDirectory;

    /**
     * Creates a simulation within a default name using the default executive
     */
    public Simulation(){
        this(null, null, null);
    }

    /**
     * Creates a simulation to run a model using the default scheduling executive.
     *
     * @param simName the name of the simulation
     */
    public Simulation(String simName) {
        this(simName, null, null);
    }

    /**
     * Creates a simulation to run a model using the default scheduling executive.
     *
     * @param simName the name of the simulation
     * @param pathToOutputDirectory the path to the output directory for the simulation files
     */
    public Simulation(String simName, Path pathToOutputDirectory) {
        this(simName, pathToOutputDirectory, null);
    }

    /**
     * Creates a simulation to run a model using the supplied scheduling executive.
     *
     * @param simName               the name of the simulation
     * @param pathToOutputDirectory the path to the output directory for the simulation files
     * @param executive             the executive, if null the default executive is used
     */
    public Simulation(String simName, Path pathToOutputDirectory, Executive executive) {
        myIdCounter_ = myIdCounter_ + 1;
        myId = myIdCounter_;
        if (simName == null) {
            simName = this.getClass().getSimpleName() + "_" + getId();
        }
        myName = simName;
        if (pathToOutputDirectory == null){
            // construct a default path within jslOutput, with name of simulation and _OutputDir to hold output files
            pathToOutputDirectory = getInstance().makeSubDirectory(myName+"_OutputDir");
        }
        myOutputDirectory = new OutputDirectory(pathToOutputDirectory, "jslOutput.txt");
        myReplicationExecutionProcess = new ReplicationExecutionProcess();
        if (executive == null) {
            executive = new Executive();
        }
        myExecutive = executive;
        myExecutive.setSimulation(this);
        String modelName = getName() + "_Model";
        myModel = new Model(modelName);
        String expName = getName() + "_Experiment";
        myExperiment = new Experiment(expName);
        //setSimulation() is package final, should be no leaking this
        myModel.setSimulation(this);
    }

    /**
     *
     * @return the defined OutputDirectory for the simulation
     */
    public final OutputDirectory getOutputDirectory() {
        return myOutputDirectory;
    }

    /**
     *
     * @return the path to the simulation's output directory
     */
    public final Path getOutputDirectoryPath(){
        return getOutputDirectory().getOutDir();
    }

    /**
     *
     * @return the pre-defined default text output file for the simulation
     */
    public final LogPrintWriter out(){
        return getOutputDirectory().out;
    }

    @Override
    public final String getName() {
        return myName;
    }

    public final long getId() {
        return (myId);
    }

    /**
     * The Experiment associated with the simulation
     *
     * @return the experiment
     */
    public final ExperimentGetIfc getExperiment() {
        return myExperiment;
    }

    /**
     * The Model associated with the simulation
     *
     * @return the model
     */
    public final Model getModel() {
        return myModel;
    }

    /**
     * The Executive associated with the simulation
     *
     * @return the Executive
     */
    public final Executive getExecutive() {
        return myExecutive;
    }

    /**
     * A StatisticalBatchingElement is used to control statistical batching for
     * single replication simulations. This method creates and attaches a
     * StatisticalBatchingElement to the model. For convenience, it also returns
     * the created element
     * @return the StatisticalBatchingElement
     */
    public final StatisticalBatchingElement turnOnStatisticalBatching() {
        if (myBatchingElement == null) {
            myBatchingElement = new StatisticalBatchingElement(getModel());
        }
        return myBatchingElement;
    }

    /**
     * @return an optional of the StatisticalBatchingElement because it may or may not be attached
     */
    public final Optional<StatisticalBatchingElement> getStatisticalBatchingElement() {
        return Optional.ofNullable(myBatchingElement);
    }

    @Override
    public final void deleteObservers() {
        myReplicationExecutionProcess.deleteObservers();
    }

    @Override
    public final void deleteObserver(ObserverIfc observer) {
        myReplicationExecutionProcess.deleteObserver(observer);
    }

    @Override
    public final int countObservers() {
        return myReplicationExecutionProcess.countObservers();
    }

    /**
     * Allows an observer to be added to the simulation. The observer observes
     * an IterativeProcess that manages the execution of the replications. Each
     * step in the IterativeProcess represents an entire replication.
     *
     * @param observer the observer
     */
    @Override
    public final void addObserver(ObserverIfc observer) {
        myReplicationExecutionProcess.addObserver(observer);
    }

    @Override
    public boolean contains(ObserverIfc observer) {
        return myReplicationExecutionProcess.contains(observer);
    }

    /**
     * Returns true if additional replications need to be run
     *
     * @return true if additional replications need to be run
     */
    public final boolean hasNextReplication() {
        return myReplicationExecutionProcess.hasNext();
    }

    /**
     * Initializes the simulation in preparation for running
     */
    @Override
    public final void initialize() {
        myReplicationExecutionProcess.initialize();
    }

    /**
     * Runs the next replication if there is one
     */
    @Override
    public final void runNext() {
        myReplicationExecutionProcess.runNext();
    }

    /** A convenience method for running a simulation
     *
     * @param numReps the number of replications
     * @param runLength the length of the simulation replication
     */
    public final void run(int numReps, double runLength){
        setNumberOfReplications(numReps);
        setLengthOfReplication(runLength);
        run();
    }

    /** A convenience method for running a simulation
     *
     * @param expName the name of the experiment
     * @param numReps the number of replications
     * @param runLength the length of the simulation replication
     */
    public final void run(String expName, int numReps, double runLength){
        setExperimentName(expName);
        setNumberOfReplications(numReps);
        setLengthOfReplication(runLength);
        run();
    }

    /** A convenience method for running a simulation
     *
     * @param expName the name of the experiment
     * @param numReps the number of replications
     * @param runLength the length of the simulation replication
     * @param warmUp the length of the warmup period
     */
    public final void run(String expName, int numReps, double runLength, double warmUp){
        setExperimentName(expName);
        setNumberOfReplications(numReps);
        setLengthOfReplication(runLength);
        setLengthOfWarmUp(warmUp);
        run();
    }

    /**
     * Runs all remaining replications based on the current settings
     */
    @Override
    public final void run() {
        myReplicationExecutionProcess.run();
    }

    /**
     * Causes the simulation to end after the current replication is completed
     *
     * @param msg A message to indicate why the simulation was stopped
     */
    @Override
    public final void end(String msg) {
        myReplicationExecutionProcess.end(msg);
    }

    /**
     * Causes the simulation to end after the current replication is completed
     */
    @Override
    public final void end() {
        myReplicationExecutionProcess.end();
    }

    /**
     * Checks if the replications were finished
     *
     * @return true if unfinished
     */
    @Override
    public final boolean isUnfinished() {
        return myReplicationExecutionProcess.isUnfinished();
    }

    /**
     * Checks if the simulation stopped because of real clock time
     *
     * @return true if exceeded
     */
    @Override
    public final boolean executionTimeExceeded() {
        return myReplicationExecutionProcess.executionTimeExceeded();
    }

    /**
     * Part of the IterativeProcessIfc. Checks if a step in the process is
     * completed. A step is a replication Checks if the state of the simulation
     * is that it just completed a replication
     *
     * @return true if completed
     */
    @Override
    public final boolean isStepCompleted() {
        return myReplicationExecutionProcess.isStepCompleted();
    }

    /**
     * Checks if the simulation is running. Running means that it is executing
     * replications
     *
     * @return true means it is running
     */
    @Override
    public final boolean isRunning() {
        return myReplicationExecutionProcess.isRunning();
    }

    /**
     * Checks if the simulation has been initialized. If it is initialized, then
     * it is ready to run replications
     *
     * @return true if initialized
     */
    @Override
    public final boolean isInitialized() {
        return myReplicationExecutionProcess.isInitialized();
    }

    /**
     * Checks to see if the simulation is in the ended state If it is ended, it
     * may be for a number of reasons
     *
     * @return true if end
     */
    @Override
    public final boolean isEnded() {
        return myReplicationExecutionProcess.isEnded();
    }

    @Override
    public final boolean isCreated() {
        return myReplicationExecutionProcess.isCreated();
    }

    /**
     * Checks if the simulation has ended because it was stopped
     *
     * @return true if stopped by condition
     */
    @Override
    public final boolean stoppedByCondition() {
        return myReplicationExecutionProcess.stoppedByCondition();
    }

    /**
     * Checks if the simulation is done processing replications
     *
     * @return true if done
     */
    @Override
    public final boolean isDone() {
        return myReplicationExecutionProcess.isDone();
    }

    /**
     * Checks if the simulation completed all of its replications
     *
     * @return true if all
     */
    @Override
    public final boolean allStepsCompleted() {
        return myReplicationExecutionProcess.allStepsCompleted();
    }

    /**
     * Sets a real clock time for how long the entire simulation can last
     *
     * @param milliseconds the max allowed
     */
    @Override
    public final void setMaximumExecutionTime(long milliseconds) {
        myReplicationExecutionProcess.setMaximumExecutionTime(milliseconds);
    }

    /**
     * Returns the real clock time in milliseconds for how long the simulation
     * is allowed to run
     *
     * @return the max allowed
     */
    @Override
    public final long getMaximumAllowedExecutionTime() {
        return myReplicationExecutionProcess.getMaximumAllowedExecutionTime();
    }

    /**
     * The absolute time in milliseconds that the simulation ended
     *
     * @return the end time (in real clock time)
     */
    @Override
    public final long getEndExecutionTime() {
        return myReplicationExecutionProcess.getEndExecutionTime();
    }

    /**
     * The time in milliseconds between when the simulation was started and the
     * simulation ended
     *
     * @return the elapsed time (in real clock time)
     */
    @Override
    public final long getElapsedExecutionTime() {
        return myReplicationExecutionProcess.getElapsedExecutionTime();
    }

    /**
     * The absolute time in milliseconds that the simulation was started
     *
     * @return the begin time (in real clock time)
     */
    @Override
    public final long getBeginExecutionTime() {
        return myReplicationExecutionProcess.getBeginExecutionTime();
    }

    /**
     * The message supplied with stop()
     *
     * @return the stopping message
     */
    @Override
    public final String getStoppingMessage() {
        return myReplicationExecutionProcess.getStoppingMessage();
    }

    /**
     * Turns on a default timer and task to report on simulation progress
     *
     * @param milliseconds time for timer task
     */
    @Override
    public final void turnOnTimer(long milliseconds) {
        myReplicationExecutionProcess.turnOnTimer(milliseconds);
    }

    public final void turnOnLogReport(){
        turnOnLogReport(getName() + "_LogReport.txt");
    }
    /**
     *
     * @param reportFileName the name of the report
     */
    public final void turnOnLogReport(String reportFileName){
        Objects.requireNonNull("The name of the report was null");
        Path pathToFile = getOutputDirectory().getOutDir().resolve(reportFileName);
        turnOnLogReport(pathToFile);
    }

    /**
     * Turns on a default logging report with the provided name
     *
     * @param pathToFile the path to the file
     */
    @Override
    public final void turnOnLogReport(Path pathToFile) {
        myReplicationExecutionProcess.turnOnLogReport(pathToFile);
    }

    /**
     * Turns of the default logging report
     */
    @Override
    public final void turnOffLogReport() {
        myReplicationExecutionProcess.turnOffLogReport();
    }

    /**
     * For the IterativeProcessIfc. Returns the number of steps (replications)
     * completed
     *
     * @return number of steps completed
     */
    @Override
    public final long getNumberStepsCompleted() {
        return myReplicationExecutionProcess.getNumberStepsCompleted();
    }

    /**
     * Gets the IPLogReport that was attached to the simulation
     *
     * @return the IPLogReport
     */
    @Override
    public final IPLogReport getLogReport() {
        return myReplicationExecutionProcess.getLogReport();
    }

    /**
     * Returns the current number of replications completed
     *
     * @return the number as a double
     */
    @Override
    public final int getCurrentReplicationNumber() {
        return (myExperiment.getCurrentReplicationNumber());
    }

    @Override
    public final boolean hasMoreReplications() {
        return myExperiment.hasMoreReplications();
    }

    /**
     * Returns the number of replications for the experiment
     *
     * @return the number of replications
     */
    @Override
    public final int getNumberOfReplications() {
        return myExperiment.getNumberOfReplications();
    }

    @Override
    public final int getNumberOfStreamAdvancesPriorToRunning() {
        return myExperiment.getNumberOfStreamAdvancesPriorToRunning();
    }

    /**
     * If set to true then the streams will be reset to the start of the
     * stream prior to running the experiments. True facilitates the use of
     * common random numbers.
     *
     * @param b true means option is on
     */
    public final void setResetStartStreamOption(boolean b) {
        myExperiment.setResetStartStreamOption(b);
    }

    /**
     * Sets the option to have the streams advance to the beginning of the next
     * substream after each replication
     *
     * @param b true means option is on
     */
    public final void setAdvanceNextSubStreamOption(boolean b) {
        myExperiment.setAdvanceNextSubStreamOption(b);
    }

    /**
     * Sets whether or not the replication should be initialized before each
     * replication
     *
     * @param repInitOption true for initialize
     */
    public final void setReplicationInitializationOption(boolean repInitOption) {
        myExperiment.setReplicationInitializationOption(repInitOption);
    }

    /**
     * Sets the number of replications to be executed and whether or not the
     * antithetic option is on. If the antithetic option is on then the number
     * of replications should be divisible by 2 so that antithetic pairs can be
     * formed.
     *
     * @param numReps          number of replications
     * @param antitheticOption true means option is on
     */
    public final void setNumberOfReplications(int numReps, boolean antitheticOption) {
        myExperiment.setNumberOfReplications(numReps, antitheticOption);
    }

    /**
     * Sets the number of replications to be executed. The antithetic option is
     * off
     *
     * @param numReps number of replications
     */
    public final void setNumberOfReplications(int numReps) {
        myExperiment.setNumberOfReplications(numReps);
    }

    /**
     * Sets in real clock time (milliseconds) the amount of time available for
     * each replication within the simulation. If the replication lasts longer
     * than the supplied time it will be stopped
     *
     * @param milliseconds clock time for a replication
     */
    public final void setMaximumExecutionTimePerReplication(long milliseconds) {
        myExperiment.setMaximumExecutionTimePerReplication(milliseconds);
    }

    /**
     * Allows the length of the warm up period for each replication to be set
     *
     * @param lengthOfWarmUp in simulation time
     */
    public final void setLengthOfWarmUp(double lengthOfWarmUp) {
        myExperiment.setLengthOfWarmUp(lengthOfWarmUp);
    }

    /**
     * Sets the length of the replications in simulation time.
     *
     * @param lengthOfReplication the length of the replication
     */
    public final void setLengthOfReplication(double lengthOfReplication) {
        myExperiment.setLengthOfReplication(lengthOfReplication);
    }

    @Override
    public final String getExperimentName() {
        return myExperiment.getExperimentName();
    }

    @Override
    public final long getExperimentId() {
        return myExperiment.getExperimentId();
    }

    /**
     * Returns whether or not the start stream will be reset prior to executing
     * the simulation
     *
     * @return true if option is on
     */
    @Override
    public final boolean getResetStartStreamOption() {
        return myExperiment.getResetStartStreamOption();
    }

    /**
     * Returns how many times the random number streams will be advanced before
     * the simulation starts.
     *
     * @return true if option is on
     */
    @Override
    public final boolean getAdvanceNextSubStreamOption() {
        return myExperiment.getAdvanceNextSubStreamOption();
    }

    /**
     * Returns whether or not replications will be initialized prior to running
     * each replication
     *
     * @return true if replications will be initialized
     */
    @Override
    public final boolean getReplicationInitializationOption() {
        return myExperiment.getReplicationInitializationOption();
    }

    /**
     * Gets in real clock time (milliseconds) the amount of time available for
     * each replication within the simulation. If the replication lasts longer
     * than the supplied time it will be stopped
     *
     * @return the clock time allowed
     */
    @Override
    public final long getMaximumAllowedExecutionTimePerReplication() {
        return myExperiment.getMaximumAllowedExecutionTimePerReplication();
    }

    /**
     * Provides the length of the warm up period for each replication
     *
     * @return the length of the warm up period
     */
    @Override
    public final double getLengthOfWarmUp() {
        return myExperiment.getLengthOfWarmUp();
    }

    /**
     * Provides the length of each replication
     *
     * @return the length of the replication
     */
    @Override
    public final double getLengthOfReplication() {
        return myExperiment.getLengthOfReplication();
    }

    /**
     * Indicates whether or not the antithetic streams have been turn on or off
     *
     * @return true means option is on
     */
    @Override
    public final boolean getAntitheticOption() {
        return myExperiment.getAntitheticOption();
    }

    /**
     * Determines whether or not System.gc() is called after each replication
     *
     * @param flag true means yes
     */
    public final void setGarbageCollectAfterReplicationFlag(boolean flag) {
        myExperiment.setGarbageCollectAfterReplicationFlag(flag);
    }

    /**
     * Returns whether or not System.gc() is called after each replication
     *
     * @return true means yes
     */
    @Override
    public final boolean getGarbageCollectAfterReplicationFlag() {
        return myExperiment.getGarbageCollectAfterReplicationFlag();
    }

    /**
     * Sets the number of streams to advance prior to running an experiment
     *
     * @param n the number to advance
     */
    public final void setAdvanceStreamNumber(int n) {
        myExperiment.setNumberOfStreamAdvancesPriorToRunning(n);
    }

    /**
     * @return true if the flag permits the message to be printed
     */
    public final boolean getRepLengthWarningMessageOption() {
        return myRepLengthWarningMsgOption;
    }

    /**
     * False turns off the message
     *
     * @param flag false turns of the message
     */
    public final void setRepLengthWarningMessageOption(boolean flag) {
        myRepLengthWarningMsgOption = flag;
    }

    /**
     * Sets the name of the underlying experiment
     *
     * @param name the name to set
     */
    public final void setExperimentName(String name) {
        myExperiment.setExperimentName(name);
    }

    /**
     * Set the simulation experiment to the same attribute values as the
     * supplied experiment
     *
     * @param e the experiment
     */
    protected final void setExperiment(Experiment e) {
        myExperiment.setExperiment(e);
    }

    /**
     * Turns on a default tracing report for the Executive to trace event
     * execution to a file
     *
     * @param name the name of the file
     */
    public final void turnOnDefaultEventTraceReport(String name) {
        myExecutive.turnOnDefaultEventTraceReport(name);
    }

    /**
     * Turns on a default tracing report for the Executive to trace event
     * execution to a file
     */
    public final void turnOnDefaultEventTraceReport() {
        myExecutive.turnOnDefaultEventTraceReport();
    }

    /**
     * Turns off a default tracing report for the Executive to trace event
     * execution to a file
     */
    public final void turnOffDefaultEventTraceReport() {
        myExecutive.turnOffDefaultEventTraceReport();
    }

    /**
     * Gets a reference to the default event tracing report. May be null if not
     * turned on.
     *
     * @return the ExecutiveTraceReport
     */
    public final Optional<ExecutiveTraceReport> getDefaultExecutiveTraceReport() {
        return myExecutive.getDefaultExecutiveTraceReport();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Simulation Name: ");
        sb.append(getName());
        sb.append(System.lineSeparator());
        sb.append(myReplicationExecutionProcess);
        sb.append(System.lineSeparator());
        sb.append("Model Name: ");
        sb.append(getModel().getName());
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append(getExperiment());
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append(getExecutive());
        //      sb.append(System.lineSeparator());
        //      sb.append(getHalfWidthSummaryReport().toString());
        return sb.toString();
    }

    /**
     * @return a StringBuilder with the Half-Width Summary Report and 95 percent confidence
     */
    public StringBuilder getHalfWidthSummaryReport() {
        return getHalfWidthSummaryReport(null, 0.95);
    }

    /**
     * @param confLevel the confidence level of the report
     * @return a StringBuilder with the Half-Width Summary Report
     */
    public StringBuilder getHalfWidthSummaryReport(double confLevel) {
        return getHalfWidthSummaryReport(null, confLevel);
    }

    /**
     * @param title     the title
     * @param confLevel the confidence level
     * @return a StringBuilder representation of the half-width summary report
     */
    public StringBuilder getHalfWidthSummaryReport(String title, double confLevel) {
        SimulationReporter reporter = makeSimulationReporter();
        List<StatisticAccessorIfc> list = reporter.getAcrossReplicationStatisticsList();
        StatisticReporter sr = new StatisticReporter(list);
        return sr.getHalfWidthSummaryReport(title, confLevel);
    }

    /**
     * Prints the default half-width summary report to the console
     */
    public void printHalfWidthSummaryReport() {
        writeHalfWidthSummaryReport(new PrintWriter(System.out), null, 0.95);
    }

    /**
     * @param confLevel the confidence level of the report
     */
    public void printHalfWidthSummaryReport(double confLevel) {
        writeHalfWidthSummaryReport(new PrintWriter(System.out), null, confLevel);
    }

    /**
     * @param title     the title of the report
     * @param confLevel the confidence level of the report
     */
    public void printHalfWidthSummaryReport(String title, double confLevel) {
        writeHalfWidthSummaryReport(new PrintWriter(System.out), title, confLevel);
    }

    /**
     * @param out the place to write to
     */
    public void writeHalfWidthSummaryReport(PrintWriter out) {
        writeHalfWidthSummaryReport(out, null, 0.95);
    }

    /**
     * @param out       the place to write to
     * @param confLevel the confidence level of the report
     */
    public void writeHalfWidthSummaryReport(PrintWriter out, double confLevel) {
        writeHalfWidthSummaryReport(out, null, confLevel);
    }

    /**
     * @param out       the place to write to
     * @param title     the title of the report
     * @param confLevel the confidence level of the report
     */
    public void writeHalfWidthSummaryReport(PrintWriter out, String title, double confLevel) {
        if (out == null) {
            throw new IllegalArgumentException("The PrintWriter was null");
        }
        out.print(getHalfWidthSummaryReport(title, confLevel).toString());
        out.flush();
    }

    /**
     * Constructs a SimulationReporter instance that uses
     * this Simulation instance
     *
     * @return the SimulationReporter
     */
    public SimulationReporter makeSimulationReporter() {
        return new SimulationReporter(this);
    }

    /**
     * This method is automatically called at the start of the experiment
     * Sub-classes can inject behavior within here
     */
    protected void beforeExperiment() {
    }

    /**
     * This method is automatically called at the end of the experiment
     * Sub-classes can inject behavior within here
     */
    protected void afterExperiment() {
    }

    /**
     * This method is automatically called before each replication Sub-classes
     * can inject behavior within here
     */
    protected void beforeReplication() {
    }

    /**
     * This method is automatically called after each replication Sub-classes
     * can inject behavior within here
     */
    protected void afterReplication() {
    }

    @Override
    public final boolean isExecutionTimeExceeded() {
        return myReplicationExecutionProcess.isExecutionTimeExceeded();
    }

    @Override
    public final boolean getStoppingFlag() {
        return myReplicationExecutionProcess.getStoppingFlag();
    }

    @Override
    public final void stop() {
        myReplicationExecutionProcess.stop();
    }

    @Override
    public final void stop(String msg) {
        myReplicationExecutionProcess.stop(msg);
    }

    @Override
    public final boolean isRunningStep() {
        return myReplicationExecutionProcess.isRunningStep();
    }

    @Override
    public final boolean noStepsExecuted() {
        return myReplicationExecutionProcess.noStepsExecuted();
    }

    /**
     * This class implements the IterativeProcess behavior for the Simulation
     */
    protected class ReplicationExecutionProcess extends IterativeProcess<Executive> {

        @Override
        protected final void initializeIterations() {
            super.initializeIterations();
            myExecutive.setTerminationWarningMessageOption(false);
            myExperiment.resetCurrentReplicationNumber();
            beforeExperiment();
            myModel.markPreOrderTraversalModelElementHierarchy();
            myModel.setUpExperiment();
            if (getRepLengthWarningMessageOption()) {
                if (Double.isInfinite(myExperiment.getLengthOfReplication())) {
                    if (getMaximumAllowedExecutionTimePerReplication() == 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Simulation: In initializeIterations()\n");
                        sb.append("The experiment has an infinite horizon.\n");
                        sb.append("There was no maximum real-clock execution time specified. \n");
                        sb.append("The user is responsible for ensuring that the Executive is stopped.\n");
                        LOGGER.warn(sb.toString());
                        System.out.flush();
                    }
                }
            }

        }

        @Override
        protected final void endIterations() {
            myModel.afterExperiment(myExperiment);
            afterExperiment();
            super.endIterations();
        }

        @Override
        protected boolean hasNext() {
            return myExperiment.hasMoreReplications();
        }

        @Override
        protected final Executive next() {
            if (!hasNext()) {
                return null;
            }

            return (myExecutive);
        }

        @Override
        protected final void runStep() {
            myCurrentStep = next();
            myExperiment.incrementCurrentReplicationNumber();
            long tpr = getMaximumAllowedExecutionTimePerReplication();
            if (tpr > 0) {
                myExecutive.setMaximumExecutionTime(tpr);
            }
            beforeReplication();
            myExecutive.initialize();
            myModel.setUpReplication();
            myExecutive.executeAllEvents();
            myModel.afterReplication(myExperiment);
            afterReplication();
            if (getGarbageCollectAfterReplicationFlag()) {
                System.gc();
            }
        }
    }
}
