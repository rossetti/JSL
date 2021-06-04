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
package examples.station;

import jsl.modeling.elements.EventGenerator;
import jsl.modeling.elements.EventGeneratorActionIfc;
import jsl.modeling.elements.station.ReceiveQObjectIfc;
import jsl.modeling.elements.station.SingleQueueStation;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.queue.QObject;
import jsl.simulation.*;
import jsl.utilities.random.rvariable.ExponentialRV;

/**
 * Arriving customers choose randomly to two stations.  
 * The arrivals are Poisson with mean rate 1.1. Thus, the time 
 * between arrivals is exponential with mean 1/1.1.
 * After receiving service at the first station the customer moves 
 * directly to the second station.
 * 
 * The service times of the stations are exponential with means 0.8 and 0.7, 
 * respectively. After receiving service at the 2nd station, the
 * customer leaves.
 *
 * @author rossetti
 */
public class TandemQueue extends SchedulingElement {

    protected EventGenerator myArrivalGenerator;

    protected RandomVariable myTBA;

    protected SingleQueueStation myStation1;

    protected SingleQueueStation myStation2;

    protected RandomVariable myST1;

    protected RandomVariable myST2;
    
    protected ResponseVariable mySysTime;

    public TandemQueue(ModelElement parent) {
        this(parent, null);
    }

    public TandemQueue(ModelElement parent, String name) {
        super(parent, name);

        myTBA = new RandomVariable(this, new ExponentialRV(1.0/1.1));

        myST1 = new RandomVariable(this, new ExponentialRV(0.8));

        myST2 = new RandomVariable(this, new ExponentialRV(0.7));

        myArrivalGenerator = new EventGenerator(this, new Arrivals(), myTBA, myTBA);
       
        myStation1 = new SingleQueueStation(this, myST1, "Station1");

        myStation2 = new SingleQueueStation(this, myST2, "Station2");
        
        myStation1.setNextReceiver(myStation2);
        myStation2.setNextReceiver(new Dispose());
        
        mySysTime = new ResponseVariable(this, "System Time");

    }

    protected class Arrivals implements EventGeneratorActionIfc {

        @Override
        public void generate(EventGenerator generator, JSLEvent event) {
            myStation1.receive(new QObject(getTime()));
        }

    }

    protected class Dispose implements ReceiveQObjectIfc {

        @Override
        public void receive(QObject qObj) {
           // collect system time
            mySysTime.setValue(getTime() - qObj.getCreateTime());
        }
        
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Simulation s = new Simulation("Tandem Station Example");
        
        new TandemQueue(s.getModel());
        
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(20000);
        s.setLengthOfWarmUp(5000);
        SimulationReporter r = s.makeSimulationReporter();
        
        s.run();
        
        r.printAcrossReplicationSummaryStatistics();
    }

}
