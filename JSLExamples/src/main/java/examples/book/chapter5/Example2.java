package examples.book.chapter5;

import jsl.utilities.random.rvariable.DUniformRV;
import jsl.utilities.reporting.StatisticReporter;
import jsl.utilities.statistic.Statistic;

/**
 *  This example illustrates how to simulate the dice game "craps".
 *  The example uses discrete random variables, statistics, and logic
 *  to replicate the game outcomes. Statistics on the probability
 *  of winning are reported.
 */
public class Example2 {
    public static void main(String[] args) {
        DUniformRV d1 = new DUniformRV(1, 6);
        DUniformRV d2 = new DUniformRV(1, 6);
        Statistic probOfWinning = new Statistic("Prob of winning");
        Statistic numTosses = new Statistic("Number of Toss Statistics");
        int numGames = 5000;
        for (int k = 1; k <= numGames; k++) {
            boolean winner = false;
            int point = (int) d1.getValue() + (int) d2.getValue();
            int numberoftoss = 1;

            if (point == 7 || point == 11) {
                // automatic winner
                winner = true;
            } else if (point == 2 || point == 3 || point == 12) {
                // automatic loser
                winner = false;
            } else { // now must roll to get point
                boolean continueRolling = true;
                while (continueRolling == true) {
                    // increment number of tosses
                    numberoftoss++;
                    // make next roll
                    int nextRoll = (int) d1.getValue() + (int) d2.getValue();
                    if (nextRoll == point) {
                        // hit the point, stop rolling
                        winner = true;
                        continueRolling = false;
                    } else if (nextRoll == 7) {
                        // crapped out, stop rolling
                        winner = false;
                        continueRolling = false;
                    }
                }
            }
            probOfWinning.collect(winner);
            numTosses.collect(numberoftoss);
        }
        StatisticReporter reporter = new StatisticReporter();
        reporter.addStatistic(probOfWinning);
        reporter.addStatistic(numTosses);
        System.out.println(reporter.getHalfWidthSummaryReport());
    }
}
