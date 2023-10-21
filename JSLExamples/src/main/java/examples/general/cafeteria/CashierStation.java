/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package examples.general.cafeteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jsl.modeling.elements.station.SendQObjectIfc;
import jsl.modeling.elements.station.SingleQueueStation;
import jsl.modeling.elements.station.Station;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.QueueResponse;
import jsl.simulation.ModelElement;

/**
 *
 * @author rossetti
 */
public class CashierStation extends Station {

    /**
     * List to hold individual cashier stations
     */
    private List<SingleQueueStation> myStations;

    private Sender mySender;

    public CashierStation(ModelElement parent) {
        this(parent, 1, null, null);
    }

    public CashierStation(ModelElement parent, String name) {
        this(parent, 1, null, name);
    }

    public CashierStation(ModelElement parent, int numStations) {
        this(parent, numStations, null, null);
    }

    public CashierStation(ModelElement parent, int numStations,
            SendQObjectIfc sender, String name) {
        super(parent, sender, name);
        myStations = new ArrayList<SingleQueueStation>();
        mySender = new Sender();
        setNumberStations(numStations);
    }

    public final void setUseQObjectServiceTimeOption(boolean option){
        for(SingleQueueStation s: myStations){
            s.setUseQObjectServiceTimeOption(option);
        }
    }
    
    public final void setNumberStations(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Number of stations must be >=1");
        }
        // get current number of stations
        int m = myStations.size();
        if (m == n) {
            return;
        } else if (m > n) {
            //need to remove extra stations, starting at end
            int k = m - n;
            for (int i = 1; i <= k; i++) {
                SingleQueueStation s = myStations.remove(myStations.size() - 1);
                System.out.println("removing " + s.getName());
                s.removeFromModel();
            }
            return;
        } else {// need to add stations
            while (m < n) {
                SingleQueueStation s = new SingleQueueStation(this, "Cashier:" + (m + 1));
                Optional<QueueResponse<QObject>> r = s.getQueueResponses();
                if (r.isPresent()){
                    r.get().turnOnAcrossReplicationMaxNumInQueueCollection();
                    r.get().turnOnAcrossReplicationMaxTimeInQueueCollection();
                }
                // tell the individual station to use this CashierStation's sender
                System.out.println("adding " + s.getName());
                s.setSender(mySender);
                myStations.add(s);
                m++;
            }
        }

    }

    @Override
    public void receive(QObject qObj) {
        Station station = selectStation();
        station.receive(qObj);
    }

    public Station selectStation() {
        // check if there is station with an idle server, then use it
        for (SingleQueueStation t : myStations) {
            if (t.isResourceAvailable()) {
                return (t);
            }
        }
        // no idle servers at stations, then pick station with shortest queue
        SingleQueueStation shortest = null;
        int min = Integer.MAX_VALUE;
        for (SingleQueueStation t : myStations) {
            int n = t.getNumberInQueue();
            if (n < min) {
                shortest = t;
                min = n;
            }
        }
        return (shortest);
    }

    class Sender implements SendQObjectIfc {

        @Override
        public void send(QObject qObj) {
            CashierStation.this.send(qObj);
        }
    }
}
