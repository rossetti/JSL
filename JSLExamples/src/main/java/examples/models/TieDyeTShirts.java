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
package examples.models;

import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.EventGeneratorActionIfc;
import jsl.modeling.elements.station.ReceiveQObjectIfc;
import jsl.modeling.elements.station.SResource;
import jsl.modeling.elements.station.SingleQueueStation;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.DEmpiricalRV;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.TriangularRV;
import jsl.utilities.random.rvariable.UniformRV;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.lang.IllegalStateException;

/**  This implementation models the Tie Dye T-Shirt example without shared resources.
 *
 * @author rossetti
 */
public class TieDyeTShirts extends ModelElement {

    private final EventGenerator myOrderGenerator;
    private RandomVariable myTBOrders;
    private RandomVariable myOrderSize;
    private RandomVariable myOrderType;
    private RandomVariable myShirtMakingTime;
    private RandomVariable myPaperWorkTime;
    private RandomVariable myPackagingTime;
    private SingleQueueStation myShirtMakingStation;
    private SingleQueueStation myPWStation;
    private SingleQueueStation myPackagingStation;
    private SResource myShirtMakers;
    private SResource myWorker;
    private SResource myPackager;
    private ResponseVariable mySystemTime;
    private TimeWeighted myNumInSystem;

    public TieDyeTShirts(ModelElement parent) {
        this(parent, null);
    }

    public TieDyeTShirts(ModelElement parent, String name) {
        super(parent, name);
        myTBOrders = new RandomVariable(this, new ExponentialRV(15));
        myOrderGenerator = new EventGenerator(this, new OrderArrivals(),
                myTBOrders, myTBOrders);
        DEmpiricalRV type = new DEmpiricalRV(new double[]{1.0, 2.0}, new double[] {0.7, 1.0});
        DEmpiricalRV size = new DEmpiricalRV(new double[]{3.0, 5.0}, new double[] {0.75, 1.0});
        myOrderSize = new RandomVariable(this, size);
        myOrderType = new RandomVariable(this, type);
        myShirtMakingTime = new RandomVariable(this, new UniformRV(3, 5));
        myPaperWorkTime = new RandomVariable(this, new UniformRV(8, 10));
        myPackagingTime = new RandomVariable(this, new TriangularRV(5, 10, 15));
        myShirtMakers = new SResource(this, 1, "ShirtMakers_R");
        myPackager = new SResource(this, 1, "Packager_R");
        myShirtMakingStation = new SingleQueueStation(this, myShirtMakers,
                myShirtMakingTime, "Shirt_Station");
        myWorker = new SResource(this, 1, "PW-Worker");
        myPWStation = new SingleQueueStation(this, myWorker,
                myPaperWorkTime, "PW_Station");
        myPackagingStation = new SingleQueueStation(this, myPackager,
                myPackagingTime, "Packing_Station");
        // need to set senders/receivers
        myShirtMakingStation.setNextReceiver(new AfterShirtMaking());
        myPWStation.setNextReceiver(new AfterPaperWork());
        myPackagingStation.setNextReceiver(new Dispose());
        mySystemTime = new ResponseVariable(this, "System Time");
        myNumInSystem = new TimeWeighted(this, "Num in System");
    }

    private class OrderArrivals implements EventGeneratorActionIfc {

        @Override
        public void generate(EventGenerator generator, JSLEvent event) {
            myNumInSystem.increment();
            Order order = new Order();
            List<Order.Shirt> shirts = order.getShirts();

            for (Order.Shirt shirt : shirts) {
                myShirtMakingStation.receive(shirt);
            }
            myPWStation.receive(order.getPaperWork());

        }

    }

    protected class Dispose implements ReceiveQObjectIfc {

        @Override
        public void receive(QObject qObj) {
            // collect final statistics
            myNumInSystem.decrement();
            mySystemTime.setValue(getTime() - qObj.getCreateTime());
            Order o = (Order) qObj;
            o.dispose();
        }

    }

    protected class AfterShirtMaking implements ReceiveQObjectIfc {

        @Override
        public void receive(QObject qObj) {
            Order.Shirt shirt = (Order.Shirt) qObj;
            shirt.setDoneFlag();
        }

    }

