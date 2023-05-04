/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.general.cafeteria;

import java.util.*;

import jsl.modeling.elements.EventGenerator;

import jsl.modeling.elements.EventGeneratorActionIfc;
import jsl.modeling.elements.RandomElement;

import jsl.modeling.elements.station.DelayStation;
import jsl.modeling.elements.station.ReceiveQObjectIfc;
import jsl.modeling.elements.station.SingleQueueStation;
import jsl.modeling.elements.station.Station;
import jsl.modeling.elements.variable.RandomVariable;
import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.QueueResponse;
import jsl.observers.variable.MultipleComparisonDataCollector;
import jsl.simulation.JSLEvent;
import jsl.simulation.ModelElement;
import jsl.utilities.GetValueIfc;
import jsl.utilities.random.rvariable.DEmpiricalRV;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.UniformRV;


/**
 *
 * @author rossetti
 */
public class Cafeteria extends ModelElement {

    private EventGenerator myArrivalGenerator;

    private RandomVariable myGroupSizeRV;

    private RandomVariable myDrinksRV;

    private RandomVariable myHotFoodsRV;
    
    private UniformRV myHFCDF;
    
    private UniformRV mySSCDF;

    private RandomVariable mySSRV;

    private ResponseVariable mySystemTime;

    private TimeWeighted myNumInSys;

    private ResponseVariable myMaxInSys;

    private SingleQueueStation myHotFoods;

    private DelayStation myDrinks;

    private SingleQueueStation mySpecialtySandwiches;

    private CashierStation myCashierStation;

    private RandomElement<ReceiveQObjectIfc>  myStationSelector;

    private ReceiveQObjectIfc myExitReceiver;

    private Map<Station, GetValueIfc> myACTRV;

    private Map<Station, ResponseVariable> myTotalDelayMap;

    private Map<Station, ResponseVariable> myTotalMaxDelayMap;

    public Cafeteria(ModelElement parent, String name) {
        super(parent, name);
        ExponentialRV ad = new ExponentialRV(30.0,1);
        myArrivalGenerator = new EventGenerator(this, new ArrivalListener(), ad, ad);
        mySystemTime = new ResponseVariable(this, getName() + ":System Time");
        myNumInSys = new TimeWeighted(this, getName() + ":Num in System");
        myMaxInSys = new ResponseVariable(this, getName() + ":Avg Max in System");

        // set up the group size
        double[] values = {1.0, 2.0, 3.0, 4.0};
        double[] cdf = {0.5, 0.8, 0.9, 1.0};
        DEmpiricalRV gs = new DEmpiricalRV(values, cdf, 2);
        myGroupSizeRV = new RandomVariable(this, gs);

        // make the drink station
        myDrinks = new DelayStation(this, "Drinks");
        myDrinksRV = new RandomVariable(this, new UniformRV(5.0, 20.0, 6));
        myDrinks.setDelayTime(myDrinksRV);

        // make the hot food station
        myHotFoods = new SingleQueueStation(this, "Hot Foods");
        Optional<QueueResponse<QObject>> hqr = myHotFoods.getQueueResponses();
        if (hqr.isPresent()){
            hqr.get().turnOnAcrossReplicationMaxNumInQueueCollection();
            hqr.get().turnOnAcrossReplicationMaxTimeInQueueCollection();
        }
        myHFCDF = new UniformRV(50.0, 120, 4);
        myHotFoodsRV = new RandomVariable(this,myHFCDF);
        myHotFoods.setServiceTime(myHotFoodsRV);

        // make the specialty sandwich station
        mySpecialtySandwiches = new SingleQueueStation(this, "Sandwiches");
        Optional<QueueResponse<QObject>> ssqr = mySpecialtySandwiches.getQueueResponses();
        if (ssqr.isPresent()){
            ssqr.get().turnOnAcrossReplicationMaxNumInQueueCollection();
            ssqr.get().turnOnAcrossReplicationMaxTimeInQueueCollection();
        }
        mySSCDF = new UniformRV(60.0, 180, 5);
        mySSRV = new RandomVariable(this,mySSCDF);
        mySpecialtySandwiches.setServiceTime(mySSRV);

        //make the cashier station
        myCashierStation = new CashierStation(this, 2);
        myCashierStation.setUseQObjectServiceTimeOption(true);
        
        // make the cafeteria's exit receiver
        myExitReceiver = new ExitReceiver();

        // hook up the stations
        // drinks always goes to cashier
        myDrinks.setNextReceiver(myCashierStation);
        // hot foods always goes to drinks
        myHotFoods.setNextReceiver(myDrinks);
        // specialty sandwiches always goes to drinks
        mySpecialtySandwiches.setNextReceiver(myDrinks);
        // customers must leave through the cafeteria's exit
        myCashierStation.setNextReceiver(myExitReceiver);

        // set up initial station selection
        List<ReceiveQObjectIfc> list = new ArrayList<>();
        list.add(myHotFoods);
        list.add(mySpecialtySandwiches);
        list.add(myDrinks);
        double[] cdf2 = {0.8, 0.95, 1.0};
        myStationSelector = new RandomElement<ReceiveQObjectIfc>(this, list, cdf2);
        myStationSelector.setRandomNumberStream(3);
        // set up accumulated cashier times
        RandomVariable d = new RandomVariable(this, new ExponentialRV(50.0, 7));
        RandomVariable ss = new RandomVariable(this, new ExponentialRV(45.0, 8));
        RandomVariable hf = new RandomVariable(this, new ExponentialRV(30.0, 9));

        myACTRV = new HashMap<Station, GetValueIfc>();
        myACTRV.put(myHotFoods, new SumGetValue(hf, d));
        myACTRV.put(mySpecialtySandwiches, new SumGetValue(ss, d));
        myACTRV.put(myDrinks, d);

        // set up total and max delay collection by type
        myTotalDelayMap = new HashMap<Station, ResponseVariable>();
        myTotalDelayMap.put(myHotFoods, new ResponseVariable(this, "HF Total Delay"));
        myTotalDelayMap.put(mySpecialtySandwiches, new ResponseVariable(this, "SS Total Delay"));
        myTotalDelayMap.put(myDrinks, new ResponseVariable(this, "Drink Only Total Delay"));

        myTotalMaxDelayMap = new HashMap<Station, ResponseVariable>();
        myTotalMaxDelayMap.put(myHotFoods, new ResponseVariable(this, "HF Total Max Delay"));
        myTotalMaxDelayMap.put(mySpecialtySandwiches, new ResponseVariable(this, "SS Total Max Delay"));
        myTotalMaxDelayMap.put(myDrinks, new ResponseVariable(this, "Drink Only Total Max Delay"));

    }

