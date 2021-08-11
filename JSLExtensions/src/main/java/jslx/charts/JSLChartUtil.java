package jslx.charts;

import jsl.utilities.reporting.JSL;
import jsl.utilities.statistic.Statistic;
import jsl.utilities.statistic.welch.WelchDataArrayObserver;
import jsl.utilities.statistic.welch.WelchDataFileAnalyzer;
//import org.knowm.xchart.XYChart;
//import org.knowm.xchart.XYChartBuilder;
//import org.knowm.xchart.XYSeries;
//import org.knowm.xchart.style.Styler;
//import org.knowm.xchart.XYChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.components.Axis;
import tech.tablesaw.plotly.components.Figure;
import tech.tablesaw.plotly.components.Layout;
import tech.tablesaw.plotly.traces.ScatterTrace;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Objects;

public class JSLChartUtil {

    public static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final SimpleDateFormat TIME_STAMP_FORMATTER = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    public static final String DEFAULT_FIGURE_FILE_NAME = "DefaultFigure_";

    /** Shows the plot in a web page and makes an html file of the plot. Squelches inconvenient FileIO exceptions
     *  The plot file is named DefaultFigure_ + yyyy_MM_dd_HH_mm_ss
     * @param figureToPlot the constructed figure to plot
     * @return false if there was a problem/error
     */
    public static boolean showPlot(Figure figureToPlot){
        // get a time stamp
        Instant now = Instant.now();
        Timestamp ts = Timestamp.from(Instant.now());
        String fileName = DEFAULT_FIGURE_FILE_NAME + TIME_STAMP_FORMATTER.format(ts);
        return showPlot(figureToPlot, fileName);
    }

    /** Shows the plot in a web page and makes an html file of the plot. Squelches inconvenient FileIO exceptions
     *
     * @param figureToPlot the constructed figure to plot
     * @param nameOfFigureFile the name of the file in the jslOutput/plotDir to hold the plot
     * @return false if there was a problem/error
     */
    public static boolean showPlot(Figure figureToPlot, String nameOfFigureFile){
        Objects.requireNonNull(nameOfFigureFile, "The name of the figure file was null!");
        Objects.requireNonNull(figureToPlot, "The figure to plot was null!");
        File plotDir = JSL.getInstance().makeSubDirectory("plotDir").toFile();
        Path path = plotDir.toPath().resolve(nameOfFigureFile + ".html");
        return showPlotNE(figureToPlot,path);
    }

    /** Shows the plot in a web page and makes an html file of the plot. Squelches inconvenient FileIO exceptions
     *
     * @param figureToPlot the constructed figure to plot
     * @param pathToSaveFigure the path to the file that will hold the html for the plot
     * @return false if there was a problem/error
     */
    public static boolean showPlotNE(Figure figureToPlot, Path pathToSaveFigure){
        try {
            showPlot(figureToPlot, pathToSaveFigure);
            return true;
        } catch (IOException e) {
            LOGGER.error("There was a problem with the path {} to the figure.", pathToSaveFigure);
            return false;
        }
    }

    /** Shows the plot in a web page and makes an html file of the plot.
     *
     * @param figureToPlot the constructed figure to plot
     * @param pathToSaveFigure the path to the file that will hold the html for the plot
     * @throws IOException if a problem with the temp files occurs
     */
    public static void showPlot(Figure figureToPlot, Path pathToSaveFigure) throws IOException {
        Objects.requireNonNull(figureToPlot, "The figure to plot was null!");
        Objects.requireNonNull(pathToSaveFigure, "The path to the plot file was null!");
        Files.createDirectories(pathToSaveFigure.getParent());
        Plot.show(figureToPlot, pathToSaveFigure.toFile());
    }

