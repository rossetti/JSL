package jslx.statistics;

import jsl.observers.ControlVariateDataCollector;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//TODO

public class ControlVariateEstimator {

    private final List<String> myResponseNames;
    private final List<String> myControlNames;
//    private final Table myData;

    public ControlVariateEstimator(ControlVariateDataCollector data){
        this(data.getResponseNames(), data.getControlNames(), data.getDataAsMap());
    }

    public ControlVariateEstimator(List<String> responseNames, List<String> controlNames,
                                   Map<String, double[]> data){
        Objects.requireNonNull(responseNames, "The response names was null");
        Objects.requireNonNull(controlNames, "The control names was null");
        Objects.requireNonNull(data, "The data was null");
        if (responseNames.isEmpty()){
            throw new IllegalArgumentException("The response names was empty");
        }
        if (controlNames.isEmpty()){
            throw new IllegalArgumentException("The control names was empty");
        }
        if (data.isEmpty()){
            throw new IllegalArgumentException("The data was empty");
        }
        myResponseNames = new ArrayList<>(responseNames);
        myControlNames = new ArrayList<>(controlNames);
//        Table t = Table.create("Control Variate Data");
//        for(String name: data.keySet()){
//            DoubleColumn nc = DoubleColumn.create(name, data.get(name));
//            t = t.addColumns(nc);
//        }
//        myData = t;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
//        sb.append(myData.shape());
//        sb.append(System.lineSeparator());
//        sb.append(System.lineSeparator());
//        sb.append(myData.structure());
//        sb.append(System.lineSeparator());
//        sb.append(System.lineSeparator());
//        sb.append(myData);
//        sb.append(System.lineSeparator());
        return sb.toString();
    }
}
