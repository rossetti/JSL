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
package examples.general.spatial;

import jsl.modeling.elements.spatial.TripWriter;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.simulation.SimulationReporter;
import jsl.modeling.elements.spatial.CoordinateIfc;
import jsl.modeling.elements.spatial.Euclidean2DPlane;
import jsl.modeling.elements.spatial.EuclideanStepBasedMovementController;
import jsl.modeling.elements.spatial.MovementControllerIfc;
import jsl.modeling.elements.spatial.RG2DMover;
import jsl.modeling.elements.spatial.RG2DMoverWriter;
import jsl.modeling.elements.spatial.RandomMover;
import jsl.modeling.elements.spatial.RectangularGridSpatialModel2D;
import jsl.modeling.elements.spatial.RectangularCell2D;
import jsl.modeling.elements.spatial.RectangularGridModel;
import jsl.modeling.elements.spatial.SpatialElement;
import jsl.modeling.elements.spatial.SpatialModel;
import jsl.utilities.reporting.StatisticReporter;

/**
 *
 * @author rossetti
 */
public class TestSpatialModel {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //test2();
        //test3();
        test4();

    }

    public static void test1() {
        RectangularGridSpatialModel2D smodel = new RectangularGridSpatialModel2D(10, 20, 2, 2);
        System.out.println(smodel);
        System.out.println();

        CoordinateIfc coordinate = smodel.getCoordinate(7.5, 15);

        RectangularCell2D cell = smodel.getCell(coordinate);
        System.out.println(cell);
        System.out.println();

        RectangularCell2D[][] mooreNeighborhood = smodel.getMooreNeighborhood(cell);
        System.out.println("Moore Neighborhood");
        System.out.println(Arrays.deepToString(mooreNeighborhood));
        System.out.println();

        List<RectangularCell2D> list = smodel.getMooreNeighborhoodAsList(cell, true);
        System.out.println("Moore Neighborhood as list with core cell");
        System.out.println(list);
        System.out.println();

        list = smodel.getMooreNeighborhoodAsList(cell, false);
        System.out.println("Moore Neighborhood as list without core cell");
        System.out.println(list);

    }

    public static void test2() {
        SpatialModel smodel = new Euclidean2DPlane();

        Simulation sim = new Simulation("Spatial Model");

        Model model = sim.getModel();
        RandomMover rm = new RandomMover(model, smodel);
        rm.turnOnTripStatistics();
        rm.setDefaultNextTripOption(true);
        rm.setMaxNumTrips(12);
        MovementControllerIfc c = new EuclideanStepBasedMovementController(model);
        rm.setMovementController(c);
        System.out.println("Setting up the simulation");
        sim.setNumberOfReplications(2);
        sim.setLengthOfReplication(100.0);
        SimulationReporter r = sim.makeSimulationReporter();

        System.out.println("**** Running the simulation");
        sim.run();
        System.out.println("**** After the simulation");
        System.out.println(sim);
        StatisticReporter sr = new StatisticReporter(r.getAcrossReplicationStatisticsList());
        System.out.println(sr.getHalfWidthSummaryReport());
    }

    public static void test3() {
        RectangularGridSpatialModel2D smodel = new RectangularGridSpatialModel2D(10, 20, 2, 2);

        System.out.println();
        System.out.println("--------------------------------------");
        System.out.println("Spatial model before adding movers");
        System.out.println(smodel);
        System.out.println("--------------------------------------");
        System.out.println("****");
        System.out.println("Notice that there are no spatial elements in the cells.");
        System.out.println("****");
        System.out.println();

        // make a few things and put them in the model
        Simulation sim = new Simulation("Spatial Model");
        Model model = sim.getModel();
        MovementControllerIfc mc = new EuclideanStepBasedMovementController(model);

        System.out.println("Making the RandomMovers");
        RandomMover rm1 = new RandomMover(model, "RM1", smodel);
        rm1.setInitialPosition(smodel.getCoordinate(5.5, 5.5));
        RandomMover rm2 = new RandomMover(model, "RM2", smodel);
        rm2.setInitialPosition(smodel.getCoordinate(0, 0));
        RandomMover rm3 = new RandomMover(model, "RM3", smodel);
        rm3.setInitialPosition(smodel.getCoordinate(.5, .5));

        // capture the movement to a file
        PrintWriter writer =sim.getOutputDirectory().makePrintWriter("RMovers");
        TripWriter cm = new TripWriter(writer);
        rm1.addObserver(cm);
        rm2.addObserver(cm);
        rm3.addObserver(cm);

        // allow random movers to automatically start a next trip
        rm1.setDefaultNextTripOption(true);
        rm2.setDefaultNextTripOption(true);
        rm3.setDefaultNextTripOption(true);

        System.out.println();
        System.out.println("--------------------------------------");
        System.out.println("Spatial model after adding movers");
        System.out.println(smodel);
        System.out.println("--------------------------------------");
        System.out.println("****");
        System.out.println("Notice that initial positions are different than");
        System.out.println("current positions. Current position and initial position");
        System.out.println("will be initialized to same value when model is initilized.");
        System.out.println("****");
        System.out.println();
        System.out.println("How to check what is in a cell:");
        RectangularCell2D c = smodel.getCell(smodel.getCoordinate(0, 0));
        System.out.print("Cell(");
        System.out.print(c.getRowIndex());
        System.out.print(",");
        System.out.print(c.getColumnIndex());
        System.out.print(") has ");
        System.out.print(c.countModelElements(RandomMover.class));
        System.out.println(" RandomMovers.");
        System.out.print("and has ");
        System.out.print(c.countSpatialElements(SpatialElement.class));
        System.out.println(" SpatialElements.");
        System.out.println("****");
        System.out.println("Notice that the position attribute determines what is in the cell,");
        System.out.println("not the initial position.");
        System.out.println("****");

        System.out.println();
        System.out.println("Tell the movers to use the step based movement controller");
        System.out.println("Notice that they can share a controller");
        rm1.setMovementController(mc);
        rm2.setMovementController(mc);
        rm3.setMovementController(mc);

        Iterator<RectangularCell2D> i = smodel.getCellIterator();
        while (i.hasNext()) {
            RectangularCell2D cell = i.next();
            System.out.print("Cell(");
            System.out.print(cell.getRowIndex());
            System.out.print(",");
            System.out.print(cell.getColumnIndex());
            System.out.print(") has ");
            System.out.print(cell.countModelElements(RandomMover.class));
            System.out.println(" RandomMovers.");
        }

        System.out.println("Setting up the simulation");
        sim.setNumberOfReplications(1);
        sim.setLengthOfReplication(100.0);
        System.out.println("**** Running the simulation");
        sim.run();
        System.out.println("**** After the simulation");

        System.out.println(sim);
        System.out.println();
        System.out.println("--------------------------------------");
        System.out.println("Spatial model after running the model:");
        System.out.println(smodel);
        System.out.println("--------------------------------------");
        System.out.println("****");
        System.out.println("Notice that some movers are in different cells");
        System.out.println("****");

        i = smodel.getCellIterator();
        while (i.hasNext()) {
            RectangularCell2D cell = i.next();
            System.out.print("Cell(");
            System.out.print(cell.getRowIndex());
            System.out.print(",");
            System.out.print(cell.getColumnIndex());
            System.out.print(") has ");
            System.out.print(cell.countModelElements(RandomMover.class));
            System.out.println(" RandomMovers.");
        }

        RectangularCell2D fcell = smodel.findCellWithMinimumElementsInNeighborhood(c);
        System.out.println(fcell);

        int v = fcell.countSpatialElements(SpatialElement.class);
        System.out.println("v = " + v);

    }

    public static void test4() {

        Simulation sim = new Simulation("Spatial Model");
        Model model = sim.getModel();

        System.out.println("**** Setting up the grid");
        RectangularGridModel rgm = new RectangularGridModel(model, 10000, 10000, 1000, 1000);

        System.out.println("**** Making the RG2DMovers");
        //optionally set up an observer to trace the movement
        PrintWriter writer =sim.getOutputDirectory().makePrintWriter("RG2DMoverTest");
        RG2DMoverWriter cm = new RG2DMoverWriter(writer);

        for (int i = 1; i <= 5; i++) {
            RG2DMover rG2DMover1 = new RG2DMover(rgm, "RG2DM-" + i);
            rG2DMover1.setDefaultNextTripOption(true);
            rG2DMover1.setInitialPositionRandomly();
            rG2DMover1.setStartRandomlyOption(true);
            //rG2DMover1.turnOnTripStatistics();
            rG2DMover1.addObserver(cm);
        }

        System.out.println("Setting up the simulation");
        sim.setNumberOfReplications(2);
        sim.setLengthOfReplication(10.0);
        SimulationReporter r = sim.makeSimulationReporter();

        System.out.println("**** Running the simulation");
        sim.run();
        System.out.println("**** After the simulation");

        StatisticReporter sr = new StatisticReporter(r.getAcrossReplicationStatisticsList());
        System.out.println(sr.getHalfWidthSummaryReport());
    }
}
