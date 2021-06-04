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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.utilities.statistics;

import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.statistic.BatchStatistic;

/**
 *
 * @author rossetti
 */
public class BatchStatisticExample {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        ExponentialRV d = new ExponentialRV(2);

        // number of observations
        int n = 1000; 
        
        // minimum number of batches permitted
        // there will not be less than this number of batches
        int minNumBatches = 40;
        
        // minimum batch size permitted
        // the batch size can be no smaller than this amount
        int minBatchSize = 25; 
        
        // maximum number of batch multiple
        //  The multiple of the minimum number of batches
        //  that determines the maximum number of batches
        //  e.g. if the min. number of batches is 20
        //  and the max number batches multiple is 2,
        //  then we can have at most 40 batches
        int maxNBMultiple = 2; 

        // In this example, since 40*25 = 1000, the batch multiple does not matter
        
        BatchStatistic bm = new BatchStatistic(minNumBatches, minBatchSize, maxNBMultiple);

        for (int i = 1; i <= n; ++i) {
            bm.collect(d.getValue());
        }
        System.out.println(bm);

        double[] bma = bm.getBatchMeanArrayCopy();
        int i=0;
        for(double x: bma){
            System.out.println("bm(" + i + ") = " + x);
            i++;
        }
        // this rebatches the 40 down to 10
        //Statistic s = bm.rebatchToNumberOfBatches(10);
        //System.out.println(s);

    }
}