    public void setNumberCashierStations(int n) {
        myCashierStation.setNumberStations(n);
    }

    public final void setSpecialtySandwichCDFRange(double min, double max) {
        mySSRV.setInitialRandomSource(new UniformRV(min, max));
    }

    public final void setHotFoodsCDFRange(double min, double max) {
        myHotFoodsRV.setInitialRandomSource(new UniformRV(min, max));
    }

    public final void setInitialNumberOfServersAtHotFoods(int n) {
        myHotFoods.setInitialCapacity(n);
    }

    public final void setInitialNumberOfServersAtSpecialtySandwiches(int n) {
        mySpecialtySandwiches.setInitialCapacity(n);
    }
    
    public final MultipleComparisonDataCollector attachMCDataCollectorToSystemTime(){
        return new MultipleComparisonDataCollector(mySystemTime);
    }

    @Override
    protected void replicationEnded() {
        double max = myNumInSys.getWithinReplicationStatistic().getMax();
        myMaxInSys.setValue(max);
        for(Station s: myTotalMaxDelayMap.keySet()){
            // get the max
            max = myTotalDelayMap.get(s).getWithinReplicationStatistic().getMax();
            // observe it
            myTotalMaxDelayMap.get(s).setValue(max);
        }
    }

    class ArrivalListener implements EventGeneratorActionIfc {

        @Override
        public void generate(EventGenerator eg, JSLEvent jsle) {
            int n = (int) myGroupSizeRV.getValue();
            for (int i = 1; i <= n; i++) {
                myNumInSys.increment();
                Customer c = new Customer(getTime());
                Station station = (Station) myStationSelector.getRandomElement();
                c.myStation = station;
                c.setValueObject(myACTRV.get(station));
                station.receive(c);
            }
        }
    }

    class Customer extends QObject {

        private Station myStation;

        public Customer(double creationTime) {
            this(creationTime, null);
        }

        public Customer(double creationTime, String name) {
            super(creationTime, name);
        }
    }

    class ExitReceiver implements ReceiveQObjectIfc {

        @Override
        public void receive(QObject qObj) {
            myNumInSys.decrement();
            Customer c = (Customer) qObj;
            mySystemTime.setValue(getTime() - c.getCreateTime());
            double t = c.getQueuedState().getTotalTimeInState();
            ResponseVariable rs = myTotalDelayMap.get(c.myStation);
            rs.setValue(t);
        }
    }

    private class SumGetValue implements GetValueIfc {

        private final GetValueIfc myG1;

        private final GetValueIfc myG2;

        public SumGetValue(GetValueIfc g1, GetValueIfc g2) {
            myG1 = g1;
            myG2 = g2;
        }

        @Override
        public double getValue() {
            return myG1.getValue() + myG2.getValue();
        }
    }
}
