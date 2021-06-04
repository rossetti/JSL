package jsl.utilities.statistic.welch;

import jsl.modeling.elements.variable.ResponseVariable;
import jsl.modeling.elements.variable.TimeWeighted;
import jsl.observers.ModelElementObserver;
import jsl.simulation.ModelElement;
import jsl.utilities.JSLFileUtil;

import java.nio.file.Path;
import java.util.Objects;

public class WelchFileObserver extends ModelElementObserver {

    private final WelchDataFileCollector myWelchDataFileCollector;

    public WelchFileObserver(ResponseVariable responseVariable, double batchSize){
        Objects.requireNonNull(responseVariable,"The response variable cannot be null");
        StatisticType statType = null;
        if (responseVariable instanceof TimeWeighted){
            statType = StatisticType.TIME_PERSISTENT;
        } else {
            statType = StatisticType.TALLY;
        }
        Path outDir = responseVariable.getSimulation().getOutputDirectory().getOutDir();
        Path subDir = outDir.resolve(responseVariable.getName() + "_Welch");
        myWelchDataFileCollector = new WelchDataFileCollector(subDir, statType, responseVariable.getName(), batchSize);
        responseVariable.addObserver(this);
    }

    @Override
    public String toString(){
        return myWelchDataFileCollector.toString();
    }

    public static WelchFileObserver createWelchFileObserver(ResponseVariable responseVariable){
        return new WelchFileObserver(responseVariable, 1.0);
    }

    public static WelchFileObserver createWelchFileObserver(TimeWeighted responseVariable, double deltaTInterval){
        return new WelchFileObserver(responseVariable, deltaTInterval);
    }

    public WelchDataFileAnalyzer makeWelchDataFileAnalyzer() {
        return myWelchDataFileCollector.makeWelchDataFileAnalyzer();
    }

    public String getWelchFileMetaDataBeanAsJson() {
        return myWelchDataFileCollector.getWelchFileMetaDataBeanAsJson();
    }

    @Override
    protected void beforeExperiment(ModelElement m, Object arg) {
        myWelchDataFileCollector.setUpCollector();
    }

    @Override
    protected void beforeReplication(ModelElement m, Object arg) {
        myWelchDataFileCollector.beginReplication();
    }

    @Override
    protected void afterReplication(ModelElement m, Object arg) {
        myWelchDataFileCollector.endReplication();
    }

    @Override
    protected void update(ModelElement m, Object arg) {
        ResponseVariable rv = (ResponseVariable)m;
        myWelchDataFileCollector.collect(rv.getTime(), rv.getValue());
    }

    @Override
    protected void afterExperiment(ModelElement m, Object arg) {
        myWelchDataFileCollector.cleanUpCollector();
    }
}
