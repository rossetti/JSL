package examples.general.variables;

import jsl.modeling.elements.variable.ResponseVariable;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;

public class TestResponseVariable extends ModelElement {


    private final ResponseVariable myRS;

    public TestResponseVariable(ModelElement parent) {
        this(parent, null);
    }

    public TestResponseVariable(ModelElement parent, String name) {
        super(parent, name);
        myRS = new ResponseVariable(this, "test constants");
        myRS.turnOnTrace(true);
    }

    @Override
    protected void initialize() {
        schedule(this::doTest).in(2.0).units();
    }

    private void doTest(JSLEvent e){
        myRS.setValue(2.0);
        schedule(this::doTest).in(2.0).units();
    }

    public static void main(String[] args) {
        Simulation sim = new Simulation("test RS");
        new TestResponseVariable(sim.getModel());
        SimulationReporter reporter = sim.makeSimulationReporter();
        reporter.turnOnReplicationCSVStatisticReporting();
        sim.setNumberOfReplications(5);
        sim.setLengthOfReplication(25.0);
        sim.run();

        sim.printHalfWidthSummaryReport();
    }
}
