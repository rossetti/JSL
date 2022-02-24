package examples.book.chapter6;

import jsl.simulation.Simulation;

/**
 *  This example illustrates how to create a simulation,
 *  attach a new model element, set the run length, and
 *  run the simulation. The example use the SchedulingEventExamples
 *  class to show how actions are used to implement events.
 */
public class Example1 {
    public static void main(String[] args) {
        Simulation s = new Simulation("Scheduling Example");
        new SchedulingEventExamples(s.getModel());
        s.setLengthOfReplication(100.0);
        s.run();
    }
}