    /**
     * Warning: This method uses plotly. If there are too many data points then it may not work.
     * Stick with less than 50K points
     *
     * @param dataName    the name of the response
     * @param partialSums the partial sums to plot
     * @return the figure
     */
    public static Figure makePartialSumsPlotFigure(String dataName, double[] partialSums) {
        Objects.requireNonNull(dataName, "The data name was null!");
        Objects.requireNonNull(partialSums, "The partial sums array was null!");
        double[] js = new double[partialSums.length];
        for (int i = 0; i < js.length; i++) {
            js[i] = i;
        }
        Axis xAxis = Axis.builder()
                .title("Indices")
                .build();
        Axis yAxis = Axis.builder()
                .title(dataName)
                .build();
        Layout layout = Layout.builder()
                .title("Partial Sums Plot for " + dataName)
                .xAxis(xAxis)
                .yAxis(yAxis)
                .height(800)
                .width(1200)
                .build();
        ScatterTrace partial_sums = ScatterTrace.builder(js, partialSums)
                .mode(ScatterTrace.Mode.LINE)
                .name("Partial Sums")
                .build();
        Figure figure = new Figure(layout, partial_sums);
        return figure;
    }

    /**
     * Warning: This method uses plotly. If there are too many data points then it may not work.
     * This will only permit less than 50K points
     *
     * @param analyzer the WelchDataFileAnalyzer that has the Welch data to display
     * @return the figure
     */
    public static Figure makeWelchPlotFigure(WelchDataFileAnalyzer analyzer) {
        Objects.requireNonNull(analyzer, "The data array collector was null!");
        long nObs = analyzer.getMinNumObservationsInReplications();
        int totalNumObservations = Math.toIntExact(nObs);
        return makeWelchPlotFigure(analyzer, totalNumObservations);
    }

    /**
     * Warning: This method uses plotly. If there are too many data points then it may not work.
     * This will only permit less than 50K points
     *
     * @param analyzer the WelchDataFileAnalyzer that has the Welch data to display
     * @param totalNumObservations the number of observations to plot
     * @return the figure
     */
    public static Figure makeWelchPlotFigure(WelchDataFileAnalyzer analyzer, int totalNumObservations) {
        Objects.requireNonNull(analyzer, "The data array collector was null!");
        double deltaT = analyzer.getAverageTimePerObservation();
        String title = String.format("Welch Plot for %s , 1 obs = %.2f time units.", analyzer.getResponseName(), deltaT);
        return makeWelchPlotFigure(title, analyzer.getResponseName(),
                analyzer.getWelchAveragesNE(totalNumObservations), analyzer.getCumulativeWelchAverages(totalNumObservations));
    }

    /**
     * Warning: This method uses plotly. If there are too many data points then it may not work.
     * Stick with less than 50K points
     *
     * @param dataArrayObserver the WelchDataArrayObserver that has the Welch data to display
     * @return the figure
     */
    public static Figure makeWelchPlotFigure(WelchDataArrayObserver dataArrayObserver) {
        Objects.requireNonNull(dataArrayObserver, "The data array collector was null!");
        double[] tbo =dataArrayObserver.getAvgTimeBtwObservationsForEachReplication();
        double deltaT = Statistic.collectStatistics(tbo).getAverage();
        String title = String.format("Welch Plot for %s , 1 obs = %.2f time units.", dataArrayObserver.getResponseName(), deltaT);
        return makeWelchPlotFigure(title, dataArrayObserver.getResponseName(),
                dataArrayObserver.getWelchAverages(), dataArrayObserver.getWelchCumulativeAverages());
    }

