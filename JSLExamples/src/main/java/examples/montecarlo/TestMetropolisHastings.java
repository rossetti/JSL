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

package examples.montecarlo;

import jsl.observers.ObserverIfc;
import jsl.utilities.JSLFileUtil;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.distributions.Normal;
import jsl.utilities.random.mcmc.MetropolisHastings1D;
import jsl.utilities.random.mcmc.ProposalFunction1DIfc;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.reporting.JSL;

import java.io.PrintWriter;

public class TestMetropolisHastings {

    public static void main(String[] args) {

        Function f = new Function();
        PropFunction q = new PropFunction();

        MetropolisHastings1D m = new MetropolisHastings1D(0.0, f, q);
        m.addObserver(new WriteOut());

        m.runAll(10000);
        System.out.println(m);
    }

    public static class Function implements FunctionIfc {

        Normal n = new Normal(10, 1);

        @Override
        public double fx(double x) {
            return n.pdf(x);
        }
    }

    public static class PropFunction implements ProposalFunction1DIfc {

        NormalRV n = new NormalRV(0, 0.01);

        @Override
        public double getProposalRatio(double x, double y) {
            return 1.0;
        }

        @Override
        public double generateProposedGivenCurrent(double current) {
            return current + n.getValue();
        }
    }

    public static class WriteOut implements ObserverIfc{

        PrintWriter printWriter = JSLFileUtil.makePrintWriter("MHOut.txt");

        @Override
        public void update(Object theObserved, Object arg) {
            MetropolisHastings1D m = (MetropolisHastings1D)theObserved;
            printWriter.println(m.getCurrentX());
        }
    }
}
