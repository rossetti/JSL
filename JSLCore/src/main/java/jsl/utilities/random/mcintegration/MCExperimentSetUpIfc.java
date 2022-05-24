package jsl.utilities.random.mcintegration;

public interface MCExperimentSetUpIfc {
    /**
     * @param level the desired confidence level
     */
    void setConfidenceLevel(double level);

    /**
     * @return the initial sample size for the experiment
     */
    int getInitialSampleSize();

    /**
     * @param initialSampleSize the intial sample size for pilot simulation
     */
    void setInitialSampleSize(int initialSampleSize);

    /**
     * @return the maximum number of samples permitted
     */
    long getMaxSampleSize();

    /**
     * @param maxSampleSize the maximum number of samples permitted
     */
    void setMaxSampleSize(int maxSampleSize);

    /**
     * @return the desired half-width bound for the experiment
     */
    double getDesiredHWErrorBound();

    /**
     * @param desiredHWErrorBound the desired half-width error bound for the experiment
     */
    void setDesiredHWErrorBound(double desiredHWErrorBound);

    /**
     * @return true if the reset stream option is on
     */
    boolean isResetStreamOptionOn();

    /**
     * @param resetStreamOptionOn determines whether the reset stream option is on (true) or off (fals)
     */
    void setResetStreamOption(boolean resetStreamOptionOn);

    /**
     * @return the number of micro replications to perform
     */
    int getMicroRepSampleSize();

    /**
     * @param microRepSampleSize the number of micro replications to perform
     */
    void setMicroRepSampleSize(int microRepSampleSize);
}
