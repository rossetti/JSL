/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jslx.fxutilities;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
//import jsl.utilities.statistic.welch.deprecated.WelchDataFileAnalyzer;
import jsl.utilities.statistic.welch.WelchDataFileAnalyzer;

/** Use JavaFX to display a WelchChart.  It must have two arguments when
 *  launched: first is a string that represents the path to a wpdf file
 *  made by WelchDataFileAnalyzer.  It is best to launch this via 
 *  the WelchDataFileAnalyzer class
 *
 * @author rossetti
 */
public class WelchChart extends Application {

    private DataInputStream myDIn;
    private long myNumObs;

    /** Opens up a JavaFX window with a chart displaying the Welch plot using the minimum number of observations
     *  across the replications
     *
     * @param welchDataFileAnalyzer must not be null
     */
    public static void display(WelchDataFileAnalyzer welchDataFileAnalyzer){
        Objects.requireNonNull(welchDataFileAnalyzer, "The WelchDataFileAnalyzer was null");
        long numObs = welchDataFileAnalyzer.getMinNumObservationsInReplications();
        File wpdf = welchDataFileAnalyzer.makeWelchPlotDataFile(numObs);
        display(wpdf, numObs);
    }

    /** Opens up a JavaFX window with a chart displaying the Welch plot
     *
     * @param welchDataFileAnalyzer must not be null
     * @param numObs the number of observations to display
     */
    public static void display(WelchDataFileAnalyzer welchDataFileAnalyzer, long numObs){
        Objects.requireNonNull(welchDataFileAnalyzer, "The WelchDataFileAnalyzer was null");
        File wpdf = welchDataFileAnalyzer.makeWelchPlotDataFile(numObs);
        display(wpdf, numObs);
    }

    /**
     * Opens up a JavaFX window with a chart displaying the Welch plot
     *
     * @param file must be a file of type wpdf made by a WelchDataCollector, must not be null
     * @param numObs the number of observations to be on the chart
     */
    public static void display(File file, long numObs) {
        Objects.requireNonNull(file, "The file was null");
        String[] args = new String[2];
        String canonicalPath = null;
        try {
            canonicalPath = file.getCanonicalPath();
            args[0] = canonicalPath;
            args[1] = Long.toString(numObs);
            // (new Thread(new WelchChartRunnable(args))).start();
            launchWelchChart(args);
        } catch (IOException ex) {
            Logger.getLogger(WelchDataFileAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        final File myFile;
        final FileInputStream myFIn;
        final Parameters params = getParameters();
        final List<String> parameters = params.getRaw();
        if (parameters.size() != 2) {
            throw new IllegalArgumentException("The number of parameters must be 2");
        }
        Path path = Paths.get(parameters.get(0));
        myFile = path.toFile();
        myNumObs = Long.parseLong(parameters.get(1));
        try {
            myFIn = new FileInputStream(myFile);
            myDIn = new DataInputStream(myFIn);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(WelchChart.class.getName()).log(Level.SEVERE, null, ex);
        }

        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        ScatterChart<Number, Number> scatterChart = new ScatterChart<>(xAxis, yAxis);
        xAxis.setLabel("Observation Number");
        yAxis.setLabel("Observation");
        scatterChart.setTitle("Welch Plot Chart");
        scatterChart.setAnimated(false);

        ObservableList<XYChart.Series<Number, Number>> chartData = getChartData();

        scatterChart.setData(chartData);

        StackPane root = new StackPane();
        root.getChildren().add(scatterChart);
        Scene scene = new Scene(root, 600, 550);
        primaryStage.setTitle("Welch Plot Analysis");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ObservableList<XYChart.Series<Number, Number>> getChartData() {
        ObservableList<XYChart.Series<Number, Number>> data = FXCollections.observableArrayList();
        XYChart.Series<Number, Number> myWelchAvg = new XYChart.Series<>();
        XYChart.Series<Number, Number> myCumAvg = new XYChart.Series<>();;
        myWelchAvg.setName("avg");
        myCumAvg.setName("cum avg");
        for (long i = 1; i <= myNumObs; i++) {
            try {
                double avg = myDIn.readDouble();
                double cavg = myDIn.readDouble();
                XYChart.Data<Number, Number> avgDataPoint = new XYChart.Data<>(i, avg);
                XYChart.Data<Number, Number> cAvgDataPoint = new XYChart.Data<>(i, cavg);
                myWelchAvg.getData().add(avgDataPoint);
                myCumAvg.getData().add(cAvgDataPoint);
            } catch (IOException ex) {
                Logger.getLogger(WelchChart.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        data.add(myWelchAvg);
        data.add(myCumAvg);
        return data;
    }

    public static void launchWelchChart(String[] args) {
        WelchChart.launch(WelchChart.class, args);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
