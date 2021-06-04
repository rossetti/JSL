package examples.utilities.random;

import jsl.utilities.distributions.Normal;
import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.*;

public class GenerateRVExamples {

    public static void main(String[] args) {
        //NormalRVExample();
        System.out.println();
        //TriangularRVExample();
        System.out.println();
        DEmpiricalRVExample();
        System.out.println();
       // NormalRVExample2();
    }

    public static void NormalRVExample() {
        // create a normal mean = 20.0, variance = 4.0 random variable
        NormalRV n = new NormalRV(20.0, 4.0);
        System.out.printf("%3s %15s %n", "n", "Values");
        // generate some values
        for (int i = 0; i < 5; i++) {
            // getValue() method returns generated values
            double x = n.getValue();
            System.out.printf("%3d %15f %n", i+1, x);
        }
    }

    public static void TriangularRVExample(){
        // create a triangular random variable with min = 2.0, mode = 5.0, max = 10.0
        TriangularRV t = new TriangularRV(2.0, 5.0, 10.0);
        // sample 5 values
        double[] sample = t.sample(5);
        System.out.printf("%3s %15s %n", "n", "Values");
        for (int i = 0; i < sample.length; i++) {
            System.out.printf("%3d %15f %n", i+1, sample[i]);
        }
    }

    public static void DEmpiricalRVExample(){
        // values is the set of possible values
        double[] values = {1.0, 2.0, 3.0, 4.0};
        // cdf is the cumulative distribution function over the values
        double[] cdf = {1.0/6.0, 3.0/6.0, 5.0/6.0, 1.0};
        //create a discrete empirical random variable
        DEmpiricalRV n1 = new DEmpiricalRV(values, cdf);
        System.out.println(n1);
        System.out.printf("%3s %15s %n", "n", "Values");
        for (int i = 1; i <= 5; i++) {
            System.out.printf("%3d %15f %n", i+1, n1.getValue());
        }
    }

    public static void NormalRVExample2(){
        // create distribution
        Normal n = new Normal(20.0, 4.0);
        // get a random variable
        RVariableIfc rv1 = n.getRandomVariable();
        // change the parameters of the distribution
        n.setMean(100.0);
        n.setVariance(20.0);
        // get another random variable
        RVariableIfc rv2 = n.getRandomVariable();
        System.out.printf("%3s %15s %15s %n", "n", "rv1", "rv2");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%3d %15f %15f %n", i+1, rv1.getValue(), rv2.getValue());
        }
    }

    public static void specificStreamExample(){
        // get stream 3
        RNStreamIfc stream = JSLRandom.rnStream(3);
        // create a normal mean = 20.0, variance = 4.0, with the stream
        NormalRV n = new NormalRV(20.0, 4.0, stream);
        System.out.printf("%3s %15s %n", "n", "Values");
        for (int i = 0; i < 5; i++) {
            // getValue() method returns generated values
            double x = n.getValue();
            System.out.printf("%3d %15f %n", i+1, x);
        }
    }
}
