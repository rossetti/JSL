package examples.general.utilities.random;

import jsl.utilities.Interval;
import jsl.utilities.distributions.PDFIfc;
import jsl.utilities.distributions.Uniform;
import jsl.utilities.random.rvariable.AcceptanceRejectionRV;
import jsl.utilities.random.rvariable.RVariableIfc;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;

public class TestAcceptanceRejection {

    public static void main(String[] args) {

        class F implements PDFIfc {

            public double pdf(double x) {
                return (0.75*(1.0 - x*x));
            }

            @Override
            public Interval getDomain() {
                return new Interval(-1.0, 1.0);
            }
        }

        RVariableIfc ar = new AcceptanceRejectionRV(new Uniform(-1.0, 1.0), new F());
        double[] sample = ar.sample(10000);
        DoubleColumn dc = DoubleColumn.create("data", sample);
        Table t = Table.create(dc);
        Plot.show(tech.tablesaw.plotly.api.Histogram.create("Histogram", t, "data"));
    }
}
