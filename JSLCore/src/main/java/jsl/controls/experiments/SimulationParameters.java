package jsl.controls.experiments;

/**
 * Defines simulation parameters for simulation runs.  Note that all
 * fields (other than the firstReplication and useAntithetic) are nullable
 * so the model (or JSL) must deal with defaults as needed.  The main purpose
 * of this class is to facilitate data transfer of simulation run parameters.
 */
public class SimulationParameters {
    // simple public fields for effective JSON data transfer
    public Double lengthOfReplication = null;
    public Double lengthOfWarmup = null;
    public Integer numberOfReplications = null;
    // firstReplication MUST be defined (as it's not held anywhere else)
    // and defaults to 0 (the first replication)
    public int firstReplication = 0;
    public boolean useAntithetic = false;

    /**
     * required no parameter constructor for JSON I/O
     */
    public SimulationParameters() {
    }

    /**
     * full constructor
     *
     * @param firstReplication
     */
    public SimulationParameters(
            int firstReplication,
            Double lengthOfReplication,
            Double lengthOfWarmup,
            Integer numberOfReplications,
            boolean useAntithetic) {
        this.firstReplication = firstReplication;
        this.lengthOfReplication = lengthOfReplication;
        this.lengthOfWarmup = lengthOfWarmup;
        this.numberOfReplications = numberOfReplications;
        this.useAntithetic = useAntithetic;
    }

    public SimulationParameters newInstance() {
        return new SimulationParameters(
                this.firstReplication,
                this.lengthOfReplication,
                this.lengthOfWarmup,
                this.numberOfReplications,
                this.useAntithetic
        );
    }

    public void setDefaults(SimulationParameters defaultParams) {
        if (defaultParams != null) {
            if (this.lengthOfReplication == null) this.lengthOfReplication = defaultParams.lengthOfReplication;
            if (this.lengthOfWarmup == null) this.lengthOfWarmup = defaultParams.lengthOfWarmup;
            if (this.numberOfReplications == null) this.numberOfReplications = defaultParams.numberOfReplications;
        }
    }

    public Integer lastReplication() {
        return firstReplication + numberOfReplications - 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimulationParameters)) return false;

        SimulationParameters that = (SimulationParameters) o;

        if (firstReplication != that.firstReplication) return false;
        if (useAntithetic != that.useAntithetic) return false;
        if (lengthOfReplication != null ? !lengthOfReplication.equals(that.lengthOfReplication) : that.lengthOfReplication != null)
            return false;
        if (lengthOfWarmup != null ? !lengthOfWarmup.equals(that.lengthOfWarmup) : that.lengthOfWarmup != null)
            return false;
        return numberOfReplications != null ? numberOfReplications.equals(that.numberOfReplications) : that.numberOfReplications == null;
    }

    @Override
    public int hashCode() {
        int result = lengthOfReplication != null ? lengthOfReplication.hashCode() : 0;
        result = 31 * result + (lengthOfWarmup != null ? lengthOfWarmup.hashCode() : 0);
        result = 31 * result + (numberOfReplications != null ? numberOfReplications.hashCode() : 0);
        result = 31 * result + firstReplication;
        result = 31 * result + (useAntithetic ? 1 : 0);
        return result;
    }

    /**
     * builder class for parameters
     */
    public static class Builder {
        private SimulationParameters parameters = new SimulationParameters();

        public Builder withFirstReplication(Integer i) {
            if (i != null) parameters.firstReplication = i;
            return this;
        }

        public Builder withNumberOfReplications(int i) {
            parameters.numberOfReplications = i;
            return this;
        }

        public Builder withLengthOfReplication(Double l) {
            parameters.lengthOfReplication = l;
            return this;
        }

        public Builder withLengthOfWarmup(Double l) {
            parameters.lengthOfWarmup = l;
            return this;
        }

        public Builder withAntitheticOption(Boolean useAntithetic) {
            parameters.useAntithetic = useAntithetic;
            return this;
        }

        public SimulationParameters create() {
            return parameters;
        }
    }
}