    protected class AfterPaperWork implements ReceiveQObjectIfc {

        @Override
        public void receive(QObject qObj) {
            Order.PaperWork pw = (Order.PaperWork) qObj;
            pw.setDoneFlag();
        }

    }

    /**
     * Handles the completion of an order
     *
     */
    private void orderCompleted(Order order) {
        myPackagingStation.receive(order);
    }

    private class Order extends QObject {

        private int myType;
        private int mySize;
        private PaperWork myPaperWork;
        private List<Shirt> myShirts;
        private int myNumCompleted;
        private boolean myPaperWorkDone;

        public Order(double creationTime, String name) {
            super(creationTime, name);
            myNumCompleted = 0;
            myPaperWorkDone = false;
            myType = (int) myOrderType.getValue();
            mySize = (int) myOrderSize.getValue();
            myShirts = new ArrayList<>();
            for (int i = 1; i <= mySize; i++) {
                myShirts.add(new Shirt());
            }
            myPaperWork = new PaperWork();
        }

        public Order() {
            this(getTime());
        }

        public Order(double creationTime) {
            this(creationTime, null);
        }

        public int getType() {
            return myType;
        }

        public void dispose() {
            myPaperWork = null;
            myShirts.clear();
            myShirts = null;
        }

        public List<Shirt> getShirts() {
            List<Shirt> list = new ArrayList<>(myShirts);
            return list;
        }

        public PaperWork getPaperWork() {
            return myPaperWork;
        }

        /**
         * The order is complete if it has all its shirts and its paperwork
         *
         * @return
         */
        public boolean isComplete() {
            return ((areShirtsDone()) && (isPaperWorkDone()));
        }

        public boolean areShirtsDone() {
            return (myNumCompleted == mySize);
        }

        public boolean isPaperWorkDone() {
            return (myPaperWorkDone);
        }

        public int getNumShirtsCompleted() {
            return myNumCompleted;
        }

        private void shirtCompleted() {
            if (areShirtsDone()) {
                throw new IllegalStateException("The order already has all its shirts.");
            }
            // okay not complete, need to add shirt
            myNumCompleted = myNumCompleted + 1;
            if (isComplete()) {
                TieDyeTShirts.this.orderCompleted(this);
            }
        }

        private void paperWorkCompleted() {
            if (isPaperWorkDone()) {
                throw new IllegalStateException("The order already has paperwork.");
            }
            myPaperWorkDone = true;
            if (isComplete()) {
                TieDyeTShirts.this.orderCompleted(this);
            }
        }

        protected class Shirt extends QObject {

            protected boolean myDoneFlag = false;

            public Shirt() {
                this(getTime());
            }

            public Shirt(double creationTime) {
                this(creationTime, null);
            }

            public Shirt(double creationTime, String name) {
                super(creationTime, name);
            }

            public Order getOrder() {
                return Order.this;
            }

            public void setDoneFlag() {
                if (myDoneFlag == true) {
                    throw new IllegalStateException("The shirt is already done.");
                }
                myDoneFlag = true;
                Order.this.shirtCompleted();
            }

            public boolean isCompleted() {
                return myDoneFlag;
            }

        }

        protected class PaperWork extends Shirt {

            @Override
            public void setDoneFlag() {
                if (myDoneFlag == true) {
                    throw new IllegalStateException("The paperwork is already done.");
                }
                myDoneFlag = true;
                Order.this.paperWorkCompleted();
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation sim = new Simulation("Tie-Dye T-Shirts");
        // get the model
        Model m = sim.getModel();
        // add system to the main model
        TieDyeTShirts system = new TieDyeTShirts(m);
        // set the parameters of the experiment
        sim.setNumberOfReplications(50);
        //sim.setNumberOfReplications(2);
        sim.setLengthOfReplication(200000.0);
        sim.setLengthOfWarmUp(50000.0);
        SimulationReporter r = sim.makeSimulationReporter();
        System.out.println("Simulation started.");
        sim.run();
        System.out.println("Simulation completed.");
        r.printAcrossReplicationSummaryStatistics();
        r.writeAcrossReplicationSummaryStatisticsAsLaTeX(new PrintWriter(System.out));
    }
}
