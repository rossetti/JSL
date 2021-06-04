/*
 * Copyright (c) 2019. Manuel D. Rossetti, rossetti@uark.edu
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

package examples.utilities.random;

import jsl.utilities.distributions.Bernoulli;
import jsl.utilities.random.rvariable.*;

import java.util.Optional;

public class RVControlExamples {

    public static void main(String[] args) {

        RVFactoryExample();

        RVControlExample();
    }

    public static void RVFactoryExample(){
        //make a control for normal random variables
        // the factory can make many types based on supplied enum
        Optional<RVControls> nrvControls = RVFactory.getRVControls(RVariableIfc.RVType.Normal);

        RVControls rvControls = nrvControls.get();

        // default control makes N(0,1)
        System.out.println("N(0,1)");
        RVariableIfc rv1 = rvControls.makeRVariable();
        for(int i=1;i<=5;i++){
            System.out.printf("rv1 = %f %n", rv1.getValue());
        }

        // use control to make N(20, 2) rv
        rvControls.changeDoubleControl("mean", 20.0);
        rvControls.changeDoubleControl("variance", 2.0);
        System.out.println("N(20,2)");
        RVariableIfc rv2 = rvControls.makeRVariable();
        for(int i=1;i<=5;i++){
            System.out.printf("rv2 = %f %n", rv2.getValue());
        }

    }

    public static void RVControlExample(){

        // any of the implemented distributions know how to make their own controls
        RVControls binomialControl = BinomialRV.makeControls();

        System.out.println("Binomial(n=2,p=0.5)");
        RVariableIfc rv1 = binomialControl.makeRVariable();
        for(int i=1;i<=5;i++){
            System.out.printf("rv1 = %f %n", rv1.getValue());
        }
        binomialControl.changeIntegerControl("NumTrials", 100);
        binomialControl.changeDoubleControl("ProbOfSuccess", 0.8);

        RVariableIfc rv2 = binomialControl.makeRVariable();
        System.out.println("Binomial(n=100,p=0.8)");
        for(int i=1;i<=5;i++){
            System.out.printf("rv2 = %f %n", rv2.getValue());
        }

        // make the random variable based on stream 3
        RVariableIfc rv3 = binomialControl.makeRVariable(3);
        System.out.println("Binomial(n=100,p=0.8)");
        for(int i=1;i<=5;i++){
            System.out.printf("rv3 = %f %n", rv3.getValue());
        }

        // make the random variable based on the next stream
        RVariableIfc rv4 = binomialControl.makeRVariable(JSLRandom.nextRNStream());
        System.out.println("Binomial(n=100,p=0.8)");
        for(int i=1;i<=5;i++){
            System.out.printf("rv3 = %f %n", rv3.getValue());
        }
    }
}
