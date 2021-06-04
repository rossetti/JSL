package jslx.dbutilities;

import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.observers.ModelElementObserver;
import jsl.utilities.reporting.JSL;
import org.jooq.exception.DataAccessException;

import java.util.Objects;

/**
 * The purpose of this class is to facilitate the observation of a simulation model's
 * before experiment, after replication, and after experiment actions so that
 * the supplied JSLDatabase may collect data into the database based on the simulation run.
 * <p>
 * The user has the option to have the collected data associated with the supplied simulation
 * cleared prior to each executed experiment of the simulation. Any other data in the database
 * from other simulation experiments will not be cleared.
 * <p>
 * The default is to clear the data from the simulation's previous experiment that has the same
 * experiment name.  Thus, if you re-run the simulation w/o changing the simulation experiment's
 * name, the data will be cleared and re-collected.
 */
public class JSLDatabaseObserver {

    private final JSLDatabase myDb;
    private final Simulation mySim;
    private final SimulationDatabaseObserver myObserver;
    private boolean myClearDataBeforeExperimentOption;

    /**
     * By default collected data will be cleared before each experiment with the same name.
     *
     * @param db  the database to collect data into, must not be null
     * @param sim the simulation to collect data from, must not be null
     */
    public JSLDatabaseObserver(JSLDatabase db, Simulation sim) {
        this(db, sim, true);
    }

    /**
     * @param db                              the database to collect data into, must not be null
     * @param sim                             the simulation to collect data from, must not be null
     * @param clearDataBeforeExperimentOption true indicates that data collected from previous experiments
     *                                        will be cleared before a new experiment executes
     */
    public JSLDatabaseObserver(JSLDatabase db, Simulation sim, boolean clearDataBeforeExperimentOption) {
        Objects.requireNonNull(db, "The JSL database cannot be null");
        Objects.requireNonNull(sim, "The JSL simulation cannot be null");
        myClearDataBeforeExperimentOption = clearDataBeforeExperimentOption;
        myDb = db;
        mySim = sim;
        myObserver = new SimulationDatabaseObserver();
        mySim.getModel().addObserver(myObserver);
    }

    /**
     * Creates a JSLDatabaseObserver with a JSLDatabase that is named after the simulation, as JSLDb_nameOfSimulation
     * The database will be cleared before new experiments execute
     *
     * @param sim the simulation to collect data from, must not be null
     * @return the created JSLDatabaseObserver
     */
    public static JSLDatabaseObserver createJSLDatabaseObserver(Simulation sim) {
        return createJSLDatabaseObserver(null, sim, true);
    }

    /**
     * Creates a JSLDatabaseObserver with a JSLDatabase that is named after the simulation, as JSLDb_nameOfSimulation
     *
     * @param sim                             the simulation to collect data from, must not be null
     * @param clearDataBeforeExperimentOption true indicates that data collected from previous experiments
     *                                        will be cleared before a new experiment executes
     * @return the created JSLDatabaseObserver
     */
    public static JSLDatabaseObserver createJSLDatabaseObserver(Simulation sim, boolean clearDataBeforeExperimentOption) {
        return createJSLDatabaseObserver(null, sim, clearDataBeforeExperimentOption);
    }

    /**
     * Creates a JSLDatabaseObserver with a JSLDatabase that is named after the simulation. The database will
     * be cleared before new experiments execute
     *
     * @param dbName the name of the JSLDatabase, if null the name is derived from the simulation name as JSLDb_nameOfSimulation
     * @param sim    the simulation to collect data from, must not be null
     * @return the created JSLDatabaseObserver
     */
    public static JSLDatabaseObserver createJSLDatabaseObserver(String dbName, Simulation sim) {
        return createJSLDatabaseObserver(dbName, sim, true);
    }

