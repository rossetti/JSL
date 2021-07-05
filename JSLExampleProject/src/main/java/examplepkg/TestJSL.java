package examplepkg;


import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.statistic.Statistic;
import jslx.statistics.Bootstrap;

import java.util.List;

public class TestJSL {
    public static void main(String[] args) {

        System.out.println("Hello, JSL");

        NormalRV rv = new NormalRV();

        double[] sample = rv.sample(100);

        Statistic s = new Statistic();
        s.collect(sample);
        System.out.println(s);

        // use a class from the JSLExtensions
        example1();
    }

    public static void example1(){
        Normal n = new Normal(10, 3);
        RVariableIfc rv = n.getRandomVariable();

        Bootstrap bs = new Bootstrap("Test Normal", rv.sample(50));

        bs.generateSamples(10, true);
        System.out.println(bs);
        List<Statistic> list = bs.getStatisticForEachBootstrapSample();

        for(Statistic s: list){
            System.out.println(s.asString());
        }

        bs.generateSamples(10, true);
        System.out.println(bs);
        List<Statistic> list2 = bs.getStatisticForEachBootstrapSample();

        for(Statistic s: list2){
            System.out.println(s.asString());
        }
    }
}
