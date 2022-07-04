package jsl.controls.experiments;

import jsl.observers.ReplicationDataCollector;
import jsl.observers.SimulationTimer;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.utilities.random.rvariable.RVParameterSetter;
import jsl.utilities.random.rvariable.RVParameters;
import jsl.utilities.reporting.JSL;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.IntStream;

/**
 * A SimulationRunner is used to:
 * - extract annotation controls from a Model
 * - For an SimulationRun
 * - set values for those controls based on a map of control-names and values
 * - set simulation controls (length,warmup,stream etc.)
 * - run the simulation and gather responses by replication
 * - add response data into the SimulationRun objects
 * <p>
 * This could be called in sequence OR in parallel from any other routine.
 * It can be called locally or as part of the Serverless routines.
 */
public class SimulationRunner {

    private final Simulation mySim;
    private final Model myModel;

    private SimulationRun simulationRun = new SimulationRun();

    public SimulationRunner(Simulation sim) {
        Objects.requireNonNull(sim, "The supplied simulation must not be null");
        mySim = sim;
        myModel = sim.getModel();
        // make sure the model random numbers are reset to starting positions
        //TODO why immediately, why not mySim.setResetStartStreamOption(true);
        myModel.resetStartStream();
    }

    /**
     * Sets up the simulation to run with the experimental parameters
     * and controls
     */
    protected void setupSimulation() {
        //try to use simulationRun.parameters to set the simulation
        //need to worry about null and improper values, see updateParameters() in Andrew's code
        SimulationParameters p = simulationRun.parameters;
        if (p != null) {
            // there are parameters to use, need to check if they were set, if so, use them
            if (p.lengthOfReplication != null) {
                mySim.setLengthOfReplication(p.lengthOfReplication);
            }
            if (p.lengthOfWarmup != null) {
                mySim.setLengthOfWarmUp(p.lengthOfWarmup);
            }
            if (p.numberOfReplications != null) {
                mySim.setNumberOfReplications(p.numberOfReplications, p.useAntithetic);
            }
            if (simulationRun.name != null) {
                mySim.setExperimentName(simulationRun.name);
            }
        } else {
            // no supplied parameters, remember what was specified for the simulation by the user
            simulationRun.parameters = new SimulationParameters();
            simulationRun.parameters.lengthOfReplication = mySim.getLengthOfReplication();
            simulationRun.parameters.lengthOfWarmup = mySim.getLengthOfWarmUp();
            simulationRun.parameters.numberOfReplications = mySim.getNumberOfReplications();
            simulationRun.parameters.useAntithetic = mySim.getAntitheticOption();
            simulationRun.name = mySim.getExperimentName();
            //TODO simulationRun.parameters.firstReplication = ??
        }

        //set up the inputs for the run
        if (simulationRun.inputs != null) {
            //TODO some inputs could be controls, some could be random variable parameters
            // get the controls

            // get the random variables
            mySim.useControls(simulationRun.inputs);

//            if (simulationRun.rvParameters != null) {
//                RVParameterSetter setter = mySim.getRVParameterSetter();
//                setter.changeParameters(simulationRun.rvParameters);
//            }

        }

    }

//    public final Map<String, Double> getRVParametersAsDoubles(String catChar){
//        Objects.requireNonNull(catChar, "The concatenation string must not be null");
//        LinkedHashMap<String,Double> theMap = new LinkedHashMap<>();
//        RVParameterSetter setter = new RVParameterSetter();
//        Map<String, RVParameters> params = setter.extractParameters(this);
//        //TODO process the parameters
//
//        return theMap;
//    }

    public SimulationRun run() {
        try {
            // grab the current timestamp
            simulationRun.handlerStartedNs = System.nanoTime();

            // make use of a ReplicationDataCollector class to grab all
            // response variables at the end of each run.
            ReplicationDataCollector rdc = new ReplicationDataCollector(myModel, true);
            SimulationTimer timer = new SimulationTimer(mySim);

            // reset streams to their start for all RandomIfc elements in the model
            // and skip ahead to the right replication (advancing sub-streams)
            myModel.resetStartStream();
            if (simulationRun.parameters.firstReplication > 0) {
                myModel.advanceSubstreams(simulationRun.parameters.firstReplication);
            }

            // set simulation run parameters and controls
            setupSimulation();
            // run the simulation
            mySim.run();

            // calculate replications
            int s = simulationRun.parameters.firstReplication;
            int n = mySim.getNumberOfReplications();
            double[] reps = IntStream.range(s, n + s)
                    .mapToDouble(x -> (double) x).toArray();

            LinkedHashMap<String, double[]> results = new LinkedHashMap<>();
            results.put("replication", reps);

            // add elapsed times
            results.put("elapsedNs", timer.getReplicationTimes());
            // add everything else
            Map<String, double[]> map = rdc.getAllReplicationDataAsMap();
            results.putAll(map);
            // update the simulation run results
            simulationRun.responseData = results;
            // capture execution end time for the experiment
            simulationRun.handlerEndedNs = System.nanoTime();
        } catch (Exception e) {
            // capture the full stack trace
            // per https://www.baeldung.com/java-stacktrace-to-string
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            simulationRun.functionError = sw.toString();
            // return an empty HashMap of results
            simulationRun.responseData = new LinkedHashMap<>();
            JSL.getInstance().LOGGER.error("There was a fatal exception during the running of simulation {} within SimulationRunner.",
                    mySim.getName());
            JSL.getInstance().LOGGER.error("No responses were recorded.");
            JSL.getInstance().LOGGER.error(sw.toString());
        } finally {
            // return the simulationRun (for chaining purposes)
            return simulationRun;
        }

    }
}