    /**
     * Creates a JSLDatabaseObserver with a JSLDatabase
     *
     * @param dbName                          the name of the JSLDatabase, if null the name is derived from the simulation name as JSLDb_nameOfSimulation
     * @param sim                             the simulation to collect data from, must not be null
     * @param clearDataBeforeExperimentOption true indicates that data collected from previous experiments
     *                                        will be cleared before a new experiment executes
     * @return the created JSLDatabaseObserver
     */
    public static JSLDatabaseObserver createJSLDatabaseObserver(String dbName, Simulation sim, boolean clearDataBeforeExperimentOption) {
        Objects.requireNonNull(sim, "The JSL simulation cannot be null");
        if (dbName == null) {
            // use the simulation name
            String name = sim.getName().replaceAll("\\s+", "");
            dbName = name + "_JSLDb";
        }
        JSLDatabase db = JSLDatabase.createEmbeddedDerbyJSLDatabase(dbName, sim.getOutputDirectory().getOutDir());
        return new JSLDatabaseObserver(db, sim, true);
    }

    /**
     * @return the underlying JSLDatabase
     */
    public final JSLDatabase getJSLDatabase() {
        return myDb;
    }

    /**
     * @return the underlying simulation being observed.
     */
    public final Simulation getSimulation() {
        return mySim;
    }

    /**
     * @return true indicates that data collected from previous experiments with the same name
     * will be cleared before a new experiment with the same name executes
     */
    public final boolean getClearDataBeforeExperimentOption() {
        return myClearDataBeforeExperimentOption;
    }

    /**
     * @param clearDataBeforeExperimentOption true indicates that data collected from previous experiments
     *                                        will be cleared before a new experiment executes
     */
    public final void setClearDataBeforeExperimentOption(boolean clearDataBeforeExperimentOption) {
        myClearDataBeforeExperimentOption = clearDataBeforeExperimentOption;
    }

    /**
     * Tells the observer to stop observing the Simulation Model
     */
    public final void stopObserving() {
        mySim.getModel().deleteObserver(myObserver);
    }

    /**
     * If the observer is not already observing the simulation model, then
     * it will start observing
     */
    public final void startObserving() {
        if (!mySim.getModel().contains(myObserver)) {
            mySim.getModel().addObserver(myObserver);
        }
    }

    protected class SimulationDatabaseObserver extends ModelElementObserver {

        @Override
        protected void beforeExperiment(ModelElement m, Object arg) {
            super.beforeExperiment(m, arg);
            //handle clearing of database here
            if (getClearDataBeforeExperimentOption()) {
                myDb.clearSimulationData(mySim);
            } else {
                // no clear option, need to check if simulation record exists
                String simName = mySim.getName();
                String expName = mySim.getExperimentName();
                if (myDb.simulationRunRecordExists(simName, expName)) {
                    JSL.getInstance().LOGGER.error("A simulation run record exists for simulation: {}, and experiment: {} in database {}",
                            simName, expName, myDb.getDatabase().getLabel());
                    JSL.getInstance().LOGGER.error("You attempted to run a simulation for a run that has ");
                    JSL.getInstance().LOGGER.error(" the same name and experiment without allowing its data to be cleared.");
                    JSL.getInstance().LOGGER.error("You should consider using setClearDataBeforeExperimentOption() on the observer.");
                    JSL.getInstance().LOGGER.error("Or, you might change the name of the experiment before calling simulation.run().");
                    JSL.getInstance().LOGGER.error("This error is to prevent you from accidentally losing data associated with simulation: {}, and experiment: {} in database {}",
                            simName, expName, myDb.getDatabase().getLabel());
                    throw new DataAccessException("A simulation run record already exists with the name " + simName + " and experiment name " + expName);
                }
            }
            myDb.beforeExperiment(mySim);
        }

        @Override
        protected void afterReplication(ModelElement m, Object arg) {
            super.afterReplication(m, arg);
            myDb.afterReplication(mySim);
        }

        @Override
        protected void afterExperiment(ModelElement m, Object arg) {
            super.afterExperiment(m, arg);
            myDb.afterExperiment(mySim);
        }
    }
}
