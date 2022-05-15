package examples.general.utilities.statistics;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.random.rvariable.NormalRV;

import java.io.PrintWriter;
import java.util.Arrays;

public class TestSomething {

    public static void main(String[] args) {
        double x = 10.668;

        System.out.printf("rint(%f) = %f %n", x, Math.rint(x));
        System.out.printf("IEEEremainder(%f, 1) = %f %n", x, Math.IEEEremainder(x, 1));
        System.out.printf("FractionalPart(%f) = %f %n", x, x%1);

        double[] sample = new NormalRV(10.0, 1.0).sample(5);
        double[] cp = Arrays.copyOf(sample, sample.length);
        JSLArrayUtil.remainder(cp, 1.0);
        System.out.println(JSLArrayUtil.toCSVString(sample));
        System.out.println(JSLArrayUtil.toCSVString(cp));
        System.out.println();
        double[] y = Arrays.copyOf(sample, sample.length);
        JSLArrayUtil.apply(y, Math::sqrt);
        System.out.println(JSLArrayUtil.toCSVString(y));
    }
}
