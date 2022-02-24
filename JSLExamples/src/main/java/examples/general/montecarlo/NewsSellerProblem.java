/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package examples.general.montecarlo;

import jsl.utilities.random.rvariable.DEmpiricalRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.*;
import jslx.statistics.Bootstrap;

/**
 *
 */
public class NewsSellerProblem {

    private DEmpiricalRV typeofday;

    private DEmpiricalRV gd;

    private DEmpiricalRV fd;

    private DEmpiricalRV pd;

    private RVariableIfc[] demand = new RVariableIfc[4];

    private double price = 0.50;

    private double cost = 0.33;

    private double lostRevenueCost = 0.17;

    private double scrapPrice = 0.05;

    private int qMin = 50;

    private int qMax = 100;

    private Statistic myProfitStat;

    private Statistic myProfitGT0Stat;

    /**
     *
     */
    public NewsSellerProblem() {
        //super();
        System.out.println("Constructing NSP");
        typeofday = new DEmpiricalRV(new double[] {1.0, 2.0, 3.0}, new double[] {0.35, 0.80, 1.0});

        gd = new DEmpiricalRV(new double[]{40.0, 50.0, 60.0, 70.0, 80.0, 90.0, 100.0},
         new double[] {0.03, 0.08, 0.23, 0.43, 0.78, 0.93, 1.0});

        fd = new DEmpiricalRV(new double[]{40.0, 50.0, 60.0, 70.0, 80.0, 90.0},
                new double[] {0.1, 0.28, 0.68, 0.88, 0.96, 1.0});

        pd = new DEmpiricalRV(new double[]{40.0, 50.0, 60.0, 70.0, 80.0},
                new double[] {0.44, 0.66, 0.82, 0.94, 1.0});

        demand = new RVariableIfc[4];

        demand[1] = gd;
        demand[2] = fd;
        demand[3] = pd;

        myProfitStat = new Statistic("Avg Profit");
        myProfitGT0Stat = new Statistic("Profit > 0");

    }

    public void setPrice(double p) {
        price = p;
    }

    public double getPrice() {
        return (price);
    }

    public void runSimulation(int q, int n) {
        myProfitStat.reset();
        myProfitGT0Stat.reset();
        for (int k = 1; k <= n; k++) {
            double d = demand[(int) typeofday.getValue()].getValue();
            double profit = price * Math.min(d, q) - cost * q
                    - lostRevenueCost * Math.max(0, d - q) + scrapPrice
                    * Math.max(0, q - d);
            myProfitStat.collect(profit);
            myProfitGT0Stat.collect(profit > 0);

        }
    }

    public Statistic getProfitStat(){
        return myProfitStat;
    }

    public Statistic getProfitGT0Stat(){
        return myProfitGT0Stat;
    }

    public static void main(String[] args) {

        NewsSellerProblem nsp = new NewsSellerProblem();
        nsp.getProfitStat().setSaveOption(true);
        nsp.runSimulation(50, 100);
        System.out.println(nsp.getProfitStat());

        double[] profits = nsp.getProfitStat().getSavedData();

        Bootstrap bs = new Bootstrap("Newseller Bootstrap Example", profits);

        bs.generateSamples(1000);

        System.out.println(bs);
    }
}
