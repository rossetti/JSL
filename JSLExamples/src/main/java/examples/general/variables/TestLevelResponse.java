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

package examples.general.variables;

import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.modeling.elements.variable.LevelResponse;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.Variable;
import jsl.utilities.random.rvariable.NormalRV;


public class TestLevelResponse extends ModelElement {

    private RandomVariable myRV;
    private Variable myVariable;

    private LevelResponse myLR;

    private ResponseVariable myR;

    public TestLevelResponse(ModelElement parent) {
        this(parent, null);
    }

    public TestLevelResponse(ModelElement parent, String name) {
        super(parent, name);
        myRV = new RandomVariable(this, new NormalRV(0.0, 1.0, 1));
        myVariable = new Variable(this, "Level Variable");
        myLR = new LevelResponse(myVariable, 0.0);
        myR = new ResponseVariable(this, "Observations");
    }

    @Override
    protected void initialize() {
        schedule(this::variableUpdate).in(1.0).units();
    }

    protected void variableUpdate(JSLEvent evnt){

        double x = myRV.getValue();
        myVariable.setValue(x);
        myR.setValue(x);
        schedule(this::variableUpdate).in(1.0).units();
        //System.out.println("in variable update");
    }

    public static void main(String[] args) {
        Simulation s = new Simulation("Temp");

        new TestLevelResponse(s.getModel());

        s.setNumberOfReplications(10);
        s.setLengthOfReplication(10000.0);
        s.run();

        s.printHalfWidthSummaryReport();
    }

}
