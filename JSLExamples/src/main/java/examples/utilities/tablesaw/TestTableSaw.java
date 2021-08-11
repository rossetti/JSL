package examples.utilities.tablesaw;

import jsl.utilities.reporting.JSL;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.ScatterPlot;

import java.io.IOException;
import java.nio.file.Path;

public class TestTableSaw {

    public static void main(String[] args) throws IOException {
        // run TestJSL.java to generate the data.csv file
        Path path = JSL.getInstance().getOutDir().resolve("data.csv");
        Table table = Table.read().csv(path.toFile());

        System.out.println(table.structure());

        System.out.println(table.first(10));

        Plot.show(
                ScatterPlot.create("X vs Y Scatter PLot",
                        table, "X", "Y"));
    }
}
