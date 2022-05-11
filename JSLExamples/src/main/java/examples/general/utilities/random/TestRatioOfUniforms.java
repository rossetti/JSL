package examples.general.utilities.random;

import jsl.utilities.Interval;
import jsl.utilities.distributions.PDFIfc;
import jsl.utilities.distributions.Uniform;
import jsl.utilities.math.FunctionIfc;
import jsl.utilities.random.rvariable.AcceptanceRejectionRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import jsl.utilities.random.rvariable.RatioOfUniformsRV;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;

public class TestRatioOfUniforms {

    public static void main(String[] args) {

        class Example88 implements PDFIfc {

            public double pdf(double x) {
                if ((0.0 <= x) && (x <= 1.0)) {
                    return x * x;
                } else {
                    return 0.0;
                }
            }

            @Override
            public Interval getDomain() {
                return new Interval(0.0, 1.0);
            }
        }

        RVariableIfc ar = new RatioOfUniformsRV(1.0, 0.0, 1.0, new Example88());
        double[] sample = ar.sample(10000);
        DoubleColumn dc = DoubleColumn.create("data", sample);
        Table t = Table.create(dc);
        Plot.show(tech.tablesaw.plotly.api.Histogram.create("Histogram", t, "data"));
    }
}
