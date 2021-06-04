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
package jsl.utilities.rootfinding;

import jsl.utilities.math.FunctionIfc;

public class BisectionRootFinder extends RootFinder {

    public BisectionRootFinder() {
        super();
    }

    public BisectionRootFinder(FunctionIfc func, double xLower, double xUpper) {
        super(func, xLower, xUpper);
    }

    @Override
    protected double evaluateIteration() {
        result = (xPos + xNeg) * 0.5;
        if (f.fx(result) > 0) {
            xPos = result;
        } else {
            xNeg = result;
        }
        return relativePrecision(Math.abs(xPos - xNeg));
    }

    @Override
    protected void finalizeIterations() {
    }

    @Override
    protected void initializeIterations() {
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

        FunctionIfc f = new FunctionIfc() {

            @Override
            public double fx(double x) {
                return x * x * x + 4.0 * x * x - 10.0;
            }
        };

        BisectionRootFinder b = new BisectionRootFinder(f, 1.0, 2.0);

        b.evaluate();

        System.out.println(b);

    }
}
