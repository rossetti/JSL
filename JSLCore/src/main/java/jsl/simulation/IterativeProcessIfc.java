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

import java.nio.file.Path;
import java.util.TimerTask;

import jsl.observers.textfile.IPLogReport;
import jsl.utilities.GetNameIfc;
import jsl.utilities.IdentityIfc;

/**
 *
 * @author rossetti
 */
public interface IterativeProcessIfc extends GetNameIfc {

    /**
     * A flag to indicate whether the iterative process is done A iterative
     * process can be done if: 1) it ran all of its steps 2) it was ended by a
     * client prior to completing all of its steps 3) it ended because it
     * exceeded its maximum allowable execution time before completing all of
     * its steps. 4) its end condition was satisfied
     *
     * @return true if done
     */
    boolean isDone();

    /**
     * Returns if the elapsed execution time exceeds the maximum time allowed.
     * Only true if the maximum was set and elapsed time is greater than or
     * equal to getMaximumAllowedExecutionTime()
     *
     * @return true if the execution time exceeds
     * getMaximumAllowedExecutionTime()
     */
    boolean isExecutionTimeExceeded();

    /**
     * Returns system time in milliseconds that the iterative process started
     *
     * @return the number as a long
     */
    long getBeginExecutionTime();

    /**
     * Gets the clock time in milliseconds since the iterative process was
     * initialized
     *
     * @return a long representing the elapsed time
     */
    long getElapsedExecutionTime();

    /**
     * Returns system time in milliseconds that the iterative process ended
     *
     * @return the number as a long
     */
    long getEndExecutionTime();

    /**
     * Returns maximum (real) clock time allocated for the iterative process
     *
     * @return the number as long representing milliseconds
     */
    long getMaximumAllowedExecutionTime();

    /**
     * Set the maximum allotted (suggested) execution (real) clock for the
     * entire iterative process. This is a suggested time because the execution
     * time requirement is only checked after the completion of an individual
     * step After it is discovered that cumulative time for executing the step
     * has exceeded the maximum time, then the iterative process will be ended
     * (perhaps) not completing other steps.
     *
     * @param milliseconds the time
     */
    void setMaximumExecutionTime(long milliseconds);

    /**
     * Returns the number of steps completed since the iterative process was
     * last initialized
     *
     * @return the number of steps completed
     */
    long getNumberStepsCompleted();

    /**
     * Turns on the log report. This report yields a text file of the state
     * changes for the iterative process
     *
     * @param pathToFile specifies a prefix name for the report
     */
    void turnOnLogReport(Path pathToFile);

    /**
     * Turns off log reporting.
     */
    void turnOffLogReport();

    /**
     * Returns a reference to the current log report.
     *
     * @return The log report
     */
    IPLogReport getLogReport();

    /**
     * This method will cause the a timer to start allowing a TimerTask to be
     * scheduled. This method causes a print to console task to be run which
     * prints the elapsed time since initializing the iterative process.
     *
     * @param milliseconds the time for the timer
     */
    void turnOnTimer(long milliseconds);

    /**
     * Checks if the iterative process is in the created state. If the
     * iterative process is in the created state this method will return true
     *
     * @return true if in the created state
     */
    boolean isCreated();

    /**
     * Checks if the iterative process is in the initialized state After the
     * iterative process has been initialized this method will return true
     *
     * @return true if initialized
     */
    boolean isInitialized();

    /**
     * An iterative process is running if it is been told to run (i.e.
     * runNext()) but has not yet been told to end().
     *
     * @return true if running
     */
    boolean isRunning();

    /**
     * Checks if the iterative process is in the completed step state After the
     * iterative process has successfully completed a step this method will
     * return true
     *
     * @return true if the iterative process completed the step
     */
    boolean isStepCompleted();

    /**
     * Checks if the iterative process is in the ended state After the iterative
     * process has been ended this method will return true
     *
     * @return true if ended
     */
    boolean isEnded();

    /**
     * The iterative process may end by a variety of means, this method checks
     * if the iterative process ended because it ran all of its steps
     *
     *
     * @return true if all completed
     */
    boolean allStepsCompleted();

    /**
     * The iterative process may end by a variety of means, this method checks
     * if the iterative process ended because it timed out
     *
     *
     * @return true if exceeded
     */
    boolean executionTimeExceeded();

    /**
     * The iterative process may end by a variety of means, this method checks
     * if the iterative process ended because it was stopped
     *
     *
     * @return true if it was stopped via stop()
     */
    boolean stoppedByCondition();

    /**
     * The iterative process may end by a variety of means, this method checks
     * if the iterative process ended but was unfinished, not all steps
     * completed
     *
     *
     * @return true if the process is not finished
     */
    boolean isUnfinished();

    /**
     * Initializes the iterative process prior to running any steps This must be
     * done prior to calling runNext();
     */
    void initialize();

    /**
     * Runs the next step in the iterative process
     */
    void runNext();

    /**
     * Runs all of the steps of the iterative process.
     *
     * If the iterative process has not been initialized, then it will
     * automatically be initialized.
     *
     * After attempting to run the steps, the process will be in the end()
     * state. The process may or may not complete all of its steps.
     *
     */
    void run();

    /**
     * The iterative process will continue until there are no more steps or its
     * maximum execution time has been reached, whichever comes first. If this
     * method is called the iterative process will stop processing (terminate)
     * before the next step and not process the next step in the process. The
     * current step will be completed. This method can be used to stop the
     * process at an arbitrary step. Once stopped, the process must be
     * restarted.
     *
     */
    void end();

    /**
     * The iterative process will continue until there are no more steps or its
     * maximum execution time has been reached, whichever comes first. If this
     * method is called the iterative process will stop processing (terminate)
     * before the next step and not process the next step in the process. The
     * current step will be completed. This method can be used to stop the
     * process at an arbitrary step. Once stopped, the process must be
     * restarted.
     *
     * @param msg an option message to indicate the reason for stopping
     */
    void end(String msg);

    /**
     * A string message for why stop() was called.
     *
     * @return the message
     */
    String getStoppingMessage();

    /**
     * Returns the stopping flag
     *
     * @return true if the process has been told to stop via stop()
     */
    boolean getStoppingFlag();

    /**
     * This sets a flag to indicate to the process that is should stop after the
     * next step is completed. This is different than end(). Calling end()
     * immediately places the process in the End state. The process needs to be
     * in a valid state before end() can be used. Calling stop tells the process
     * to eventually get into the end state. stop() can be used to arbitrarily
     * stop the process based on some user defined condition.
     */
    void stop();

    /**
     * This sets a flag to indicate to the process that is should stop after the
     * next step is completed. This is different than end(). Calling end()
     * immediately places the process in the End state. The process needs to be
     * in a valid state before end() can be used. Calling stop tells the process
     * to eventually get into the end state. stop() can be used to arbitrarily
     * stop the process based on some user defined condition.
     *
     * @param msg A string to represent the reason for the stopping
     */
    void stop(String msg);

    /**
     * Indicates that the iterative process is currently running an individual
     * step
     *
     * @return true if the step is in progress
     */
    boolean isRunningStep();

    /**
     * Indicates that the iterative process ended because of no steps
     *
     * @return True if no steps are executed
     */
    boolean noStepsExecuted();
}
