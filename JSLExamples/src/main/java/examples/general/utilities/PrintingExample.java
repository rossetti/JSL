package examples.general.utilities;

import jsl.utilities.JSLArrayUtil;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.reporting.TableFormatter;


public class PrintingExample {

    public static void main(String[] args) {
        double[][] array = new double[5][6];

        JSLArrayUtil.fill(array, new NormalRV());

        String asString = TableFormatter.asString(array);

        System.out.println(asString);
    }
}
