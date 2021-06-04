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
package test.modeling;

import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;
import jsl.modeling.resource.Request;
import jsl.modeling.resource.RequestReactorAdapter;
import jsl.modeling.resource.ResourceUnit;

/**
 *
 * @author rossetti
 */
public class TestMakingResourceUnitsAndQueues {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation sim = new Simulation("Test Making ResourceUnit");
        Model model = sim.getModel();
        ResourceUnit r1 = new ResourceUnit.Builder(model).build();
        RequestReactor rr = new RequestReactor();
        // directly hold Customer
        //Queue<Customer> q = new Queue.Builder<Customer>(model).builder();
        Queue<Customer> q = new Queue<>(model);
        QObject qo = new QObject(0.0);
        Queue<QObject> q1 = new Queue<>(model);
        q1.enqueue(qo);
        QObject remove = q1.remove(0);
    }

    public static class Customer extends QObject {

        public Customer(double creationTime, String name) {
            super(creationTime, name);
        }

    }
    
    

    public static class RequestReactor extends RequestReactorAdapter {

        @Override
        public void rejected(Request request) {
            super.rejected(request); //To change body of generated methods, choose Tools | Templates.
        }

    }
}
