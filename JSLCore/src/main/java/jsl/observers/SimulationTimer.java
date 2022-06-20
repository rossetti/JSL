package jsl.observers;

import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.utilities.statistic.ArraySaver;
import jsl.utilities.statistic.Statistic;

import java.util.Objects;

public class SimulationTimer {
    private final ArraySaver myRepTimeSaver;
    private final Simulation mySim;

    private double myTotalElapsedTime;
    private double mySimStartTime;
    private double myRepStartTime;

    private final SimObserver myObserver;

    /**
     * Automatically starts the time collection, w/o calling startTiming()
     *
     * @param sim the simulation to time
     */
    public SimulationTimer(Simulation sim) {
        Objects.requireNonNull(sim, "The simulation must not be null");
        mySim = sim;
        myRepTimeSaver = new ArraySaver();
        myTotalElapsedTime = Double.NaN;
        mySimStartTime = Double.NaN;
        myRepStartTime = Double.NaN;
        myObserver = new SimObserver();
        startTiming();
    }

    /**
     * Ensures that the simulation execution time is collected by
     * this object, if it has been started
     */
    public void startTiming() {
        if (!mySim.getModel().contains(myObserver)) {
            mySim.getModel().addObserver(myObserver);
        }
    }

    /**
     * Stop collecting execution time
     */
    public void stopTiming() {
        if (mySim.getModel().contains(myObserver)) {
            mySim.getModel().deleteObserver(myObserver);
        }
    }

    private class SimObserver extends ModelElementObserver {
        @Override
        protected void beforeExperiment(ModelElement m, Object arg) {
            myRepTimeSaver.clearSavedData();
            myTotalElapsedTime = Double.NaN;
            myRepStartTime = Double.NaN;
            mySimStartTime = (double) System.nanoTime();
        }

        @Override
        protected void beforeReplication(ModelElement m, Object arg) {
            myRepStartTime = (double) System.nanoTime();
        }

        @Override
        protected void afterReplication(ModelElement m, Object arg) {
            myRepTimeSaver.save(((double) System.nanoTime() - myRepStartTime) / 1000000.0);
        }

        @Override
        protected void afterExperiment(ModelElement m, Object arg) {
            myTotalElapsedTime = (double) System.nanoTime() - mySimStartTime;
        }
    }

    /**
     * @return the elapsed time in milliseconds
     */
    public double getElapsedTime() {
        return myTotalElapsedTime / 1000000.0;
    }

    /**
     * The time in milliseconds for each executed replication within the experiment
     *
     * @return an array of times
     */
    public double[] getReplicationTimes() {
        return myRepTimeSaver.getSavedData();
    }

    /**
     * @return the statistics for the observed replication timing in milliseconds
     */
    public Statistic getReplicationTimeStatistics() {
        return new Statistic("Replication Time Statistics (milliseconds)", getReplicationTimes());
    }
}
