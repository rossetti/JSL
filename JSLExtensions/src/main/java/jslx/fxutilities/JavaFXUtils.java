package jslx.fxutilities;

import javafx.application.Platform;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class JavaFXUtils {

    public static void main(String[] args) {

        Platform.startup(() ->
        {
            new TestFX().start(new Stage());
            // This block will be executed on JavaFX Thread
        });

        System.out.println("I'm here!");

//        Platform.runLater(new Runnable() {
//            public void run() {
//                new TestFX().start(new Stage());
//            }
//        });
    }

    /**
     * Constructs a JavaFX XYChart.Series from the supplied data arrays. The
     * series will be filled from x and y based on the array that has the minimum length.
     * Any values in the bigger array are not included in the series. If an array has
     * no elements then the series will be empty.
     *
     * @param x   an array of values of type X for the x-values, must not be null
     * @param y   an array of values of type Y for the y-values, must not be null
     * @param <X> the type of the x array
     * @param <Y> the type of the y array
     * @return the series
     */
    public static <X, Y> XYChart.Series<X, Y> makeXYChartSeries(X[] x, Y[] y) {
        Objects.requireNonNull(x, "The x array was null");
        Objects.requireNonNull(y, "The y array was null");
        int n = Math.min(x.length, y.length);
        XYChart.Series<X, Y> series = new XYChart.Series<>();
        for (int i = 0; i < n; i++) {
            series.getData().add(new XYChart.Data<>(x[i], y[i]));
        }
        return series;
    }

    /**
     * @param s1  the first series, must not be null
     * @param s2  the second series, must not be null
     * @param <X> the type of the X values
     * @param <Y> the type of the Y values
     * @return true if all elements match in the underlying lists
     */
    public static <X, Y> boolean isEqualXYChartSeries(XYChart.Series<X, Y> s1, XYChart.Series<X, Y> s2) {
        Objects.requireNonNull(s1, "The s1 series was null");
        Objects.requireNonNull(s2, "The s2 series was null");
        if (s1.getData().size() != s2.getData().size()) {
            // if size is not equal then can't hold same data
            return false;
        }
        // sizes are equal
        int n = s1.getData().size();
        // check every element for equality, if any element is not the same return false
        for (int i = 0; i < n; i++) {
            XYChart.Data<X, Y> s1Data = s1.getData().get(i);
            XYChart.Data<X, Y> s2Data = s2.getData().get(i);
            X x1Value = s1Data.getXValue();
            X x2Value = s2Data.getXValue();
            if (!x1Value.equals(x2Value)) {
                return false;
            }
            Y y1Value = s1Data.getYValue();
            Y y2Value = s2Data.getYValue();
            if (!y1Value.equals(y2Value)) {
                return false;
            }
        }
        // every element must have been the same
        return true;
    }

    /**
     * Reduces a list of series to a set containing only those series that are not the same.
     * Thus, if the set is empty, all the series contain the same data
     *
     * @param seriesList a list of XYChart.Series, must not be null
     * @param <X>        the type of the X values
     * @param <Y>        the type of the Y values
     * @return a set containing the series that are different from each other
     */
    public static <X, Y> Set<XYChart.Series<X, Y>> getUniqueSeries(List<XYChart.Series<X, Y>> seriesList) {
        Objects.requireNonNull(seriesList, "The list of series was null");
        Set<XYChart.Series<X, Y>> set = new HashSet<>();
        if (seriesList.isEmpty()) {
            return set;
        }
        for (XYChart.Series<X, Y> outer : seriesList) {
            for (XYChart.Series<X, Y> inner : seriesList) {
                if (set.contains(outer)) {
                    // no need to check against any other series, outer is already in set
                    break;
                }
                if (outer == inner) {
                    // no need to check against itself
                    continue;
                }
                // if inner is already in the set no need to check
                if (set.contains(inner)) {
                    continue;
                }
                if (!isEqualXYChartSeries(outer, inner)) {
                    // if different then they should be retained
                    set.add(outer);
                    set.add(inner);
                }
            }
        }
        return set;
    }
}