    /**
     * Warning: This method uses plotly. If there are too many data points then it may not work.
     * Stick with less than 50K points
     *
     * @param title the title of the chart
     * @param dataName the name of the response
     * @param avg      the Welch averages
     * @param cumAvg   the cumulative averages
     * @return the figure
     */
    public static Figure makeWelchPlotFigure(String title, String dataName, double[] avg, double[] cumAvg) {
        Objects.requireNonNull(title, "The title was null!");
        Objects.requireNonNull(dataName, "The data name was null!");
        Objects.requireNonNull(avg, "The average array was null!");
        Objects.requireNonNull(cumAvg, "The cumulative average array was null!");
        if (avg.length != cumAvg.length) {
            throw new IllegalArgumentException("The array sizes must be equal!");
        }
        double[] js = new double[avg.length];
        for (int i = 0; i < js.length; i++) {
            js[i] = i;
        }
        Axis xAxis = Axis.builder()
                .title("Observation Number")
                .build();
        Axis yAxis = Axis.builder()
                .title(dataName)
                .build();
        Layout layout = Layout.builder()
                .title(title)
                .xAxis(xAxis)
                .yAxis(yAxis)
                .height(800)
                .width(1200)
                .build();
        ScatterTrace avgTrace = ScatterTrace.builder(js, avg)
                .mode(ScatterTrace.Mode.LINE)
                .name("Welch Average")
                .build();
        ScatterTrace cumAvgTrace = ScatterTrace.builder(js, cumAvg)
                .mode(ScatterTrace.Mode.LINE)
                .name("Cumulative Average")
                .build();
        Figure figure = new Figure(layout, avgTrace, cumAvgTrace);
        return figure;
    }
}


//TODO  calling Plot.show() causes the following classes to log DEBUG statements to the log
        // need to figure out a way to stop it, or at least have them go to another file
// c.m.pebble.loader.ClasspathLoader
// c.m.pebble.PebbleEngine
//    c.m.pebble.lexer.TemplateSource


//    /**
//     * Warning: You may want to carefully control the number of observations on the plot
//     *
//     * @param analyzer the WelchDataArrayObserver that has the Welch data to display
//     */
//    public static XYChart makeWelchPlot(WelchDataFileAnalyzer analyzer, int numObs) {
//        Objects.requireNonNull(analyzer, "The data array collector was null!");
//        numObs = Math.min(50000, numObs);
//        return makeWelchPlot(analyzer.getBaseFileName().toString(),
//                analyzer.getWelchAveragesNE(numObs), analyzer.getCumulativeWelchAverages(numObs));
//    }
//
//    /**
//     * If there are too many data points then it may not work.
//     * Stick with less than 50K points
//     *
//     * @param dataArrayObserver the WelchDataArrayObserver that has the Welch data to display
//     */
//    public static XYChart makeWelchPlot(WelchDataArrayObserver dataArrayObserver) {
//        Objects.requireNonNull(dataArrayObserver, "The data array collector was null!");
//        return makeWelchPlot(dataArrayObserver.getResponseName(),
//                dataArrayObserver.getWelchAverages(), dataArrayObserver.getWelchCumulativeAverages());
//    }
//
//    /**
//     * If there are too many data points then it may not work.
//     * Stick with less than 50K points
//     *
//     * @param dataName the name of the response
//     * @param avg      the Welch averages
//     * @param cumAvg   the cumulative averages
//     */
//    public static XYChart makeWelchPlot(String dataName, double[] avg, double[] cumAvg) {
//        Objects.requireNonNull(dataName, "The data name was null!");
//        Objects.requireNonNull(avg, "The average array was null!");
//        Objects.requireNonNull(cumAvg, "The cumulative average array was null!");
//        if (avg.length != cumAvg.length) {
//            throw new IllegalArgumentException("The array sizes must be equal!");
//        }
//        double[] js = new double[avg.length];
//        for (int i = 0; i < js.length; i++) {
//            js[i] = i;
//        }
//
//        XYChart chart = new XYChartBuilder()
//                .width(1200)
//                .height(600)
//                .title("Welch Plot for " + dataName)
//                .xAxisTitle("Observation Number")
//                .yAxisTitle(dataName)
//                .build();
//
//        XYSeries welch_average = chart.addSeries("Welch Average", js, avg);
//        XYSeries cumulative_average = chart.addSeries("Cumulative Average", js, cumAvg);
//        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
//
////        chart.getStyler().setXAxisTickMarkSpacingHint(1000);
////        chart.getStyler().setXAxisMaxLabelCount(1000);
////        chart.getStyler().setXAxisDecimalPattern("###.##");
//XYChart welchPlot = JSLChartUtil.makeWelchPlot(rv_welch);
//        new SwingWrapper(welchPlot).displayChart();
//        return chart;
//    }

