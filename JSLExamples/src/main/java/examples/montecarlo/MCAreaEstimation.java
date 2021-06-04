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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package examples.montecarlo;

import jsl.utilities.random.rvariable.UniformRV;
import jsl.utilities.statistic.Statistic;

/**
 *
 * @author rossetti
 */
public class MCAreaEstimation {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double a = 1.0;
        double b = 4.0;
        UniformRV ucdf = new UniformRV(a, b);
        Statistic stat = new Statistic("Area Estimator");
        int n = 100; // sample size
        for(int i=1;i<=n;i++){
            double x = ucdf.getValue();
            double gx = Math.sqrt(x);
            double y = (b-a)*gx;
            stat.collect(y);
        }
        System.out.printf("True Area = %10.3f\n", 14.0/3.0);
        System.out.printf("Area estimate = %10.3f\n", stat.getAverage());
        System.out.println("Confidence Interval");
        System.out.println(stat.getConfidenceInterval());
       
    }
    
}
