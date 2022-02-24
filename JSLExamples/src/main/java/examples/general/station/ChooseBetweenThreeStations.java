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
package examples.general.station;

import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.EventGeneratorActionIfc;
import jsl.modeling.elements.station.NWayByChanceStationSender;
import jsl.modeling.elements.station.ReceiveQObjectIfc;
import jsl.modeling.elements.station.SingleQueueStation;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.queue.QObject;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ExponentialRV;

import java.util.Arrays;
import java.util.List;

/**
 * Arriving customers choose randomly between three stations. The arrivals are
 * Poisson with mean rate 1.1. Thus, the time between arrivals is exponential
 * with mean 1/1.1. The first station is chosen with probability 0.4. The second
 * station is chosen with probability 0.3. The 3rd station with probability 0.3.
 * The service times of the stations are exponential with means 0.8, 0.7, 0.6,
 * respectively. After receiving service at the chosen station, the customer
 * leaves.
 *
 * @author rossetti
 */
public class ChooseBetweenThreeStations extends SchedulingElement {

    protected EventGenerator myArrivalGenerator;

    protected RandomVariable myTBA;

    protected SingleQueueStation myStation1;

    protected SingleQueueStation myStation2;

    protected SingleQueueStation myStation3;

    protected RandomVariable myST1;

    protected RandomVariable myST2;

    protected RandomVariable myST3;

    protected NWayByChanceStationSender myTwoWay;

    public ChooseBetweenThreeStations(ModelElement parent) {
        this(parent, null);
    }

    public ChooseBetweenThreeStations(ModelElement parent, String name) {
        super(parent, name);

        myTBA = new RandomVariable(this, new ExponentialRV(1.0 / 1.1));

        myST1 = new RandomVariable(this, new ExponentialRV(0.8));

        myST2 = new RandomVariable(this, new ExponentialRV(0.7));

        myST3 = new RandomVariable(this, new ExponentialRV(0.6));

        myArrivalGenerator = new EventGenerator(this, new Arrivals(), myTBA, myTBA);

        // Stations must have a sender or a receiver
        Dispose d = new Dispose();

        myStation1 = new SingleQueueStation(this, myST1, "Station1");
        myStation1.setNextReceiver(d);
        myStation2 = new SingleQueueStation(this, myST2, "Station2");
        myStation2.setNextReceiver(d);
        myStation3 = new SingleQueueStation(this, myST3, "Station3");
        myStation3.setNextReceiver(d);

        List<ReceiveQObjectIfc> list = Arrays.asList(myStation1, myStation2, myStation3);
        double[] cdf = {0.4, 0.7, 1.0};
        myTwoWay = new NWayByChanceStationSender(this, list, cdf);

    }

    protected class Arrivals implements EventGeneratorActionIfc {

        @Override
        public void generate(EventGenerator generator, JSLEvent event) {

            myTwoWay.receive(new QObject(getTime()));
        }

    }

    protected class Dispose implements ReceiveQObjectIfc {

        @Override
        public void receive(QObject qObj) {
            // do nothing
            // if needs rework send back
            // else, call exitSystem()
            myStation1.receive(qObj);

        }

    }

    private void exitSystem(QObject part){



    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation s = new Simulation("Choose btw 3 Stations Example");

        new ChooseBetweenThreeStations(s.getModel());

        s.setNumberOfReplications(10);
        s.setLengthOfReplication(20000);
        s.setLengthOfWarmUp(5000);
        SimulationReporter r = s.makeSimulationReporter();

        s.run();

        r.printAcrossReplicationSummaryStatistics();
    }

}
