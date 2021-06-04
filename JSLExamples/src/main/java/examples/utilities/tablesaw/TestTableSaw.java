package examples.utilities.tablesaw;

import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.ScatterPlot;

import java.io.IOException;

public class TestTableSaw {

    public static void main(String[] args) throws IOException {

        Table table = Table.read().csv("examples/data.csv");

        System.out.println(table.structure());

        System.out.println(table.first(10));

        Plot.show(
                ScatterPlot.create("X vs Y Scatter PLot",
                        table, "X", "Y"));
    }
}
