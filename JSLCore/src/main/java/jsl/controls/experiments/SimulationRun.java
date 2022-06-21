package jsl.controls.experiments;

import jsl.utilities.JSLArrayUtil;

import java.util.Map;
import java.util.UUID;

/**
 * A SimulationRun represents the execution of a simulation with inputs (controls and parameters),
 * and output (results).  A run consists of a number of replications that were executed with the
 * same inputs and parameters, which cause the creation of results for each response within
 * each replication. The main purpose of SimulationRun is to transfer data about the execution
 * of a simulation. It acts as a data transfer class.
 */
public class SimulationRun {

    // id could be set up with a public setter/getter and a private attribute
    // but as id is really a way for the calling function to identify a job
    // and nothing internal to SimulationRun uses it, this does no harm and
    // keeps id at the top of the JSON encoded string which is beneficial
    // aesthetically.  (Public getters get encoded later in the string )
    public String id = null;

    // status information
    public String functionError = "";

    public SimulationParameters parameters = null;

    public Map<String, Double> controls = null;

    // attribute to hold elapsed ms for each experiment
    // having this null means that JSON encoding does not
    // include it until populated
    public Long handlerStartedMs = null;

    public Long handlerEndedMs = null;

    //List<Double> may be necessary/useful if other replication data needs to be added
    public Map<String, double[]> responseData = null;

    /**
     * required no parameter constructor for JSON I/O
     */
    public SimulationRun() {
    }

    /**
     * Extract a new SimulationRun for some sub-list of replications
     *
     * @param fromReplicationInclusive - integer
     * @param toReplicationExclusive   - integer
     * @return the created simulation run
     */
    public SimulationRun subTask(int fromReplicationInclusive, int toReplicationExclusive) {
        if (fromReplicationInclusive < parameters.firstReplication)
            throw new IllegalArgumentException("index error fromReplicationInclusive < firstReplication");
        if (toReplicationExclusive > (parameters.lastReplication() + 1))
            throw new IllegalArgumentException("index error toReplicationExclusive > (lastReplication + 1)");

        // define the new parameters
        SimulationParameters p = parameters.newInstance();
        p.numberOfReplications = toReplicationExclusive - fromReplicationInclusive;
        p.firstReplication = fromReplicationInclusive;

        return new SimulationRun.Builder()
                .withParameters(p)
                .withControls(controls)
                .withID(id)
                .create();
    }

    public static class Builder {
        private SimulationRun simulationRun;

        public Builder() {
            simulationRun = new SimulationRun();
            simulationRun.id = UUID.randomUUID().toString();
        }

        public Builder withID(String id) {
            simulationRun.id = id;
            return this;
        }

        public Builder withParameters(SimulationParameters parameters) {
            simulationRun.parameters = parameters;
            return this;
        }


        public Builder withControl(String nm, double value) {
            simulationRun.controls.put(nm, value);
            return this;
        }

        public Builder withControls(Map<String, Double> controls) {
            simulationRun.controls = controls;
            return this;
        }

        public Builder withControls(String[] nms, double[] values) {
            withControls(JSLArrayUtil.makeMap(nms, values));
            return this;
        }

        public SimulationRun create() {
            return simulationRun;
        }
    }
}
