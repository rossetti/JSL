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
package jsl.utilities.distributions;

/** A degenerate distribution on a single value.  The value may
 *  be changed via the setParameters() method or the setValue() method.
 *  This is primarily to avoid having to make many Constants.
 *
 * @author rossetti
 */
public class VConstant extends Constant {

    public VConstant(double value) {
        super(value);
    }

    public VConstant(double[] parameters) {
        super(parameters[0]);
    }

    @Override
    public VConstant newInstance() {
        return (new VConstant(myValue));
    }

    @Override
    public void setParameters(double[] parameters) {
        myValue = parameters[0];
    }

    /**
     *
     * @param value the value to use for the degenerate distribution
     */
    public void setValue(double value) { myValue = value;}

}
