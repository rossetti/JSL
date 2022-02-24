package examples.book.chapter4;

import jsl.simulation.State;
import jsl.utilities.random.rvariable.JSLRandom;
import jsl.utilities.statistic.StateFrequency;

import java.util.List;

/**
 * This example illustrates how to define labeled states
 * and to tabulate observations of those states. The StateFrequency
 * class generalizes the IntegerFrequency class by allowing the user
 * to collect observations on labeled states rather than integers.
 * This also allows for the tabulation of counts and proportions of single
 * step transitions between states.
 */
public class Example4 {
    public static void main(String[] args) {
        StateFrequency sf = new StateFrequency(6);
        List<State> states = sf.getStates();

        for (int i = 1; i <= 10000; i++) {
            State state = JSLRandom.randomlySelect(states);
            sf.collect(state);
        }
        System.out.println(sf);
    }
}
