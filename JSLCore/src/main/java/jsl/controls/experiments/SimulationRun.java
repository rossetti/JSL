package jsl.controls.experiments;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.Statistic;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A SimulationRun represents the execution of a simulation with inputs (controls and parameters),
 * and output (results).  A run consists of a number of replications that were executed with the
 * same inputs and parameters, which cause the creation of results for each response within
 * each replication. The main purpose of SimulationRun is to transfer data about the execution
 * of a simulation. It acts as a data transfer class.
 */
public class SimulationRun {

    /**
     * id could be set up with a public setter/getter and a private attribute
     * but as id is really a way for the calling function to identify a job
     * and nothing internal to SimulationRun uses it, this does no harm and
     * keeps id at the top of the JSON encoded string which is beneficial
     * aesthetically.  (Public getters get encoded later in the string )
     */
    public String id = null;

    /**
     *  the name of the simulation run/experiment
     */
    public String name = null;

    /**
     * to capture status information
     */
    public String functionError = "";

    //TODO why are these fields null?  Why not a default instance? To prevent JSON creation? Why?
    // solution seems to be to initialize fields to default values within the default constructor
    // initialization to values other than null is problematic
    // https://stackoverflow.com/questions/32510803/how-do-i-retain-the-default-values-of-field-in-a-deserialized-object
    /**
     * the simulation run parameters
     */
    public SimulationParameters parameters = null;

    /**
     * The controls as (String, Double) pairs
     */
    public Map<String, Double> controls = null;

    /**
     * Time in nanoseconds handler started
     */
    public Long handlerStartedNs = null;

    /**
     * Time in nanoseconds handler ended
     */
    public Long handlerEndedNs = null;

    //List<Double> may be necessary/useful if other replication data needs to be added
    /**
     * the replication results as a double array for each response
     */
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
     * @param toReplicationExclusive   - integer, warning notice exclusive
     * @return the created simulation run
     */
    public SimulationRun subTask(int fromReplicationInclusive, int toReplicationExclusive) {
        if (fromReplicationInclusive < parameters.firstReplication)
            throw new IllegalArgumentException("index error fromReplicationInclusive < firstReplication");
        if (toReplicationExclusive > (parameters.lastReplication() + 1))
            throw new IllegalArgumentException("index error toReplicationExclusive > (lastReplication + 1)");
//TODO notice toReplicationExclusive
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
        private final SimulationRun simulationRun;

        public Builder() {
            simulationRun = new SimulationRun();
            simulationRun.id = UUID.randomUUID().toString();
        }

        public Builder withID(String id) {
            if (id == null){
                simulationRun.id = UUID.randomUUID().toString();
            } else {
                simulationRun.id = id;
            }
            return this;
        }

        public Builder withParameters(SimulationParameters parameters) {
            Objects.requireNonNull(parameters, "The supplied parameters were null");
            simulationRun.parameters = parameters;
            return this;
        }


        public Builder withControl(String nm, double value) {
            Objects.requireNonNull(nm, "The supplied control name was null");
            simulationRun.controls.put(nm, value);
            return this;
        }

        public Builder withControls(Map<String, Double> controls) {
            Objects.requireNonNull(controls, "The supplied control map was null");
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

    /** Use primarily for printing out run results
     *
     * @return a StatisticReporter with the summary statistics of the run
     */
    public StatisticReporter getStatisticalReporter() {
        StatisticReporter r = new StatisticReporter();
        if (responseData != null) {
            for (Map.Entry<String, double[]> entry : responseData.entrySet()) {
                Statistic s = new Statistic(entry.getKey(), entry.getValue());
                r.addStatistic(s);
            }
        }
        return r;
    }
}
